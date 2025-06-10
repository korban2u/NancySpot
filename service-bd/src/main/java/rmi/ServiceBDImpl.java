package rmi;

import interfaces.IServiceBD;
import model.Restaurant;
import model.Reservation;
import dao.RestaurantDAO;
import org.json.JSONObject;
import org.json.JSONArray;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Implémentation du service RMI pour la base de données
 */
public class ServiceBDImpl extends UnicastRemoteObject implements IServiceBD {

    private static final Logger LOGGER = Logger.getLogger(ServiceBDImpl.class.getName());
    private static final long serialVersionUID = 1L;

    // Configuration base de données
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    // DAO
    private final RestaurantDAO restaurantDAO;

    // Service central inscrit
    private String serviceCentralHost;
    private int serviceCentralPort;

    public ServiceBDImpl(String dbUrl, String dbUser, String dbPassword) throws RemoteException {
        super();
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.restaurantDAO = new RestaurantDAO(dbUrl, dbUser, dbPassword);

        LOGGER.info("ServiceBD démarré");
    }

    @Override
    public String getAllRestaurants() throws RemoteException {
        LOGGER.info("Appel getAllRestaurants()");

        try {
            List<Restaurant> restaurants = restaurantDAO.findAll();

            // Conversion en JSON
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
    public String reserverTable(String jsonReservation) throws RemoteException {
        LOGGER.info("Appel reserverTable() avec: " + jsonReservation);

        try {
            // Parser le JSON de la réservation
            JSONObject jsonObj = new JSONObject(jsonReservation);

            // Créer l'objet Reservation
            Reservation reservation = new Reservation();
            reservation.setTableId(jsonObj.getInt("tableId"));
            reservation.setNomClient(jsonObj.getString("nomClient"));
            reservation.setPrenomClient(jsonObj.getString("prenomClient"));
            reservation.setTelephone(jsonObj.getString("telephone"));
            reservation.setNbConvives(jsonObj.getInt("nbConvives"));

            // Parser la date
            String dateStr = jsonObj.getString("dateReservation");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date dateReservation = sdf.parse(dateStr);
            reservation.setDateReservation(dateReservation);

            // Effectuer la réservation
            boolean success = restaurantDAO.reserverTable(reservation);

            // Préparer la réponse
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
    public boolean inscrireServiceCentral(String host, int port) throws RemoteException {
        LOGGER.info("Inscription du service central: " + host + ":" + port);

        try {
            this.serviceCentralHost = host;
            this.serviceCentralPort = port;

            LOGGER.info("Service central inscrit avec succès");
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur inscription service central", e);
            return false;
        }
    }

    /**
     * Obtenir une connexion à la base de données
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
}