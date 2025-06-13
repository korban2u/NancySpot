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
 * Handler pour la route /services/etat
 * Retourne l'état des services inscrits
 */
public class EtatServicesHandler implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(EtatServicesHandler.class.getName());
    private final Serveur serviceCentral;

    public EtatServicesHandler(Serveur serviceCentral) {
        this.serviceCentral = serviceCentral;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        LOGGER.info("Requête reçue : " + exchange.getRequestMethod() + " /services/etat");

        try {
            // Vérifier la méthode HTTP
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Méthode non autorisée. Utilisez GET.");
                return;
            }

            // Appeler le service central pour obtenir l'état
            String jsonResponse = serviceCentral.getEtatServices();

            // Envoyer la réponse
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }

            LOGGER.info("État des services envoyé");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du traitement de la requête", e);
            sendError(exchange, 500, "Erreur serveur : " + e.getMessage());
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