package utils;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Utilitaires pour la gestion des requêtes et réponses HTTP.
 *
 * Cette classe fournit des méthodes helper pour simplifier la gestion
 * des échanges HTTP dans les handlers du service central. Elle standardise
 * le format des réponses, la gestion des erreurs et la validation des données.
 *
 * Fonctionnalités :
 * - Validation des méthodes HTTP
 * - Formatage standardisé des réponses JSON
 * - Gestion centralisée des erreurs
 * - Lecture du corps des requêtes
 * - Validation des données JSON
 *
 * @author Nancy Spot Team
 * @version 1.0
 * @since 1.0
 */
public class HttpUtils {

    private static final Logger LOGGER = Logger.getLogger(HttpUtils.class.getName());

    /**
     * Vérifie si la méthode HTTP de la requête correspond à celle attendue.
     *
     * @param exchange l'échange HTTP en cours
     * @param expectedMethod la méthode HTTP attendue (GET, POST, etc.)
     * @return true si la méthode correspond, false sinon
     * @throws IOException en cas d'erreur lors de l'envoi de la réponse d'erreur
     */
    public static boolean checkMethod(HttpExchange exchange, String expectedMethod) throws IOException {
        if (!expectedMethod.equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Méthode non autorisée. Utilisez " + expectedMethod + ".");
            return false;
        }
        return true;
    }

    /**
     * Envoie une réponse JSON avec le code de statut 200.
     * Configure automatiquement les en-têtes appropriés et l'encodage UTF-8.
     *
     * @param exchange l'échange HTTP en cours
     * @param jsonResponse la réponse JSON à envoyer
     * @throws IOException en cas d'erreur lors de l'envoi de la réponse
     */
    public static void sendJsonResponse(HttpExchange exchange, String jsonResponse) throws IOException {
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    /**
     * Envoie une réponse d'erreur formatée en JSON.
     * Crée automatiquement un objet JSON contenant le message d'erreur
     * et un timestamp.
     *
     * @param exchange l'échange HTTP en cours
     * @param code le code de statut HTTP de l'erreur
     * @param message le message d'erreur à inclure
     * @throws IOException en cas d'erreur lors de l'envoi de la réponse
     */
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

    /**
     * Lit le corps d'une requête HTTP et le retourne sous forme de chaîne.
     * Utilise l'encodage UTF-8 pour la conversion.
     *
     * @param exchange l'échange HTTP en cours
     * @return le contenu du corps de la requête
     * @throws IOException en cas d'erreur lors de la lecture
     */
    public static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Gère une requête GET de manière standardisée.
     * Vérifie la méthode HTTP, exécute le service demandé et retourne la réponse.
     *
     * @param exchange l'échange HTTP en cours
     * @param endpoint le nom de l'endpoint pour les logs
     * @param serviceCall l'interface fonctionnelle à exécuter pour traiter la requête
     */
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

    /**
     * Gère une requête POST de manière standardisée avec validation.
     * Vérifie la méthode HTTP, lit et valide le corps de la requête,
     * puis exécute le service demandé.
     *
     * @param exchange l'échange HTTP en cours
     * @param endpoint le nom de l'endpoint pour les logs
     * @param serviceCall l'interface fonctionnelle à exécuter pour traiter la requête
     * @param validator le validateur pour le contenu JSON (peut être null)
     */
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

    /**
     * Interface fonctionnelle pour les appels de service sans paramètre.
     * Utilisée pour les requêtes GET qui ne nécessitent pas de données d'entrée.
     */
    @FunctionalInterface
    public interface ServiceCall {
        /**
         * Exécute l'appel de service.
         *
         * @return la réponse JSON du service
         * @throws Exception en cas d'erreur lors de l'exécution
         */
        String execute() throws Exception;
    }

    /**
     * Interface fonctionnelle pour les appels de service avec paramètre.
     * Utilisée pour les requêtes POST qui nécessitent des données d'entrée.
     */
    @FunctionalInterface
    public interface PostServiceCall {
        /**
         * Exécute l'appel de service avec les données de la requête.
         *
         * @param requestBody le corps de la requête HTTP
         * @return la réponse JSON du service
         * @throws Exception en cas d'erreur lors de l'exécution
         */
        String execute(String requestBody) throws Exception;
    }

    /**
     * Interface pour la validation des données JSON.
     * Permet de valider le contenu des requêtes avant traitement.
     */
    public interface JsonValidator {
        /**
         * Valide le contenu JSON.
         *
         * @param json le contenu JSON à valider
         * @return true si le JSON est valide, false sinon
         */
        boolean validate(String json);

        /**
         * Retourne le message d'erreur en cas de validation échouée.
         *
         * @return le message d'erreur de validation
         */
        String getErrorMessage();
    }

    /**
     * Validateur spécialisé pour les données de réservation.
     * Vérifie la présence de tous les champs obligatoires pour une réservation.
     */
    public static class ReservationValidator implements JsonValidator {
        private String errorMessage;

        /**
         * Valide les données de réservation.
         * Vérifie la présence des champs : tableId, nomClient, prenomClient,
         * telephone, nbConvives, dateReservation.
         *
         * @param json le JSON de réservation à valider
         * @return true si toutes les données obligatoires sont présentes
         */
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

        /**
         * Retourne le message d'erreur de validation.
         *
         * @return le message d'erreur ou null si aucune erreur
         */
        @Override
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}