package clients;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Client HTTP de base avec support du proxy réseau.
 * Classe abstraite fournissant les fonctionnalités communes pour l'accès aux APIs externes.
 */
public abstract class BaseHttpClient {

    protected static final Logger LOGGER = Logger.getLogger(BaseHttpClient.class.getName());

    protected final HttpClient httpClient;
    protected final boolean useProxy;
    private final String proxyHost;
    private final String proxyPort;

    /**
     * Constructeur avec configuration complète du proxy.
     *
     * @param useProxy indique si le proxy doit être utilisé
     * @param proxyHost adresse du proxy
     * @param proxyPort port du proxy
     */
    public BaseHttpClient(boolean useProxy, String proxyHost, String proxyPort) {
        this.useProxy = useProxy;
        this.proxyHost = proxyHost != null ? proxyHost : "proxy.infra.univ-lorraine.fr";
        this.proxyPort = proxyPort != null ? proxyPort : "3128";

        if (useProxy) {
            configureProxy();
        }

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Constructeur simplifié avec proxy par défaut.
     *
     * @param useProxy indique si le proxy doit être utilisé
     */
    public BaseHttpClient(boolean useProxy) {
        this(useProxy, null, null);
    }

    /**
     * Configure les propriétés système pour le proxy.
     */
    private void configureProxy() {
        System.setProperty("http.proxyHost", this.proxyHost);
        System.setProperty("http.proxyPort", this.proxyPort);
        System.setProperty("https.proxyHost", this.proxyHost);
        System.setProperty("https.proxyPort", this.proxyPort);
        LOGGER.info("Proxy configuré : " + this.proxyHost + ":" + this.proxyPort);
    }

    /**
     * Récupère des données JSON depuis une URL avec timeout par défaut.
     *
     * @param url URL à interroger
     * @return objet JSON de la réponse
     * @throws Exception en cas d'erreur de réseau ou de parsing
     */
    protected JSONObject fetchJsonData(String url) throws Exception {
        return fetchJsonData(url, 10);
    }

    /**
     * Récupère des données JSON depuis une URL avec timeout personnalisé.
     *
     * @param url URL à interroger
     * @param timeoutSeconds timeout en secondes
     * @return objet JSON de la réponse
     * @throws Exception en cas d'erreur de réseau ou de parsing
     */
    protected JSONObject fetchJsonData(String url, int timeoutSeconds) throws Exception {
        try {
            LOGGER.info("Appel API : " + url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Accept", "application/json")
                    .header("User-Agent", "ServiceProxy-Nancy/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Code HTTP " + response.statusCode() + " : " + response.body());
            }

            String responseBody = response.body();
            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new RuntimeException("Réponse vide du serveur");
            }

            return new JSONObject(responseBody);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur appel API " + url, e);
            throw e;
        }
    }

    /**
     * Crée une réponse d'erreur standardisée.
     *
     * @param message message d'erreur principal
     * @param e exception source
     * @return JSON d'erreur formaté
     */
    protected String createErrorResponse(String message, Exception e) {
        JSONObject error = new JSONObject();
        error.put("error", true);
        error.put("message", message + ": " + e.getMessage());
        error.put("timestamp", System.currentTimeMillis());
        return error.toString();
    }

    /**
     * Crée une réponse vide standardisée.
     *
     * @param dataType type de données (ex: "incidents")
     * @param source source des données
     * @return JSON de réponse vide formaté
     */
    protected String createEmptyResponse(String dataType, String source) {
        JSONObject response = new JSONObject();
        response.put(dataType, new org.json.JSONArray());
        response.put("nombre" + capitalize(dataType), 0);
        response.put("timestamp", System.currentTimeMillis());
        response.put("source", source + " (aucune donnée)");
        return response.toString();
    }

    /**
     * Retourne la date/heure actuelle formatée.
     *
     * @return date/heure au format yyyy-MM-dd HH:mm:ss
     */
    protected String getCurrentDateTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
    }

    /**
     * Formate une date ISO en format lisible.
     *
     * @param isoDate date au format ISO
     * @return date formatée ou date actuelle si erreur
     */
    protected String formatDate(String isoDate) {
        try {
            if (isoDate == null || isoDate.trim().isEmpty()) {
                return getCurrentDateTime();
            }

            java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(isoDate);
            java.time.format.DateTimeFormatter formatter =
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            return dateTime.format(formatter);
        } catch (Exception e) {
            LOGGER.warning("Erreur formatage date: " + isoDate);
            return getCurrentDateTime();
        }
    }

    /**
     * Met en forme une chaîne avec la première lettre en majuscule.
     *
     * @param str chaîne à formatter
     * @return chaîne avec première lettre en majuscule
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Récupère les données spécifiques du service.
     * Méthode abstraite à implémenter dans les classes filles.
     *
     * @return données du service au format JSON
     */
    public abstract String getData();

    /**
     * Retourne le nom du service.
     * Méthode abstraite à implémenter dans les classes filles.
     *
     * @return nom du service
     */
    protected abstract String getServiceName();

    /**
     * Retourne les URLs du service.
     * Méthode abstraite à implémenter dans les classes filles.
     *
     * @return tableau des URLs utilisées par le service
     */
    protected abstract String[] getServiceUrls();
}