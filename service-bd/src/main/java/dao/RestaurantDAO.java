package dao;

import model.Restaurant;
import model.TableResto;
import model.Reservation;
import model.Creneau;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * DAO pour la gestion des restaurants, tables et réservations avec support des créneaux
 * Version améliorée avec gestion transactionnelle et méthodes pour les créneaux horaires
 *
 * @author Nancy Spot Team
 * @version 2.0 - Avec gestion des créneaux
 */
public class RestaurantDAO {

    private static final Logger LOGGER = Logger.getLogger(RestaurantDAO.class.getName());
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public RestaurantDAO(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    // ==================== MÉTHODES RESTAURANTS ====================

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
            LOGGER.log(Level.SEVERE, "Erreur findAll restaurants", e);
            throw e;
        }

        return restaurants;
    }

    // ==================== MÉTHODES CRÉNEAUX ====================

    /**
     * Récupère tous les créneaux actifs triés par ordre d'affichage
     */
    public List<Creneau> findCreneauxActifs() throws SQLException {
        List<Creneau> creneaux = new ArrayList<>();
        String sql = "SELECT id, libelle, heure_debut, heure_fin, actif, ordre_affichage " +
                "FROM creneau WHERE actif = 1 ORDER BY ordre_affichage, heure_debut";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Creneau creneau = new Creneau();
                creneau.setId(rs.getInt("id"));
                creneau.setLibelle(rs.getString("libelle"));
                creneau.setHeureDebut(rs.getString("heure_debut"));
                creneau.setHeureFin(rs.getString("heure_fin"));
                creneau.setActif(rs.getInt("actif") == 1);
                creneau.setOrdreAffichage(rs.getInt("ordre_affichage"));
                creneaux.add(creneau);
            }

            LOGGER.info("Trouvé " + creneaux.size() + " créneaux actifs");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur findCreneauxActifs", e);
            throw e;
        }

        return creneaux;
    }

    /**
     * Récupère un créneau par son ID
     */
    public Creneau findCreneauById(int creneauId) throws SQLException {
        String sql = "SELECT id, libelle, heure_debut, heure_fin, actif, ordre_affichage " +
                "FROM creneau WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, creneauId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Creneau creneau = new Creneau();
                    creneau.setId(rs.getInt("id"));
                    creneau.setLibelle(rs.getString("libelle"));
                    creneau.setHeureDebut(rs.getString("heure_debut"));
                    creneau.setHeureFin(rs.getString("heure_fin"));
                    creneau.setActif(rs.getInt("actif") == 1);
                    creneau.setOrdreAffichage(rs.getInt("ordre_affichage"));
                    return creneau;
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur findCreneauById " + creneauId, e);
            throw e;
        }

        return null;
    }

    // ==================== MÉTHODES TABLES ====================

    /**
     * Méthode dépréciée - utiliser findTablesLibresPourCreneau à la place
     */
    @Deprecated
    public List<TableResto> findTablesLibres(int restaurantId) throws SQLException {
        return findAllTablesRestaurant(restaurantId);
    }

    /**
     * Récupère toutes les tables d'un restaurant
     */
    public List<TableResto> findAllTablesRestaurant(int restaurantId) throws SQLException {
        List<TableResto> tables = new ArrayList<>();
        String sql = "SELECT id, restaurant_id, numero_table, nb_places " +
                "FROM tables_resto WHERE restaurant_id = ? ORDER BY numero_table";

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
                    tables.add(table);
                }
            }

            LOGGER.info("Trouvé " + tables.size() + " tables pour restaurant " + restaurantId);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur findAllTablesRestaurant", e);
            throw e;
        }

        return tables;
    }

    /**
     * Récupère les tables libres pour un restaurant, une date et un créneau donnés
     */
    public List<TableResto> findTablesLibresPourCreneau(int restaurantId, String dateReservation,
                                                        int creneauId) throws SQLException {
        List<TableResto> tables = new ArrayList<>();
        String sql = "SELECT t.id, t.restaurant_id, t.numero_table, t.nb_places " +
                "FROM tables_resto t " +
                "WHERE t.restaurant_id = ? " +
                "AND NOT EXISTS (" +
                "    SELECT 1 FROM reservation r " +
                "    WHERE r.table_id = t.id " +
                "    AND r.creneau_id = ? " +
                "    AND r.date_reservation = TO_DATE(?, 'YYYY-MM-DD') " +
                "    AND r.statut = 'confirmee'" +
                ") " +
                "ORDER BY t.numero_table";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, restaurantId);
            pstmt.setInt(2, creneauId);
            pstmt.setString(3, dateReservation);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TableResto table = new TableResto();
                    table.setId(rs.getInt("id"));
                    table.setRestaurantId(rs.getInt("restaurant_id"));
                    table.setNumeroTable(rs.getInt("numero_table"));
                    table.setNbPlaces(rs.getInt("nb_places"));
                    table.setStatut("libre"); // Par défaut libre pour cette requête
                    tables.add(table);
                }
            }

            LOGGER.info("Trouvé " + tables.size() + " tables libres pour restaurant " +
                    restaurantId + ", date " + dateReservation + ", créneau " + creneauId);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur findTablesLibresPourCreneau", e);
            throw e;
        }

        return tables;
    }

    /**
     * Récupère toutes les tables avec leur statut pour une date et un créneau
     */
    public List<TableResto> findTablesAvecStatut(int restaurantId, String dateReservation,
                                                 int creneauId) throws SQLException {
        List<TableResto> tables = new ArrayList<>();
        String sql = "SELECT t.id, t.restaurant_id, t.numero_table, t.nb_places, " +
                "CASE WHEN r.id IS NOT NULL THEN 'occupee' ELSE 'libre' END as statut " +
                "FROM tables_resto t " +
                "LEFT JOIN reservation r ON (t.id = r.table_id " +
                "    AND r.creneau_id = ? " +
                "    AND r.date_reservation = TO_DATE(?, 'YYYY-MM-DD') " +
                "    AND r.statut = 'confirmee') " +
                "WHERE t.restaurant_id = ? " +
                "ORDER BY t.numero_table";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, creneauId);
            pstmt.setString(2, dateReservation);
            pstmt.setInt(3, restaurantId);

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

            LOGGER.info("Récupéré statut de " + tables.size() + " tables pour restaurant " +
                    restaurantId + ", date " + dateReservation + ", créneau " + creneauId);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur findTablesAvecStatut", e);
            throw e;
        }

        return tables;
    }

    // ==================== MÉTHODES RÉSERVATIONS ====================

    /**
     * Vérifie la disponibilité d'une table pour un créneau et une date
     */
    public boolean verifierDisponibilite(int tableId, String dateReservation, int creneauId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservation " +
                "WHERE table_id = ? AND creneau_id = ? " +
                "AND date_reservation = TO_DATE(?, 'YYYY-MM-DD') " +
                "AND statut = 'confirmee'";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tableId);
            pstmt.setInt(2, creneauId);
            pstmt.setString(3, dateReservation);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0; // Disponible si aucune réservation trouvée
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur verifierDisponibilite", e);
            throw e;
        }

        return false;
    }

    /**
     * Effectue une réservation avec gestion transactionnelle
     */
    public boolean reserverTable(Reservation reservation) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmtCheck = null;
        PreparedStatement pstmtInsert = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // 1. Vérifier la disponibilité avec verrou sur la table
            String sqlCheck = "SELECT COUNT(*) FROM reservation " +
                    "WHERE table_id = ? AND creneau_id = ? " +
                    "AND date_reservation = ? AND statut = 'confirmee'";

            pstmtCheck = conn.prepareStatement(sqlCheck);
            pstmtCheck.setInt(1, reservation.getTableId());
            pstmtCheck.setInt(2, reservation.getCreneauId());
            pstmtCheck.setDate(3, new java.sql.Date(reservation.getDateReservation().getTime()));

            try (ResultSet rs = pstmtCheck.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    LOGGER.warning("Table " + reservation.getTableId() + " déjà réservée pour ce créneau");
                    conn.rollback();
                    return false;
                }
            }

            // 2. Insérer la réservation
            String sqlInsert = "INSERT INTO reservation " +
                    "(id, table_id, creneau_id, date_reservation, nom_client, prenom_client, " +
                    "telephone, nb_convives, date_creation, statut) " +
                    "VALUES (seq_reservation.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, 'confirmee')";

            pstmtInsert = conn.prepareStatement(sqlInsert, new String[]{"ID"});
            pstmtInsert.setInt(1, reservation.getTableId());
            pstmtInsert.setInt(2, reservation.getCreneauId());
            pstmtInsert.setDate(3, new java.sql.Date(reservation.getDateReservation().getTime()));
            pstmtInsert.setString(4, reservation.getNomClient());
            pstmtInsert.setString(5, reservation.getPrenomClient());
            pstmtInsert.setString(6, reservation.getTelephone());
            pstmtInsert.setInt(7, reservation.getnbConvives());

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

            // 3. Valider la transaction
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
            closeQuietly(pstmtCheck);
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

    /**
     * Récupère les réservations pour un restaurant et une date donnée
     */
    public List<Reservation> findReservationsPourDate(int restaurantId, String dateReservation) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.id, r.table_id, r.creneau_id, r.date_reservation, " +
                "r.nom_client, r.prenom_client, r.telephone, r.nb_convives, " +
                "r.date_creation, r.statut, " +
                "t.numero_table, t.nb_places, " +
                "c.libelle, c.heure_debut, c.heure_fin " +
                "FROM reservation r " +
                "JOIN tables_resto t ON r.table_id = t.id " +
                "JOIN creneau c ON r.creneau_id = c.id " +
                "WHERE t.restaurant_id = ? " +
                "AND r.date_reservation = TO_DATE(?, 'YYYY-MM-DD') " +
                "AND r.statut = 'confirmee' " +
                "ORDER BY c.ordre_affichage, t.numero_table";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, restaurantId);
            pstmt.setString(2, dateReservation);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = new Reservation();
                    reservation.setId(rs.getInt("id"));
                    reservation.setTableId(rs.getInt("table_id"));
                    reservation.setCreneauId(rs.getInt("creneau_id"));
                    reservation.setDateReservation(rs.getDate("date_reservation"));
                    reservation.setNomClient(rs.getString("nom_client"));
                    reservation.setPrenomClient(rs.getString("prenom_client"));
                    reservation.setTelephone(rs.getString("telephone"));
                    reservation.setnbConvives(rs.getInt("nb_convives"));
                    reservation.setDateCreation(rs.getTimestamp("date_creation"));
                    reservation.setStatut(rs.getString("statut"));

                    // Ajout des objets liés
                    TableResto table = new TableResto();
                    table.setId(rs.getInt("table_id"));
                    table.setNumeroTable(rs.getInt("numero_table"));
                    table.setNbPlaces(rs.getInt("nb_places"));
                    reservation.setTable(table);

                    Creneau creneau = new Creneau();
                    creneau.setId(rs.getInt("creneau_id"));
                    creneau.setLibelle(rs.getString("libelle"));
                    creneau.setHeureDebut(rs.getString("heure_debut"));
                    creneau.setHeureFin(rs.getString("heure_fin"));
                    reservation.setCreneau(creneau);

                    reservations.add(reservation);
                }
            }

            LOGGER.info("Trouvé " + reservations.size() + " réservations pour restaurant " +
                    restaurantId + " le " + dateReservation);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur findReservationsPourDate", e);
            throw e;
        }

        return reservations;
    }

    /**
     * Annule une réservation
     */
    public boolean annulerReservation(int reservationId) throws SQLException {
        String sql = "UPDATE reservation SET statut = 'annulee' WHERE id = ? AND statut = 'confirmee'";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reservationId);
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                LOGGER.info("Réservation " + reservationId + " annulée avec succès");
                return true;
            } else {
                LOGGER.warning("Réservation " + reservationId + " non trouvée ou déjà annulée");
                return false;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur annulerReservation " + reservationId, e);
            throw e;
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

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

    /**
     * Utilitaire pour convertir une chaîne de date en java.util.Date
     */
    private java.util.Date parseDate(String dateString) throws ParseException {
        return DATE_FORMAT.parse(dateString);
    }

    /**
     * Utilitaire pour convertir une java.util.Date en chaîne
     */
    private String formatDate(java.util.Date date) {
        return DATE_FORMAT.format(date);
    }
}