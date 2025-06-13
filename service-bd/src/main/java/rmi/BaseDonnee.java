package rmi;

import interfaces.ServiceBD;
import interfaces.ServiceCentral;
import model.Restaurant;
import model.TableResto;
import model.Reservation;
import dao.RestaurantDAO;
import org.json.JSONObject;
import org.json.JSONArray;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;


public class BaseDonnee implements ServiceBD {

    private static final Logger LOGGER = Logger.getLogger(BaseDonnee.class.getName());

    private final RestaurantDAO restaurantDAO;



    public BaseDonnee(String dbUrl, String dbUser, String dbPassword) {
        this.restaurantDAO = new RestaurantDAO(dbUrl, dbUser, dbPassword);

        LOGGER.info("ServiceBD créé (pas encore exporté)");
    }

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

            return response.toString();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur getAllRestaurants", e);

            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", true);
            errorResponse.put("message", "Erreur lors de la récupération des restaurants: " + e.getMessage());
            return errorResponse.toString();
        }
    }

    @Override
    public String getTablesLibres(int restaurantId) throws RemoteException {
        LOGGER.info("Appel getTablesLibres() pour restaurant " + restaurantId);

        try {
            List<TableResto> tables = restaurantDAO.findTablesLibres(restaurantId);

            JSONArray jsonArray = new JSONArray();
            for (TableResto table : tables) {
                JSONObject jsonTable = new JSONObject();
                jsonTable.put("id", table.getId());
                jsonTable.put("restaurantId", table.getRestaurantId());
                jsonTable.put("numeroTable", table.getNumeroTable());
                jsonTable.put("nbPlaces", table.getNbPlaces());
                jsonTable.put("statut", table.getStatut());

                jsonArray.put(jsonTable);
            }

            JSONObject response = new JSONObject();
            response.put("tables", jsonArray);
            response.put("count", tables.size());
            response.put("restaurantId", restaurantId);

            return response.toString();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur getTablesLibres", e);

            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", true);
            errorResponse.put("message", "Erreur lors de la récupération des tables: " + e.getMessage());
            return errorResponse.toString();
        }
    }

    @Override
    public String reserverTable(String jsonReservation) throws RemoteException {
        LOGGER.info("Appel reserverTable() avec: " + jsonReservation);

        try {
            JSONObject jsonObj = new JSONObject(jsonReservation);

            Reservation reservation = new Reservation();
            reservation.setTableId(jsonObj.getInt("tableId"));
            reservation.setNomClient(jsonObj.getString("nomClient"));
            reservation.setPrenomClient(jsonObj.getString("prenomClient"));
            reservation.setTelephone(jsonObj.getString("telephone"));
            reservation.setNbConvives(jsonObj.getInt("nbConvives"));

            String dateStr = jsonObj.getString("dateReservation");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date dateReservation = sdf.parse(dateStr);
            reservation.setDateReservation(dateReservation);

            boolean success = restaurantDAO.reserverTable(reservation);

            JSONObject response = new JSONObject();
            if (success) {
                response.put("success", true);
                response.put("message", "Réservation effectuée avec succès");
                response.put("reservationId", reservation.getId());
            } else {
                response.put("success", false);
                response.put("message", "La table n'est plus disponible");
            }

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
    public boolean ping() throws RemoteException {
        return true;
    }
}