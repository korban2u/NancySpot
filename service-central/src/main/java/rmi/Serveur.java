package rmi;

import interfaces.ServiceBD;
import interfaces.ServiceProxy;
import interfaces.ServiceCentral;
import org.json.JSONObject;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.logging.Logger;
import java.util.logging.Level;


public class Serveur implements ServiceCentral {

    private static final Logger LOGGER = Logger.getLogger(Serveur.class.getName());

    private ServiceBD serviceBD = null;
    private ServiceProxy serviceProxy = null;


    public Serveur() {
        LOGGER.info("Service Central créé (pas encore exporté) - Centre de la topologie");
    }

    @Override
    public boolean enregistrerServiceBD(ServiceBD serviceBD) throws RemoteException {
        try {
            serviceBD.ping();

            this.serviceBD = serviceBD;
            String serviceBDHost = RemoteServer.getClientHost();

            LOGGER.info("Service BD inscrit : " + serviceBDHost);
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur inscription Service BD", e);
            return false;
        }
    }

    @Override
    public boolean enregistrerServiceProxy(ServiceProxy serviceProxy) throws RemoteException {
        try {
            serviceProxy.ping();

            this.serviceProxy = serviceProxy;
            String serviceProxyHost = RemoteServer.getClientHost();

            LOGGER.info("Service Proxy inscrit : " + serviceProxyHost );
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
                    LOGGER.info("Service BD désinscrit");
                    return true;

                case "PROXY":
                    serviceProxy = null;
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
                .put("disponible", bdDisponible));

        etat.put("serviceProxy", new JSONObject()
                .put("disponible", proxyDisponible));

        etat.put("timestamp", System.currentTimeMillis());

        return etat.toString();
    }

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