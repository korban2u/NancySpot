package rmi;

import interfaces.ServiceBD;
import interfaces.ServiceProxy;
import interfaces.ServiceCentral;
import org.json.JSONObject;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Service Central amélioré avec support des créneaux horaires
 * Fait le pont entre les clients HTTP et les services RMI
 *
 * @author Nancy Spot Team
 * @version 2.0 - Avec gestion des créneaux
 */
public class Serveur implements ServiceCentral {

    private static final Logger LOGGER = Logger.getLogger(Serveur.class.getName());

    private ServiceBD serviceBD = null;
    private ServiceProxy serviceProxy = null;

    public Serveur() {
        LOGGER.info("Service Central créé avec support des créneaux - Centre de la topologie");
    }

    // ==================== GESTION DES SERVICES RMI ====================

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
            LOGGER.info("Service Proxy inscrit : " + serviceProxyHost);
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

        etat.put("serviceBD", new JSONObject().put("disponible", bdDisponible));
        etat.put("serviceProxy", new JSONObject().put("disponible", proxyDisponible));
        etat.put("timestamp", System.currentTimeMillis());

        return etat.toString();
    }

    // ==================== MÉTHODES RESTAURANTS ====================

    public String getAllRestaurants() throws RemoteException {
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.getAllRestaurants();
    }

    // ==================== MÉTHODES CRÉNEAUX ====================

    /**
     * Récupère la liste des créneaux disponibles
     */
    public String getCreneauxDisponibles() throws RemoteException {
        LOGGER.info("Appel getCreneauxDisponibles()");
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.getCreneauxDisponibles();
    }

    /**
     * Récupère un créneau spécifique par son ID
     */
    public String getCreneauById(int creneauId) throws RemoteException {
        LOGGER.info("Appel getCreneauById(" + creneauId + ")");
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.getCreneauById(creneauId);
    }

    // ==================== MÉTHODES TABLES ====================

    /**
     * Méthode dépréciée - utiliser getTablesLibresPourCreneau
     */
    @Deprecated
    public String getTablesLibres(int restaurantId) throws RemoteException {
        LOGGER.warning("Appel méthode dépréciée getTablesLibres() - utiliser getTablesLibresPourCreneau()");
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.getTablesLibres(restaurantId);
    }

    /**
     * Récupère les tables libres pour un restaurant, une date et un créneau
     */
    public String getTablesLibresPourCreneau(int restaurantId, String dateReservation, int creneauId) throws RemoteException {
        LOGGER.info("Appel getTablesLibresPourCreneau(" + restaurantId + ", " + dateReservation + ", " + creneauId + ")");
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.getTablesLibresPourCreneau(restaurantId, dateReservation, creneauId);
    }

    /**
     * Récupère toutes les tables avec leur statut pour une date et un créneau
     */
    public String getTablesAvecStatut(int restaurantId, String dateReservation, int creneauId) throws RemoteException {
        LOGGER.info("Appel getTablesAvecStatut(" + restaurantId + ", " + dateReservation + ", " + creneauId + ")");
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.getTablesAvecStatut(restaurantId, dateReservation, creneauId);
    }

    /**
     * Vérifie la disponibilité d'une table pour un créneau et une date
     */
    public String verifierDisponibilite(int tableId, String dateReservation, int creneauId) throws RemoteException {
        LOGGER.info("Appel verifierDisponibilite(" + tableId + ", " + dateReservation + ", " + creneauId + ")");
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.verifierDisponibilite(tableId, dateReservation, creneauId);
    }

    // ==================== MÉTHODES RÉSERVATIONS ====================

    /**
     * Effectue une réservation de table avec support des créneaux
     */
    public String reserverTable(String jsonReservation) throws RemoteException {
        LOGGER.info("Appel reserverTable() avec créneaux");
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.reserverTable(jsonReservation);
    }

    /**
     * Récupère les réservations d'un restaurant pour une date donnée
     */
    public String getReservationsPourDate(int restaurantId, String dateReservation) throws RemoteException {
        LOGGER.info("Appel getReservationsPourDate(" + restaurantId + ", " + dateReservation + ")");
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.getReservationsPourDate(restaurantId, dateReservation);
    }

    /**
     * Annule une réservation existante
     */
    public String annulerReservation(int reservationId) throws RemoteException {
        LOGGER.info("Appel annulerReservation(" + reservationId + ")");
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.annulerReservation(reservationId);
    }

    // ==================== MÉTHODES STATISTIQUES (FUTURES) ====================

    /**
     * Récupère les statistiques de réservation pour un restaurant
     */
    public String getStatistiquesReservations(int restaurantId, String dateDebut, String dateFin) throws RemoteException {
        LOGGER.info("Appel getStatistiquesReservations(" + restaurantId + ", " + dateDebut + ", " + dateFin + ")");
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.getStatistiquesReservations(restaurantId, dateDebut, dateFin);
    }

    /**
     * Récupère le planning complet d'un restaurant
     */
    public String getPlanningRestaurant(int restaurantId, String dateDebut, String dateFin) throws RemoteException {
        LOGGER.info("Appel getPlanningRestaurant(" + restaurantId + ", " + dateDebut + ", " + dateFin + ")");
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.getPlanningRestaurant(restaurantId, dateDebut, dateFin);
    }

    // ==================== MÉTHODES PROXY (INCIDENTS) ====================

    /**
     * Récupère les incidents de circulation
     */
    public String getIncidents() throws RemoteException {
        if (serviceProxy == null) {
            throw new RemoteException("Service Proxy non disponible");
        }
        return serviceProxy.getIncidents();
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Valide les paramètres pour les endpoints avec créneaux
     */
    private boolean validateCreneauxParams(int restaurantId, String dateReservation, int creneauId) {
        if (restaurantId <= 0) {
            LOGGER.warning("ID restaurant invalide: " + restaurantId);
            return false;
        }

        if (creneauId <= 0) {
            LOGGER.warning("ID créneau invalide: " + creneauId);
            return false;
        }

        if (dateReservation == null || dateReservation.trim().isEmpty()) {
            LOGGER.warning("Date de réservation invalide: " + dateReservation);
            return false;
        }

        // Validation format date yyyy-MM-dd
        if (!dateReservation.matches("\\d{4}-\\d{2}-\\d{2}")) {
            LOGGER.warning("Format de date invalide: " + dateReservation);
            return false;
        }

        return true;
    }

    /**
     * Crée une réponse d'erreur standardisée
     */
    private String createErrorResponse(String message, String details) {
        JSONObject error = new JSONObject();
        error.put("error", true);
        error.put("message", message);
        if (details != null) {
            error.put("details", details);
        }
        error.put("timestamp", System.currentTimeMillis());
        return error.toString();
    }

    /**
     * Log les appels d'API pour le débogage
     */
    private void logApiCall(String method, Object... params) {
        if (LOGGER.isLoggable(Level.INFO)) {
            StringBuilder sb = new StringBuilder();
            sb.append("API Call: ").append(method).append("(");
            for (int i = 0; i < params.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(params[i]);
            }
            sb.append(")");
            LOGGER.info(sb.toString());
        }
    }
}