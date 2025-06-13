package dao;

import model.Restaurant;
import model.TableResto;
import model.Reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;


public class RestaurantDAO {

    private static final Logger LOGGER = Logger.getLogger(RestaurantDAO.class.getName());

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public RestaurantDAO(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }


    public List<Restaurant> findAll() throws SQLException {
        List<Restaurant> restaurants = new ArrayList<>();
        String sql = "SELECT id, nom, adresse, telephone, latitude, longitude FROM restaurant ORDER BY nom";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Restaurant resto = new Restaurant();
                resto.setId(rs.getInt("id"));
                resto.setNom(rs.getString("nom"));
                resto.setAdresse(rs.getString("adresse"));
                resto.setTelephone(rs.getString("telephone"));
                resto.setLatitude(rs.getDouble("latitude"));
                resto.setLongitude(rs.getDouble("longitude"));

                restaurants.add(resto);
            }

            LOGGER.info("Trouvé " + restaurants.size() + " restaurants");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur findAll", e);
            throw e;
        }

        return restaurants;
    }


    public List<TableResto> findTablesLibres(int restaurantId) throws SQLException {
        List<TableResto> tables = new ArrayList<>();
        String sql = "SELECT id, restaurant_id, numero_table, nb_places, statut " +
                "FROM tables_resto " +
                "WHERE restaurant_id = ? AND statut = 'libre' " +
                "ORDER BY numero_table";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, restaurantId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TableResto table = new TableResto();
                    table.setId(rs.getInt("id"));
                    table.setRestaurantId(rs.getInt("restaurant_id"));
                    table.setNumeroTable(rs.getInt("numero_table"));
                    table.setNbPlaces(rs.getInt("nb_places"));
                    table.setStatut(rs.getString("statut"));

                    tables.add(table);
                }
            }

            LOGGER.info("Trouvé " + tables.size() + " tables libres pour restaurant " + restaurantId);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur findTablesLibres", e);
            throw e;
        }

        return tables;
    }


    public boolean reserverTable(Reservation reservation) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmtCheck = null;
        PreparedStatement pstmtUpdate = null;
        PreparedStatement pstmtInsert = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            String sqlCheck = "SELECT statut FROM tables_resto WHERE id = ? FOR UPDATE";
            pstmtCheck = conn.prepareStatement(sqlCheck);
            pstmtCheck.setInt(1, reservation.getTableId());
            rs = pstmtCheck.executeQuery();

            if (!rs.next()) {
                LOGGER.warning("Table " + reservation.getTableId() + " introuvable");
                conn.rollback();
                return false;
            }

            String statut = rs.getString("statut");
            if (!"libre".equals(statut)) {
                LOGGER.warning("Table " + reservation.getTableId() + " déjà occupée");
                conn.rollback();
                return false;
            }

            // 2. Mettre à jour le statut de la table
            String sqlUpdate = "UPDATE tables_resto SET statut = 'occupee' WHERE id = ?";
            pstmtUpdate = conn.prepareStatement(sqlUpdate);
            pstmtUpdate.setInt(1, reservation.getTableId());
            int rowsUpdated = pstmtUpdate.executeUpdate();

            if (rowsUpdated != 1) {
                LOGGER.warning("Impossible de mettre à jour la table " + reservation.getTableId());
                conn.rollback();
                return false;
            }

            // 3. Insérer la réservation
            String sqlInsert = "INSERT INTO reservation (id, table_id, nom_client, prenom_client, " +
                    "telephone, nb_convives, date_reservation) " +
                    "VALUES (seq_reservation.NEXTVAL, ?, ?, ?, ?, ?, ?)";

            pstmtInsert = conn.prepareStatement(sqlInsert, new String[]{"ID"});
            pstmtInsert.setInt(1, reservation.getTableId());
            pstmtInsert.setString(2, reservation.getNomClient());
            pstmtInsert.setString(3, reservation.getPrenomClient());
            pstmtInsert.setString(4, reservation.getTelephone());
            pstmtInsert.setInt(5, reservation.getNbConvives());
            pstmtInsert.setTimestamp(6, new Timestamp(reservation.getDateReservation().getTime()));

            int rowsInserted = pstmtInsert.executeUpdate();

            if (rowsInserted != 1) {
                LOGGER.warning("Impossible d'insérer la réservation");
                conn.rollback();
                return false;
            }

            // Récupérer l'ID généré
            try (ResultSet generatedKeys = pstmtInsert.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reservation.setId(generatedKeys.getInt(1));
                }
            }

            // 4. Valider la transaction
            conn.commit();
            LOGGER.info("Réservation effectuée avec succès, ID: " + reservation.getId());
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la réservation", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Erreur lors du rollback", ex);
                }
            }
            throw e;
        } finally {
            // Fermer toutes les ressources
            closeQuietly(rs);
            closeQuietly(pstmtCheck);
            closeQuietly(pstmtUpdate);
            closeQuietly(pstmtInsert);
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Erreur fermeture connexion", e);
                }
            }
        }
    }


    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }


    private void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Erreur fermeture ressource", e);
            }
        }
    }
}