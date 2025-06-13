package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Reverse Proxy HTTPS qui redirige toutes les requêtes vers le Service Central HTTP
 * Permet d'accéder au Service Central via HTTPS depuis webetu
 */
public class HttpsProxyServer {

    private static final Logger LOGGER = Logger.getLogger(HttpsProxyServer.class.getName());

    private final int httpsPort;
    private final String backendHost;
    private final int backendPort;
    private HttpsServer server;
    private final HttpClient httpClient;

    public HttpsProxyServer(int httpsPort, String backendHost, int backendPort) {
        this.httpsPort = httpsPort;
        this.backendHost = backendHost;
        this.backendPort = backendPort;

        // Client HTTP pour communiquer avec le backend
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Démarrer le proxy HTTPS
     */
    public void start() throws Exception {
        LOGGER.info("Démarrage du proxy HTTPS sur le port " + httpsPort);
        LOGGER.info("Backend: http://" + backendHost + ":" + backendPort);

        // Créer le serveur HTTPS
        server = HttpsServer.create(new InetSocketAddress(httpsPort), 0);

        // Configurer SSL avec un certificat auto-signé
        SSLContext sslContext = createSelfSignedSSLContext();
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            @Override
            public void configure(HttpsParameters params) {
                params.setSSLParameters(getSSLContext().getDefaultSSLParameters());
            }
        });

        // Configurer le pool de threads
        server.setExecutor(Executors.newFixedThreadPool(10));

        // Handler proxy pour toutes les routes
        server.createContext("/", new ProxyHandler());

        // Démarrer le serveur
        server.start();

        LOGGER.info("Proxy HTTPS démarré sur https://localhost:" + httpsPort);
        LOGGER.info("Toutes les requêtes sont redirigées vers http://" + backendHost + ":" + backendPort);
    }

    /**
     * Arrêter le proxy
     */
    public void stop() {
        if (server != null) {
            LOGGER.info("Arrêt du proxy HTTPS...");
            server.stop(0);
            LOGGER.info("Proxy HTTPS arrêté");
        }
    }

    /**
     * Handler qui redirige toutes les requêtes vers le backend HTTP
     */
    private class ProxyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();

            // Construire l'URL backend
            String backendUrl = "http://" + backendHost + ":" + backendPort + path;
            if (query != null && !query.isEmpty()) {
                backendUrl += "?" + query;
            }

            LOGGER.info("Proxy: " + method + " " + path + " -> " + backendUrl);

            try {
                // Préparer la requête vers le backend
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(backendUrl))
                        .timeout(Duration.ofSeconds(30));

                // Copier les headers (sauf Host et certains headers sensibles)
                exchange.getRequestHeaders().forEach((key, values) -> {
                    if (!key.equalsIgnoreCase("Host") &&
                            !key.equalsIgnoreCase("Connection") &&
                            !key.equalsIgnoreCase("Content-Length")) {
                        for (String value : values) {
                            requestBuilder.header(key, value);
                        }
                    }
                });

                // Lire le body de la requête si présent
                String requestBody = null;
                if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
                    try (InputStream is = exchange.getRequestBody()) {
                        requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    }
                }

                // Configurer la méthode HTTP et le body
                switch (method.toUpperCase()) {
                    case "GET":
                        requestBuilder.GET();
                        break;
                    case "POST":
                        requestBuilder.POST(HttpRequest.BodyPublishers.ofString(
                                requestBody != null ? requestBody : ""));
                        break;
                    case "PUT":
                        requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(
                                requestBody != null ? requestBody : ""));
                        break;
                    case "DELETE":
                        requestBuilder.DELETE();
                        break;
                    case "OPTIONS":
                        requestBuilder.method("OPTIONS", HttpRequest.BodyPublishers.noBody());
                        break;
                    default:
                        sendError(exchange, 405, "Méthode non supportée: " + method);
                        return;
                }

                // Envoyer la requête au backend
                HttpRequest request = requestBuilder.build();
                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                // Copier les headers de réponse
                response.headers().map().forEach((key, values) -> {
                    if (!key.equalsIgnoreCase("Transfer-Encoding")) {
                        for (String value : values) {
                            exchange.getResponseHeaders().add(key, value);
                        }
                    }
                });

                // Ajouter les headers CORS si pas déjà présents
                if (!exchange.getResponseHeaders().containsKey("Access-Control-Allow-Origin")) {
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Accept, Authorization");
                }

                // Envoyer la réponse
                byte[] responseBytes = response.body().getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(response.statusCode(), responseBytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }

                LOGGER.fine("Proxy: Réponse " + response.statusCode() + " envoyée");

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur proxy pour " + path, e);
                sendError(exchange, 502, "Erreur backend: " + e.getMessage());
            }
        }
    }

    /**
     * Envoyer une erreur HTTP
     */
    private void sendError(HttpExchange exchange, int code, String message) throws IOException {
        String jsonError = "{\"error\": true, \"message\": \"" + message + "\"}";
        byte[] errorBytes = jsonError.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(code, errorBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(errorBytes);
        }
    }

    /**
     * Créer un contexte SSL avec certificat auto-signé
     * ⚠️ UNIQUEMENT POUR LE DÉVELOPPEMENT
     */
    private SSLContext createSelfSignedSSLContext() throws Exception {
        // Créer un TrustManager qui accepte tous les certificats
        X509TrustManager trustAllManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {}

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {}

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };

        // Créer le contexte SSL
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new X509TrustManager[]{trustAllManager}, new SecureRandom());

        return sslContext;
    }

    /**
     * Main pour démarrer le proxy en standalone
     */
    public static void main(String[] args) {
        try {
            // Configuration par défaut
            int httpsPort = args.length > 0 ? Integer.parseInt(args[0]) : 8443;
            String backendHost = args.length > 1 ? args[1] : "localhost";
            int backendPort = args.length > 2 ? Integer.parseInt(args[2]) : 8080;

            // Créer et démarrer le proxy
            HttpsProxyServer proxy = new HttpsProxyServer(httpsPort, backendHost, backendPort);
            proxy.start();

            LOGGER.info("=== Proxy HTTPS opérationnel ===");
            LOGGER.info("Accès HTTPS: https://localhost:" + httpsPort);
            LOGGER.info("Backend HTTP: http://" + backendHost + ":" + backendPort);
            LOGGER.info("Appuyez sur Ctrl+C pour arrêter");

            // Shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(proxy::stop));

            // Garder le proxy actif
            Thread.currentThread().join();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur fatale du proxy HTTPS", e);
            System.exit(1);
        }
    }
}