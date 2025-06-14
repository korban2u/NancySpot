package rmi;

import interfaces.ServiceProxy;
import interfaces.ServiceCentral;

import clients.IncidentsClient;
import org.json.JSONObject;

import java.rmi.RemoteException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Implémentation du service Proxy RMI.
 * Service responsable de l'accès aux APIs externes avec gestion du proxy réseau.
 */
public class Proxy implements ServiceProxy {

    private static final Logger LOGGER = Logger.getLogger(Proxy.class.getName());

    private final IncidentsClient incidentsClient;

    /**
     * Constructeur du service Proxy.
     *
     * @param useProxy indique si le proxy réseau doit être utilisé
     * @param proxyHost adresse du proxy réseau
     * @param proxyPort port du proxy réseau
     */
    public Proxy(boolean useProxy, String proxyHost, String proxyPort) {
        this.incidentsClient = new IncidentsClient(useProxy, proxyHost, proxyPort);
        LOGGER.info("ServiceProxy créé avec proxy: " + useProxy +
                (useProxy ? " (" + proxyHost + ":" + proxyPort + ")" : ""));
    }

    /**
     * Récupère les incidents de circulation au format JSON.
     *
     * Format de réponse en cas de succès :
     * {
     *   "incidents": [
     *     {
     *       "id": "string",
     *       "type": "travaux|accident|manifestation|embouteillage|deviation|incident",
     *       "titre": "string",
     *       "description": "string",
     *       "latitude": number,
     *       "longitude": number,
     *       "impact": "fort|moyen|faible",
     *       "dateDebut": "yyyy-MM-dd HH:mm:ss",
     *       "dateFin": "yyyy-MM-dd HH:mm:ss",
     *       "rue": "string (optionnel)",
     *       "lieu": "string (optionnel)"
     *     }
     *   ],
     *   "nombreIncidents": number,
     *   "timestamp": number,
     *   "source": "Métropole du Grand Nancy"
     * }
     *
     * Format en cas d'erreur :
     * {
     *   "error": true,
     *   "message": "string"
     * }
     *
     * @return JSON contenant les incidents ou une erreur
     * @throws RemoteException en cas d'erreur RMI
     */
    @Override
    public String getIncidents() throws RemoteException {
        LOGGER.info("Appel getIncidents()");

        try {
            return incidentsClient.getIncidentsReels();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur getIncidents", e);

            JSONObject error = new JSONObject();
            error.put("error", true);
            error.put("message", "Erreur lors de la récupération des incidents: " + e.getMessage());
            return error.toString();
        }
    }

    @Override
    public boolean ping() throws RemoteException {
        return true;
    }
}