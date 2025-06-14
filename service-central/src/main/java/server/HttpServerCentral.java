package server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpContext;
import rmi.Serveur;
import handlers.*;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.logging.Level;


public class HttpServerCentral {

    private static final Logger LOGGER = Logger.getLogger(HttpServerCentral.class.getName());

    private final int port;
    private final boolean httpsEnabled;
    private final String keystorePath;
    private final String keystorePassword;
    private HttpServer server;
    private final Serveur serviceCentral;

    public HttpServerCentral(int port, Serveur serviceCentral) {
        this(port, serviceCentral, false, null, null);
    }

    public HttpServerCentral(int port, Serveur serviceCentral, boolean httpsEnabled,
                             String keystorePath, String keystorePassword) {
        this.port = port;
        this.serviceCentral = serviceCentral;
        this.httpsEnabled = httpsEnabled;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
    }


    public void start() throws Exception {
        if (httpsEnabled) {
            startHttpsServer();
        } else {
            startHttpServer();
        }
    }


    private void startHttpsServer() throws Exception {
        LOGGER.info("Démarrage du serveur HTTPS sur le port " + port);

        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            keyStore.load(fis, keystorePassword.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, keystorePassword.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(port), 0);
        httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            @Override
            public void configure(HttpsParameters params) {
                params.setSSLParameters(sslContext.getDefaultSSLParameters());
            }
        });

        this.server = httpsServer;
        configureServer();

        LOGGER.info("Serveur HTTPS démarré sur https://172.22.152.208:" + port);
    }


    private void startHttpServer() throws IOException {
        LOGGER.info("Démarrage du serveur HTTP sur le port " + port);

        server = HttpServer.create(new InetSocketAddress(port), 0);
        configureServer();

        LOGGER.info("Serveur HTTP démarré sur http://172.22.152.208:" + port);
    }


    private void configureServer() {
        server.setExecutor(Executors.newFixedThreadPool(10));

        createContexts();

        server.start();

        LOGGER.info("Routes disponibles:");
        LOGGER.info("  GET  /restaurants");
        LOGGER.info("  GET  /tables/{restaurantId}");
        LOGGER.info("  POST /reserver");
        LOGGER.info("  GET  /velib");
        LOGGER.info("  GET  /incidents");
        LOGGER.info("  GET  /services/etat");
    }


    private void createContexts() {

        HttpContext restaurantsContext = server.createContext("/restaurants",
                new RestaurantsHandler(serviceCentral));

        HttpContext tablesContext = server.createContext("/tables",
                new TablesHandler(serviceCentral));

        HttpContext reserverContext = server.createContext("/reserver",
                new ReserverHandler(serviceCentral));


        HttpContext incidentsContext = server.createContext("/incidents",
                new IncidentsHandler(serviceCentral));

        HttpContext etatContext = server.createContext("/services/etat",
                new EtatServicesHandler(serviceCentral));

        CorsFilter corsFilter = new CorsFilter();
        restaurantsContext.getFilters().add(corsFilter);
        tablesContext.getFilters().add(corsFilter);
        reserverContext.getFilters().add(corsFilter);
        incidentsContext.getFilters().add(corsFilter);
        etatContext.getFilters().add(corsFilter);

        LOGGER.info("Contextes HTTP créés avec filtres CORS");
    }


    public void stop() {
        if (server != null) {
            LOGGER.info("Arrêt du serveur...");
            server.stop(0);
            LOGGER.info("Serveur arrêté");
        }
    }
}