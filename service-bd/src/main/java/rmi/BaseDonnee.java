package rmi;

import interfaces.ServiceBD;
import model.Restaurant;
import model.TableResto;
import model.Reservation;
import model.Creneau;
import dao.RestaurantDAO;
import org.json.JSONObject;
import org.json.JSONArray;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Implémentation du service BD avec support des créneaux horaires
 * Version améliorée pour la gestion des réservations par créneaux
 *
 * @author Nancy Spot Team
 * @version 2.0 - Avec gestion des créneaux
 */
public class BaseDonnee implements ServiceBD {

    private static final Logger LOGGER = Logger.getLogger(BaseDonnee.class.getName());
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final RestaurantDAO restaurantDAO;

    public BaseDonnee(String dbUrl, String dbUser, String dbPassword) {
        this.restaurantDAO = new RestaurantDAO(dbUrl, dbUser, dbPassword);
        LOGGER.info("ServiceBD créé avec support des créneaux");
    }

    // ==================== MÉTHODES RESTAURANTS ====================

    @Override
    public String getAllRestaurants() throws RemoteException {
        LOGGER.info("Appel getAllRestaurants()");

        try {
            List<Restaurant> restaurants = restaurantDAO.findAll();

            JSONArray jsonArray = new JSONArray();
            for (Restaurant resto : restaurants) {
                JSONObject jsonResto = new JSONObject();
                jsonResto.put("id", resto.getId());
                jsonResto.put("nom", resto.getNom());
                jsonResto.put("adresse", resto.getAdresse());
                jsonResto.put("telephone", resto.getTelephone());
                jsonResto.put("latitude", resto.getLatitude());
                jsonResto.put("longitude", resto.getLongitude());
                jsonArray.put(jsonResto);
            }

            JSONObject response = new JSONObject();
            response.put("restaurants", jsonArray);
            response.put("count", restaurants.size());
            response.put("timestamp", System.currentTimeMillis());

            return response.toString();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur getAllRestaurants", e);
            return createErrorResponse("Erreur lors de la récupération des restaurants", e);
        }
    }

    // ==================== MÉTHODES CRÉNEAUX ====================

    @Override
    public String getCreneauxDisponibles() throws RemoteException {
        LOGGER.info("Appel getCreneauxDisponibles()");

        try {
            List<Creneau> creneaux = restaurantDAO.findCreneauxActifs();

            JSONArray jsonArray = new JSONArray();
            for (Creneau creneau : creneaux) {
                JSONObject jsonCreneau = new JSONObject();
                jsonCreneau.put("id", creneau.getId());
                jsonCreneau.put("libelle", creneau.getLibelle());
                jsonCreneau.put("heureDebut", creneau.getHeureDebut());
                jsonCreneau.put("heureFin", creneau.getHeureFin());
                jsonCreneau.put("description", creneau.getDescription());
                jsonCreneau.put("plageHoraire", creneau.getPlageHoraire());
                jsonCreneau.put("ordreAffichage", creneau.getOrdreAffichage());
                jsonArray.put(jsonCreneau);
            }

            JSONObject response = new JSONObject();
            response.put("creneaux", jsonArray);
            response.put("count", creneaux.size());
            response.put("timestamp", System.currentTimeMillis());

            return response.toString();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur getCreneauxDisponibles", e);
            return createErrorResponse("Erreur lors de la récupération des créneaux", e);
        }
    }

    @Override
    public String getCreneauById(int creneauId) throws RemoteException {
        LOGGER.info("Appel getCreneauById(" + creneauId + ")");

        try {
            Creneau creneau = restaurantDAO.findCreneauById(creneauId);

            if (creneau == null) {
                JSONObject errorResponse = new JSONObject();
                errorResponse.put("error", true);
                errorResponse.put("message", "Créneau non trouvé");
                errorResponse.put("creneauId", creneauId);
                return errorResponse.toString();
            }

            JSONObject jsonCreneau = new JSONObject();
            jsonCreneau.put("id", creneau.getId());
            jsonCreneau.put("libelle", creneau.getLibelle());
            jsonCreneau.put("heureDebut", creneau.getHeureDebut());
            jsonCreneau.put("heureFin", creneau.getHeureFin());
            jsonCreneau.put("description", creneau.getDescription());
            jsonCreneau.put("plageHoraire", creneau.getPlageHoraire());
            jsonCreneau.put("actif", creneau.isActif());
            jsonCreneau.put("ordreAffichage", creneau.getOrdreAffichage());

            JSONObject response = new JSONObject();
            response.put("creneau", jsonCreneau);
            response.put("timestamp", System.currentTimeMillis());

            return response.toString();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur getCreneauById " + creneauId, e);
            return createErrorResponse("Erreur lors de la récupération du créneau", e);
        }
    }

    // ==================== MÉTHODES TABLES ====================

    @Override
    @Deprecated
    public String getTablesLibres(int restaurantId) throws RemoteException {
        LOGGER.warning("Appel méthode dépréciée getTablesLibres() - utiliser getTablesLibresPourCreneau()");

        try {
            List<TableResto> tables = restaurantDAO.findAllTablesRestaurant(restaurantId);
            return formatTablesResponse(tables, restaurantId, "toutes", 0);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur getTablesLibres", e);
            return createErrorResponse("Erreur lors de la récupération des tables", e);
        }
    }

    @Override
    public String getTablesLibresPourCreneau(int restaurantId, String dateReservation, int creneauId) throws RemoteException {
        LOGGER.info("Appel getTablesLibresPourCreneau(" + restaurantId + ", " + dateReservation + ", " + creneauId + ")");

        try {
            // Validation des paramètres
            if (!isValidDate(dateReservation)) {
                return createValidationError("Date invalide. Format attendu: yyyy-MM-dd");
            }

            List<TableResto> tables = restaurantDAO.findTablesLibresPourCreneau(restaurantId, dateReservation, creneauId);
            return formatTablesResponse(tables, restaurantId, dateReservation, creneauId);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur getTablesLibresPourCreneau", e);
            return createErrorResponse("Erreur lors de la récupération des tables libres", e);
        }
    }

    @Override
    public String getTablesAvecStatut(int restaurantId, String dateReservation, int creneauId) throws RemoteException {
        LOGGER.info("Appel getTablesAvecStatut(" + restaurantId + ", " + dateReservation + ", " + creneauId + ")");

        try {
            // Validation des paramètres
            if (!isValidDate(dateReservation)) {
                return createValidationError("Date invalide. Format attendu: yyyy-MM-dd");
            }

            List<TableResto> tables = restaurantDAO.findTablesAvecStatut(restaurantId, dateReservation, creneauId);

            JSONArray jsonArray = new JSONArray();
            for (TableResto table : tables) {
                JSONObject jsonTable = new JSONObject();
                jsonTable.put("id", table.getId());
                jsonTable.put("restaurantId", table.getRestaurantId());
                jsonTable.put("numeroTable", table.getNumeroTable());
                jsonTable.put("nbPlaces", table.getNbPlaces());
                jsonTable.put("statut", table.getStatut());
                jsonTable.put("disponible", "libre".equals(table.getStatut()));
                jsonArray.put(jsonTable);
            }

            JSONObject response = new JSONObject();
            response.put("tables", jsonArray);
            response.put("count", tables.size());
            response.put("restaurantId", restaurantId);
            response.put("dateReservation", dateReservation);
            response.put("creneauId", creneauId);
            response.put("timestamp", System.currentTimeMillis());

            return response.toString();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur getTablesAvecStatut", e);
            return createErrorResponse("Erreur lors de la récupération du statut des tables", e);
        }
    }

    // ==================== MÉTHODES RÉSERVATIONS ====================

    @Override
    public String reserverTable(String jsonReservation) throws RemoteException {
        LOGGER.info("Appel reserverTable() avec: " + jsonReservation);

        try {
            JSONObject jsonObj = new JSONObject(jsonReservation);

            // Validation des champs obligatoires
            String[] requiredFields = {"tableId", "creneauId", "dateReservation", "nomClient", "prenomClient", "telephone", "nbConvives"};
            for (String field : requiredFields) {
                if (!jsonObj.has(field)) {
                    return createValidationError("Champ obligatoire manquant: " + field);
                }
            }

            // Création de l'objet réservation
            Reservation reservation = new Reservation();
            reservation.setTableId(jsonObj.getInt("tableId"));
            reservation.setCreneauId(jsonObj.getInt("creneauId"));
            reservation.setNomClient(jsonObj.getString("nomClient"));
            reservation.setPrenomClient(jsonObj.getString("prenomClient"));
            reservation.setTelephone(jsonObj.getString("telephone"));
            reservation.setNbConvives(jsonObj.getInt("nbConvives"));

            // Parsing de la date
            String dateStr = jsonObj.getString("dateReservation");
            Date dateReservation;

            try {
                if (dateStr.contains(" ")) {
                    // Format avec heure (compatibilité)
                    dateReservation = DATETIME_FORMAT.parse(dateStr);
                } else {
                    // Format date seule
                    dateReservation = DATE_FORMAT.parse(dateStr);
                }
                reservation.setDateReservation(dateReservation);
            } catch (ParseException e) {
                return createValidationError("Format de date invalide. Utilisez yyyy-MM-dd ou yyyy-MM-dd HH:mm");
            }

            // Validation métier
            if (!reservation.isValide()) {
                return createValidationError("Données de réservation invalides");
            }

            // Tentative de réservation
            boolean success = restaurantDAO.reserverTable(reservation);

            JSONObject response = new JSONObject();
            if (success) {
                response.put("success", true);
                response.put("message", "Réservation effectuée avec succès");
                response.put("reservationId", reservation.getId());
                response.put("reservation", formatReservationJson(reservation));
            } else {
                response.put("success", false);
                response.put("message", "La table n'est plus disponible pour ce créneau");
            }
            response.put("timestamp", System.currentTimeMillis());

            return response.toString();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur reserverTable", e);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("success", false);
            errorResponse.put("error", true);
            errorResponse.put("message", "Erreur lors de la réservation: " + e.getMessage());
            return errorResponse.toString();
        }
    }

    @Override
    public String verifierDisponibilite(int tableId, String dateReservation, int creneauId) throws RemoteException {
        LOGGER.info("Appel verifierDisponibilite(" + tableId + ", " + dateReservation + ", " + creneauId + ")");

        try {
            if (!isValidDate(dateReservation)) {
                return createValidationError("Date invalide. Format attendu: yyyy-MM-dd");
            }

            boolean disponible = restaurantDAO.verifierDisponibilite(tableId, dateReservation, creneauId);

            JSONObject response = new JSONObject();
            response.put("tableId", tableId);
            response.put("dateReservation", dateReservation);
            response.put("creneauId", creneauId);
            response.put("disponible", disponible);
            response.put("statut", disponible ? "libre" : "occupee");
            response.put("timestamp", System.currentTimeMillis());

            return response.toString();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur verifierDisponibilite", e);
            return createErrorResponse("Erreur lors de la vérification de disponibilité", e);
        }
    }

    @Override
    public String getReservationsPourDate(int restaurantId, String dateReservation) throws RemoteException {
        LOGGER.info("Appel getReservationsPourDate(" + restaurantId + ", " + dateReservation + ")");

        try {
            if (!isValidDate(dateReservation)) {
                return createValidationError("Date invalide. Format attendu: yyyy-MM-dd");
            }

            List<Reservation> reservations = restaurantDAO.findReservationsPourDate(restaurantId, dateReservation);

            JSONArray jsonArray = new JSONArray();
            for (Reservation reservation : reservations) {
                jsonArray.put(formatReservationJson(reservation));
            }

            JSONObject response = new JSONObject();
            response.put("reservations", jsonArray);
            response.put("count", reservations.size());
            response.put("restaurantId", restaurantId);
            response.put("dateReservation", dateReservation);
            response.put("timestamp", System.currentTimeMillis());

            return response.toString();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur getReservationsPourDate", e);
            return createErrorResponse("Erreur lors de la récupération des réservations", e);
        }
    }

    @Override
    public String annulerReservation(int reservationId) throws RemoteException {
        LOGGER.info("Appel annulerReservation(" + reservationId + ")");

        try {
            boolean success = restaurantDAO.annulerReservation(reservationId);

            JSONObject response = new JSONObject();
            response.put("success", success);
            response.put("reservationId", reservationId);
            response.put("message", success ? "Réservation annulée avec succès" : "Réservation non trouvée ou déjà annulée");
            response.put("timestamp", System.currentTimeMillis());

            return response.toString();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur annulerReservation", e);
            return createErrorResponse("Erreur lors de l'annulation de la réservation", e);
        }
    }

    // ==================== MÉTHODES NON IMPLÉMENTÉES (FUTURES) ====================

    @Override
    public String getStatistiquesReservations(int restaurantId, String dateDebut, String dateFin) throws RemoteException {
        // TODO: Implémenter les statistiques
        JSONObject response = new JSONObject();
        response.put("error", true);
        response.put("message", "Fonctionnalité non encore implémentée");
        return response.toString();
    }

    @Override
    public String getPlanningRestaurant(int restaurantId, String dateDebut, String dateFin) throws RemoteException {
        // TODO: Implémenter le planning
        JSONObject response = new JSONObject();
        response.put("error", true);
        response.put("message", "Fonctionnalité non encore implémentée");
        return response.toString();
    }

    // ==================== MÉTHODES SYSTÈME ====================

    @Override
    public boolean ping() throws RemoteException {
        return true;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private String formatTablesResponse(List<TableResto> tables, int restaurantId, String date, int creneauId) {
        JSONArray jsonArray = new JSONArray();
        for (TableResto table : tables) {
            JSONObject jsonTable = new JSONObject();
            jsonTable.put("id", table.getId());
            jsonTable.put("restaurantId", table.getRestaurantId());
            jsonTable.put("numeroTable", table.getNumeroTable());
            jsonTable.put("nbPlaces", table.getNbPlaces());
            jsonTable.put("statut", table.getStatut() != null ? table.getStatut() : "libre");
            jsonArray.put(jsonTable);
        }

        JSONObject response = new JSONObject();
        response.put("tables", jsonArray);
        response.put("count", tables.size());
        response.put("restaurantId", restaurantId);
        if (!"toutes".equals(date)) {
            response.put("dateReservation", date);
            response.put("creneauId", creneauId);
        }
        response.put("timestamp", System.currentTimeMillis());

        return response.toString();
    }

    private JSONObject formatReservationJson(Reservation reservation) {
        JSONObject jsonReservation = new JSONObject();
        jsonReservation.put("id", reservation.getId());
        jsonReservation.put("tableId", reservation.getTableId());
        jsonReservation.put("creneauId", reservation.getCreneauId());
        jsonReservation.put("dateReservation", DATE_FORMAT.format(reservation.getDateReservation()));
        jsonReservation.put("nomClient", reservation.getNomClient());
        jsonReservation.put("prenomClient", reservation.getPrenomClient());
        jsonReservation.put("telephone", reservation.getTelephone());
        jsonReservation.put("nbConvives", reservation.getNbConvives());
        jsonReservation.put("statut", reservation.getStatut());
        jsonReservation.put("nomComplet", reservation.getNomComplet());

        if (reservation.getDateCreation() != null) {
            jsonReservation.put("dateCreation", reservation.getDateCreation().getTime());
        }

        if (reservation.getTable() != null) {
            JSONObject tableInfo = new JSONObject();
            tableInfo.put("numeroTable", reservation.getTable().getNumeroTable());
            tableInfo.put("nbPlaces", reservation.getTable().getNbPlaces());
            jsonReservation.put("table", tableInfo);
        }

        if (reservation.getCreneau() != null) {
            JSONObject creneauInfo = new JSONObject();
            creneauInfo.put("libelle", reservation.getCreneau().getLibelle());
            creneauInfo.put("heureDebut", reservation.getCreneau().getHeureDebut());
            creneauInfo.put("heureFin", reservation.getCreneau().getHeureFin());
            creneauInfo.put("description", reservation.getCreneau().getDescription());
            jsonReservation.put("creneau", creneauInfo);
        }

        return jsonReservation;
    }

    private String createErrorResponse(String message, Exception e) {
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("error", true);
        errorResponse.put("message", message + ": " + e.getMessage());
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse.toString();
    }

    private String createValidationError(String message) {
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("error", true);
        errorResponse.put("validation", true);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse.toString();
    }

    private boolean isValidDate(String dateString) {
        try {
            DATE_FORMAT.parse(dateString);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}