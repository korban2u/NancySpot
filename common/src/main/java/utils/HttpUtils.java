package utils;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.logging.Level;


public class HttpUtils {

    private static final Logger LOGGER = Logger.getLogger(HttpUtils.class.getName());


    public static boolean checkMethod(HttpExchange exchange, String expectedMethod) throws IOException {
        if (!expectedMethod.equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Méthode non autorisée. Utilisez " + expectedMethod + ".");
            return false;
        }
        return true;
    }


    public static void sendJsonResponse(HttpExchange exchange, String jsonResponse) throws IOException {
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }


    public static void sendError(HttpExchange exchange, int code, String message) throws IOException {
        JSONObject error = new JSONObject();
        error.put("error", true);
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());

        String jsonError = error.toString();
        byte[] errorBytes = jsonError.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(code, errorBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(errorBytes);
        }
    }


    public static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }


    public static void handleGetRequest(HttpExchange exchange, String endpoint, ServiceCall serviceCall) {
        LOGGER.info("Requête reçue : " + exchange.getRequestMethod() + " " + endpoint);

        try {
            if (!checkMethod(exchange, "GET")) {
                return;
            }

            String jsonResponse = serviceCall.execute();
            sendJsonResponse(exchange, jsonResponse);
            LOGGER.info("Réponse envoyée pour " + endpoint);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du traitement de " + endpoint, e);
            try {
                sendError(exchange, 500, "Erreur serveur : " + e.getMessage());
            } catch (IOException ioException) {
                LOGGER.log(Level.SEVERE, "Erreur lors de l'envoi de l'erreur", ioException);
            }
        }
    }


    public static void handlePostRequest(HttpExchange exchange, String endpoint,
                                         PostServiceCall serviceCall, JsonValidator validator) {
        LOGGER.info("Requête reçue : " + exchange.getRequestMethod() + " " + endpoint);

        try {
            if (!checkMethod(exchange, "POST")) {
                return;
            }

            String requestBody = readRequestBody(exchange);
            LOGGER.info("Body reçu : " + requestBody);

            if (validator != null && !validator.validate(requestBody)) {
                sendError(exchange, 400, validator.getErrorMessage());
                return;
            }

            String jsonResponse = serviceCall.execute(requestBody);
            sendJsonResponse(exchange, jsonResponse);
            LOGGER.info("Réponse envoyée pour " + endpoint);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du traitement de " + endpoint, e);
            try {
                sendError(exchange, 500, "Erreur serveur : " + e.getMessage());
            } catch (IOException ioException) {
                LOGGER.log(Level.SEVERE, "Erreur lors de l'envoi de l'erreur", ioException);
            }
        }
    }


    @FunctionalInterface
    public interface ServiceCall {
        String execute() throws Exception;
    }


    @FunctionalInterface
    public interface PostServiceCall {
        String execute(String requestBody) throws Exception;
    }


    public interface JsonValidator {
        boolean validate(String json);
        String getErrorMessage();
    }


    public static class ReservationValidator implements JsonValidator {
        private String errorMessage;

        @Override
        public boolean validate(String json) {
            try {
                JSONObject jsonRequest = new JSONObject(json);

                if (!jsonRequest.has("tableId") || !jsonRequest.has("nomClient") ||
                        !jsonRequest.has("prenomClient") || !jsonRequest.has("telephone") ||
                        !jsonRequest.has("nbConvives") || !jsonRequest.has("dateReservation")) {

                    errorMessage = "Données manquantes. Champs requis : tableId, nomClient, prenomClient, telephone, nbConvives, dateReservation";
                    return false;
                }

                return true;
            } catch (Exception e) {
                errorMessage = "JSON invalide : " + e.getMessage();
                return false;
            }
        }

        @Override
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}