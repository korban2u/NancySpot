package rmi;

import interfaces.ServiceProxy;
import interfaces.ServiceCentral;

import clients.IncidentsClient;
import org.json.JSONObject;

import java.rmi.RemoteException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Proxy implements ServiceProxy {

    private static final Logger LOGGER = Logger.getLogger(Proxy.class.getName());

    private final IncidentsClient incidentsClient;


    public Proxy(boolean useProxy, String proxyHost, String proxyPort) {
        this.incidentsClient = new IncidentsClient(useProxy, proxyHost, proxyPort);
        LOGGER.info("ServiceProxy créé avec proxy: " + useProxy +
                (useProxy ? " (" + proxyHost + ":" + proxyPort + ")" : ""));
    }


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