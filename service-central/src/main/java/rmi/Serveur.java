package rmi;

import interfaces.ServiceBD;
import interfaces.ServiceProxy;
import interfaces.ServiceCentral;
import org.json.JSONObject;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Implémentation du service central - Centre de la topologie en étoile
 * Tous les autres services s'inscrivent auprès de lui
 */
public class Serveur extends UnicastRemoteObject implements ServiceCentral {

    private static final Logger LOGGER = Logger.getLogger(Serveur.class.getName());
    private static final long serialVersionUID = 1L;

    // Services inscrits
    private ServiceBD serviceBD = null;
    private ServiceProxy serviceProxy = null;

    // Informations des services
    private String serviceBDHost = null;
    private int serviceBDPort = -1;
    private String serviceProxyHost = null;
    private int serviceProxyPort = -1;

    public Serveur() throws RemoteException {
        super();
        LOGGER.info("Service Central démarré - Centre de la topologie");
    }

    @Override
    public boolean enregistrerServiceBD(ServiceBD serviceBD, String host, int port) throws RemoteException {
        try {
            // Tester la connectivité
            serviceBD.ping();

            this.serviceBD = serviceBD;
            this.serviceBDHost = host;
            this.serviceBDPort = port;

            LOGGER.info("Service BD inscrit : " + host + ":" + port);
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur inscription Service BD", e);
            return false;
        }
    }

    @Override
    public boolean enregistrerServiceProxy(ServiceProxy serviceProxy, String host, int port) throws RemoteException {
        try {
            // Tester la connectivité
            serviceProxy.ping();

            this.serviceProxy = serviceProxy;
            this.serviceProxyHost = host;
            this.serviceProxyPort = port;

            LOGGER.info("Service Proxy inscrit : " + host + ":" + port);
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur inscription Service Proxy", e);
            return false;
        }
    }

    @Override
    public boolean supprimerService(String serviceType) throws RemoteException {
        try {
            switch (serviceType.toUpperCase()) {
                case "BD":
                    serviceBD = null;
                    serviceBDHost = null;
                    serviceBDPort = -1;
                    LOGGER.info("Service BD désinscrit");
                    return true;

                case "PROXY":
                    serviceProxy = null;
                    serviceProxyHost = null;
                    serviceProxyPort = -1;
                    LOGGER.info("Service Proxy désinscrit");
                    return true;

                default:
                    LOGGER.warning("Type de service inconnu : " + serviceType);
                    return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur désinscription", e);
            return false;
        }
    }

    @Override
    public String getEtatServices() throws RemoteException {
        JSONObject etat = new JSONObject();

        // Vérifier Service BD
        boolean bdDisponible = false;
        if (serviceBD != null) {
            try {
                serviceBD.ping();
                bdDisponible = true;
            } catch (Exception e) {
                LOGGER.warning("Service BD non disponible");
                serviceBD = null;
            }
        }

        // Vérifier Service Proxy
        boolean proxyDisponible = false;
        if (serviceProxy != null) {
            try {
                serviceProxy.ping();
                proxyDisponible = true;
            } catch (Exception e) {
                LOGGER.warning("Service Proxy non disponible");
                serviceProxy = null;
            }
        }

        etat.put("serviceBD", new JSONObject()
                .put("disponible", bdDisponible)
                .put("host", serviceBDHost)
                .put("port", serviceBDPort));

        etat.put("serviceProxy", new JSONObject()
                .put("disponible", proxyDisponible)
                .put("host", serviceProxyHost)
                .put("port", serviceProxyPort));

        etat.put("timestamp", System.currentTimeMillis());

        return etat.toString();
    }

    // Méthodes pour accéder aux services (utilisées par HttpServerCentral)
    public String getAllRestaurants() throws RemoteException {
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.getAllRestaurants();
    }

    public String getTablesLibres(int restaurantId) throws RemoteException {
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.getTablesLibres(restaurantId);
    }

    public String reserverTable(String jsonReservation) throws RemoteException {
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.reserverTable(jsonReservation);
    }

    public String getVelibData() throws RemoteException {
        if (serviceProxy == null) {
            throw new RemoteException("Service Proxy non disponible");
        }
        return serviceProxy.getVelibData();
    }

    public String getIncidents() throws RemoteException {
        if (serviceProxy == null) {
            throw new RemoteException("Service Proxy non disponible");
        }
        return serviceProxy.getIncidents();
    }
}