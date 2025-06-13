package clients;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.logging.Level;

public class IncidentsClient extends BaseHttpClient {

    private static final String INCIDENTS_URL = "https://carto.g-ny.org/data/cifs/cifs_waze_v2.json";

    // ✅ CORRECTION : Accepter la configuration du proxy
    public IncidentsClient(boolean useProxy, String proxyHost, String proxyPort) {
        super(useProxy, proxyHost, proxyPort);
        LOGGER.info("IncidentsClient initialisé (proxy: " + useProxy +
                (useProxy ? " via " + proxyHost + ":" + proxyPort : "") + ")");
    }

    // ✅ Méthode de compatibilité pour l'ancien constructeur
    public IncidentsClient(boolean useProxy) {
        super(useProxy);
        LOGGER.info("IncidentsClient initialisé (proxy: " + useProxy + ")");
    }

    @Override
    public String getData() {
        return getIncidentsReels();
    }

    @Override
    protected String getServiceName() {
        return "Incidents Grand Nancy";
    }

    @Override
    protected String[] getServiceUrls() {
        return new String[]{INCIDENTS_URL};
    }

    public String getIncidentsReels() {
        try {
            LOGGER.info("Récupération des données " + getServiceName() + "...");

            JSONObject incidentsData = fetchJsonData(INCIDENTS_URL, 15);

            if (!incidentsData.has("incidents")) {
                LOGGER.warning("Aucune clé 'incidents' trouvée dans les données");
                return createEmptyResponse("incidents", "Métropole du Grand Nancy");
            }

            JSONArray incidentsArray = incidentsData.getJSONArray("incidents");
            JSONArray incidentsSimplifies = processIncidents(incidentsArray);

            return buildIncidentsResponse(incidentsSimplifies);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des données " + getServiceName(), e);
            return createErrorResponse("Impossible de récupérer les incidents RÉELS", e);
        }
    }

    private JSONArray processIncidents(JSONArray incidentsArray) {
        JSONArray incidentsSimplifies = new JSONArray();

        for (int i = 0; i < incidentsArray.length(); i++) {
            try {
                JSONObject incident = incidentsArray.getJSONObject(i);
                JSONObject incidentSimplifie = createSimplifiedIncident(incident, i);

                if (incidentSimplifie != null) {
                    incidentsSimplifies.put(incidentSimplifie);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Erreur traitement incident " + i, e);
            }
        }

        return incidentsSimplifies;
    }

    private JSONObject createSimplifiedIncident(JSONObject incident, int index) {
        JSONObject location = incident.getJSONObject("location");

        String polyline = location.getString("polyline");
        String[] coords = polyline.split(" ");

        if (coords.length < 2) {
            LOGGER.warning("Coordonnées invalides pour incident " + index + ": " + polyline);
            return null;
        }

        double latitude = Double.parseDouble(coords[0]);
        double longitude = Double.parseDouble(coords[1]);

        JSONObject incidentSimplifie = new JSONObject();

        incidentSimplifie.put("id", incident.optString("id", "INC_" + index));
        incidentSimplifie.put("type", determinerTypeIncident(incident));
        incidentSimplifie.put("titre", incident.optString("short_description", "Incident"));
        incidentSimplifie.put("description", incident.optString("description", "Information non disponible"));
        incidentSimplifie.put("latitude", latitude);
        incidentSimplifie.put("longitude", longitude);
        incidentSimplifie.put("impact", determinerImpact(incident));

        String dateDebut = incident.optString("starttime", getCurrentDateTime());
        String dateFin = incident.optString("endtime", getCurrentDateTime());
        incidentSimplifie.put("dateDebut", formatDate(dateDebut));
        incidentSimplifie.put("dateFin", formatDate(dateFin));

        if (location.has("street")) {
            incidentSimplifie.put("rue", location.getString("street"));
        }
        if (location.has("location_description")) {
            incidentSimplifie.put("lieu", location.getString("location_description"));
        }

        return incidentSimplifie;
    }

    private String determinerTypeIncident(JSONObject incident) {
        String type = incident.optString("type", "").toLowerCase();
        String description = incident.optString("description", "").toLowerCase();
        String shortDesc = incident.optString("short_description", "").toLowerCase();

        if (type.contains("construction") || description.contains("chantier") ||
                description.contains("travaux") || shortDesc.contains("travaux")) {
            return "travaux";
        } else if (description.contains("accident")) {
            return "accident";
        } else if (description.contains("manifestation") || description.contains("cortège")) {
            return "manifestation";
        } else if (description.contains("circulation") && description.contains("dense")) {
            return "embouteillage";
        } else if (description.contains("déviation") || description.contains("deviation")) {
            return "deviation";
        } else {
            return "incident";
        }
    }

    private String determinerImpact(JSONObject incident) {
        String description = incident.optString("description", "").toLowerCase();

        if (description.contains("rue barrée") || description.contains("circulation interdit") ||
                description.contains("accident")) {
            return "fort";
        } else if (description.contains("gêne à la circulation") || description.contains("réduction") ||
                description.contains("stationnement interdit")) {
            return "moyen";
        } else {
            return "faible";
        }
    }

    private String buildIncidentsResponse(JSONArray incidents) {
        JSONObject response = new JSONObject();
        response.put("incidents", incidents);
        response.put("nombreIncidents", incidents.length());
        response.put("timestamp", System.currentTimeMillis());
        response.put("source", "Métropole du Grand Nancy");

        LOGGER.info("Données " + getServiceName() + " récupérées : " + incidents.length() + " incidents");
        return response.toString();
    }
}