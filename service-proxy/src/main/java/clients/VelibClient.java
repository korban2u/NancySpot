package clients;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.logging.Level;

public class VelibClient extends BaseHttpClient {

    private static final String STATION_INFO_URL = "https://api.cyclocity.fr/contracts/nancy/gbfs/station_information.json";
    private static final String STATION_STATUS_URL = "https://api.cyclocity.fr/contracts/nancy/gbfs/station_status.json";

    // ✅ CORRECTION : Accepter la configuration du proxy
    public VelibClient(boolean useProxy, String proxyHost, String proxyPort) {
        super(useProxy, proxyHost, proxyPort);
        LOGGER.info("VelibClient initialisé (proxy: " + useProxy +
                (useProxy ? " via " + proxyHost + ":" + proxyPort : "") + ")");
    }

    // ✅ Méthode de compatibilité pour l'ancien constructeur
    public VelibClient(boolean useProxy) {
        super(useProxy);
        LOGGER.info("VelibClient initialisé (proxy: " + useProxy + ")");
    }

    @Override
    public String getData() {
        return getVelibData();
    }

    @Override
    protected String getServiceName() {
        return "Vélib Nancy";
    }

    @Override
    protected String[] getServiceUrls() {
        return new String[]{STATION_INFO_URL, STATION_STATUS_URL};
    }

    public String getVelibData() {
        try {
            LOGGER.info("Récupération des données " + getServiceName() + "...");

            // Récupérer les deux sources de données
            JSONObject stationInfo = fetchJsonData(STATION_INFO_URL);
            JSONObject stationStatus = fetchJsonData(STATION_STATUS_URL);

            // Extraire et traiter les données
            JSONArray stations = stationInfo.getJSONObject("data").getJSONArray("stations");
            JSONArray statuses = stationStatus.getJSONObject("data").getJSONArray("stations");

            // Créer l'index des statuts pour accès rapide
            JSONObject statusMap = createStatusMap(statuses);

            // Construire la réponse simplifiée
            JSONArray simplifiedStations = processStations(stations, statusMap);

            // Construire la réponse finale
            return buildVelibResponse(simplifiedStations);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des données " + getServiceName(), e);
            return createErrorResponse("Impossible de récupérer les données " + getServiceName(), e);
        }
    }

    private JSONObject createStatusMap(JSONArray statuses) {
        JSONObject statusMap = new JSONObject();
        for (int i = 0; i < statuses.length(); i++) {
            JSONObject status = statuses.getJSONObject(i);
            statusMap.put(status.getString("station_id"), status);
        }
        return statusMap;
    }

    private JSONArray processStations(JSONArray stations, JSONObject statusMap) {
        JSONArray simplifiedStations = new JSONArray();

        for (int i = 0; i < stations.length(); i++) {
            JSONObject station = stations.getJSONObject(i);
            String stationId = station.getString("station_id");

            JSONObject status = statusMap.optJSONObject(stationId);
            if (status != null) {
                JSONObject simplified = createSimplifiedStation(station, status);
                simplifiedStations.put(simplified);
            }
        }

        return simplifiedStations;
    }

    private JSONObject createSimplifiedStation(JSONObject station, JSONObject status) {
        JSONObject simplified = new JSONObject();

        simplified.put("id", station.getString("station_id"));
        simplified.put("nom", station.getString("name"));
        simplified.put("latitude", station.getDouble("lat"));
        simplified.put("longitude", station.getDouble("lon"));
        simplified.put("velosDisponibles", status.getInt("num_bikes_available"));
        simplified.put("placesDisponibles", status.getInt("num_docks_available"));

        boolean isInstalled = status.optBoolean("is_installed", true);
        simplified.put("estOuverte", isInstalled);

        return simplified;
    }

    private String buildVelibResponse(JSONArray stations) {
        JSONObject response = new JSONObject();
        response.put("stations", stations);
        response.put("nombreStations", stations.length());
        response.put("timestamp", System.currentTimeMillis());
        response.put("source", "API Vélib Nancy");

        LOGGER.info("Données " + getServiceName() + " récupérées : " + stations.length() + " stations");
        return response.toString();
    }
}