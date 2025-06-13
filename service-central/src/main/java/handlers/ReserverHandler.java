package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import rmi.Serveur;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.logging.Level;
/**
 * ReserverHandler mis à jour pour utiliser ServiceCentralImpl
 */
public class ReserverHandler implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(ReserverHandler.class.getName());
    private final Serveur serviceCentral;

    public ReserverHandler(Serveur serviceCentral) {
        this.serviceCentral = serviceCentral;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        LOGGER.info("Requête reçue : " + exchange.getRequestMethod() + " /reserver");

        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Méthode non autorisée. Utilisez POST.");
                return;
            }

            String requestBody = readRequestBody(exchange);
            LOGGER.info("Body reçu : " + requestBody);

            // Valider le JSON
            try {
                org.json.JSONObject jsonRequest = new org.json.JSONObject(requestBody);

                if (!jsonRequest.has("tableId") || !jsonRequest.has("nomClient") ||
                        !jsonRequest.has("prenomClient") || !jsonRequest.has("telephone") ||
                        !jsonRequest.has("nbConvives") || !jsonRequest.has("dateReservation")) {

                    sendError(exchange, 400, "Données manquantes. Champs requis : tableId, nomClient, prenomClient, telephone, nbConvives, dateReservation");
                    return;
                }

            } catch (Exception e) {
                sendError(exchange, 400, "JSON invalide : " + e.getMessage());
                return;
            }

            String jsonResponse = serviceCentral.reserverTable(requestBody);

            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }

            LOGGER.info("Réservation traitée avec succès");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du traitement de la requête", e);
            sendError(exchange, 500, "Erreur serveur : " + e.getMessage());
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

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