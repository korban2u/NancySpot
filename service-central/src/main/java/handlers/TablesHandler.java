package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import rmi.Serveur;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Handler pour la route /tables/{restaurantId}
 * Récupère les tables libres d'un restaurant
 */
public class TablesHandler implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(TablesHandler.class.getName());
    private final Serveur serviceCentral;

    public TablesHandler(Serveur serviceCentral) {
        this.serviceCentral = serviceCentral;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        LOGGER.info("Requête reçue : " + exchange.getRequestMethod() + " " + path);

        try {
            // Vérifier la méthode HTTP
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Méthode non autorisée. Utilisez GET.");
                return;
            }

            // Extraire l'ID du restaurant depuis l'URL
            // Format attendu : /tables/1 ou /tables?restaurantId=1
            int restaurantId = extractRestaurantId(exchange);

            if (restaurantId <= 0) {
                sendError(exchange, 400, "ID restaurant manquant ou invalide. Utilisez /tables/{id} ou /tables?restaurantId={id}");
                return;
            }

            // Appeler le service via le service central
            String jsonResponse = serviceCentral.getTablesLibres(restaurantId);

            // Envoyer la réponse
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }

            LOGGER.info("Tables libres envoyées pour restaurant " + restaurantId);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du traitement de la requête", e);
            sendError(exchange, 500, "Erreur serveur : " + e.getMessage());
        }
    }

    /**
     * Extraire l'ID du restaurant depuis l'URL ou les paramètres
     */
    private int extractRestaurantId(HttpExchange exchange) {
        try {
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();

            // Méthode 1 : /tables/123
            if (path.startsWith("/tables/")) {
                String idStr = path.substring("/tables/".length());
                if (!idStr.isEmpty()) {
                    return Integer.parseInt(idStr);
                }
            }

            // Méthode 2 : /tables?restaurantId=123
            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=", 2);
                    if (keyValue.length == 2 && "restaurantId".equals(keyValue[0])) {
                        return Integer.parseInt(keyValue[1]);
                    }
                }
            }

            return -1;

        } catch (NumberFormatException e) {
            LOGGER.warning("Format d'ID restaurant invalide");
            return -1;
        }
    }

    /**
     * Envoyer une erreur HTTP
     */
    private void sendError(HttpExchange exchange, int code, String message) throws IOException {
        String jsonError = "{\"error\": true, \"message\": \"" + message + "\"}";
        byte[] errorBytes = jsonError.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(code, errorBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(errorBytes);
        }
    }
}