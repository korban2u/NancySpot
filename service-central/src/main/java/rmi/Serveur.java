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
 * Implémentation du service central avec support des créneaux horaires.
 *
 * Le serveur central fait le pont entre les clients HTTP et les services RMI.
 * Il maintient un registre des services disponibles et route les requêtes
 * vers les services appropriés.
 *
 * Architecture :
 * - Clients HTTP/HTTPS → Service Central → Services RMI (BD, Proxy)
 */
public class Serveur implements ServiceCentral {

    private static final Logger LOGGER = Logger.getLogger(Serveur.class.getName());

    private ServiceBD serviceBD = null;
    private ServiceProxy serviceProxy = null;

    /**
     * Constructeur du serveur central.
     * Initialise le serveur avec support des créneaux horaires.
     */
    public Serveur() {
        LOGGER.info("Service Central créé avec support des créneaux - Centre de la topologie");
    }

    /**
     * Enregistre un service de base de données.
     * Vérifie la connectivité du service avant de l'enregistrer.
     *
     * @param serviceBD le service de base de données à enregistrer
     * @return true si l'enregistrement a réussi, false sinon
     * @throws RemoteException en cas d'erreur de communication RMI
     */
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

    /**
     * Enregistre un service proxy.
     * Vérifie la connectivité du service avant de l'enregistrer.
     *
     * @param serviceProxy le service proxy à enregistrer
     * @return true si l'enregistrement a réussi, false sinon
     * @throws RemoteException en cas d'erreur de communication RMI
     */
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

    /**
     * Supprime un service du registre.
     *
     * @param serviceType le type de service à supprimer ("BD" ou "PROXY")
     * @return true si la suppression a réussi, false si le type est inconnu
     * @throws RemoteException en cas d'erreur de communication RMI
     */
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

    /**
     * Retourne l'état de tous les services enregistrés.
     * Effectue un test de connectivité en temps réel.
     *
     * @return un JSON contenant l'état de disponibilité de chaque service
     * @throws RemoteException en cas d'erreur de communication RMI
     */
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

    /**
     * Récupère la liste de tous les restaurants.
     * Délègue la requête au service de base de données.
     *
     * @return un JSON contenant la liste des restaurants
     * @throws RemoteException si le service BD n'est pas disponible
     */
    public String getAllRestaurants() throws RemoteException {
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.getAllRestaurants();
    }

    /**
     * Récupère la liste des créneaux disponibles.
     *
     * @return un JSON contenant la liste des créneaux horaires actifs
     * @throws RemoteException si le service BD n'est pas disponible
     */
    public String getCreneauxDisponibles() throws RemoteException {
        LOGGER.info("Appel getCreneauxDisponibles()");
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.getCreneauxDisponibles();
    }

    /**
     * Récupère un créneau spécifique par son ID.
     *
     * @param creneauId l'identifiant du créneau
     * @return un JSON contenant les données du créneau
     * @throws RemoteException si le service BD n'est pas disponible
     */
    public String getCreneauById(int creneauId) throws RemoteException {
        LOGGER.info("Appel getCreneauById(" + creneauId + ")");
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.getCreneauById(creneauId);
    }

    /**
     * Récupère les tables libres pour un restaurant (version dépréciée).
     *
     * @param restaurantId l'identifiant du restaurant
     * @return un JSON contenant la liste des tables
     * @throws RemoteException si le service BD n'est pas disponible
     * @deprecated Utiliser getTablesLibresPourCreneau()
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
     * Récupère les tables libres pour un restaurant, une date et un créneau.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param dateReservation la date de réservation
     * @param creneauId l'identifiant du créneau
     * @return un JSON contenant la liste des tables disponibles
     * @throws RemoteException si le service BD n'est pas disponible
     */
    public String getTablesLibresPourCreneau(int restaurantId, String dateReservation, int creneauId) throws RemoteException {
        LOGGER.info("Appel getTablesLibresPourCreneau(" + restaurantId + ", " + dateReservation + ", " + creneauId + ")");
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.getTablesLibresPourCreneau(restaurantId, dateReservation, creneauId);
    }

    /**
     * Récupère toutes les tables avec leur statut pour une date et un créneau.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param dateReservation la date de réservation
     * @param creneauId l'identifiant du créneau
     * @return un JSON contenant toutes les tables avec leur statut
     * @throws RemoteException si le service BD n'est pas disponible
     */
    public String getTablesAvecStatut(int restaurantId, String dateReservation, int creneauId) throws RemoteException {
        LOGGER.info("Appel getTablesAvecStatut(" + restaurantId + ", " + dateReservation + ", " + creneauId + ")");
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.getTablesAvecStatut(restaurantId, dateReservation, creneauId);
    }

    /**
     * Vérifie la disponibilité d'une table pour un créneau et une date.
     *
     * @param tableId l'identifiant de la table
     * @param dateReservation la date de réservation
     * @param creneauId l'identifiant du créneau
     * @return un JSON contenant le statut de disponibilité
     * @throws RemoteException si le service BD n'est pas disponible
     */
    public String verifierDisponibilite(int tableId, String dateReservation, int creneauId) throws RemoteException {
        LOGGER.info("Appel verifierDisponibilite(" + tableId + ", " + dateReservation + ", " + creneauId + ")");
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.verifierDisponibilite(tableId, dateReservation, creneauId);
    }

    /**
     * Effectue une réservation de table avec support des créneaux.
     *
     * @param jsonReservation un JSON contenant les données de réservation
     * @return un JSON contenant le résultat de la réservation
     * @throws RemoteException si le service BD n'est pas disponible
     */
    public String reserverTable(String jsonReservation) throws RemoteException {
        LOGGER.info("Appel reserverTable() avec créneaux");
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.reserverTable(jsonReservation);
    }

    /**
     * Récupère les réservations d'un restaurant pour une date donnée.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param dateReservation la date de réservation
     * @return un JSON contenant la liste des réservations
     * @throws RemoteException si le service BD n'est pas disponible
     */
    public String getReservationsPourDate(int restaurantId, String dateReservation) throws RemoteException {
        LOGGER.info("Appel getReservationsPourDate(" + restaurantId + ", " + dateReservation + ")");
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.getReservationsPourDate(restaurantId, dateReservation);
    }

    /**
     * Annule une réservation existante.
     *
     * @param reservationId l'identifiant de la réservation
     * @return un JSON contenant le résultat de l'annulation
     * @throws RemoteException si le service BD n'est pas disponible
     */
    public String annulerReservation(int reservationId) throws RemoteException {
        LOGGER.info("Appel annulerReservation(" + reservationId + ")");
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.annulerReservation(reservationId);
    }

    /**
     * Récupère les statistiques de réservation pour un restaurant.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param dateDebut la date de début de la période
     * @param dateFin la date de fin de la période
     * @return un JSON contenant les statistiques détaillées
     * @throws RemoteException si le service BD n'est pas disponible
     */
    public String getStatistiquesReservations(int restaurantId, String dateDebut, String dateFin) throws RemoteException {
        LOGGER.info("Appel getStatistiquesReservations(" + restaurantId + ", " + dateDebut + ", " + dateFin + ")");
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.getStatistiquesReservations(restaurantId, dateDebut, dateFin);
    }

    /**
     * Récupère le planning complet d'un restaurant.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param dateDebut la date de début de la période
     * @param dateFin la date de fin de la période
     * @return un JSON contenant le planning détaillé
     * @throws RemoteException si le service BD n'est pas disponible
     */
    public String getPlanningRestaurant(int restaurantId, String dateDebut, String dateFin) throws RemoteException {
        LOGGER.info("Appel getPlanningRestaurant(" + restaurantId + ", " + dateDebut + ", " + dateFin + ")");
        if (serviceBD == null) {
            throw new RemoteException("Service BD non disponible");
        }
        return serviceBD.getPlanningRestaurant(restaurantId, dateDebut, dateFin);
    }

    /**
     * Récupère les incidents de circulation.
     * Délègue la requête au service proxy.
     *
     * @return un JSON contenant la liste des incidents
     * @throws RemoteException si le service Proxy n'est pas disponible
     */
    public String getIncidents() throws RemoteException {
        if (serviceProxy == null) {
            throw new RemoteException("Service Proxy non disponible");
        }
        return serviceProxy.getIncidents();
    }

    /**
     * Valide les paramètres pour les endpoints avec créneaux.
     * Vérifie la cohérence des identifiants et du format de date.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param dateReservation la date de réservation
     * @param creneauId l'identifiant du créneau
     * @return true si tous les paramètres sont valides
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

        if (!dateReservation.matches("\\d{4}-\\d{2}-\\d{2}")) {
            LOGGER.warning("Format de date invalide: " + dateReservation);
            return false;
        }

        return true;
    }

    /**
     * Crée une réponse d'erreur standardisée.
     *
     * @param message le message d'erreur principal
     * @param details les détails additionnels (peut être null)
     * @return un JSON formaté contenant l'erreur
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
     * Enregistre les appels d'API pour le débogage.
     *
     * @param method le nom de la méthode appelée
     * @param params les paramètres de la méthode
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