package model;

import java.io.Serializable;
import java.util.Date;

/**
 * Modèle représentant une réservation de table avec gestion des créneaux horaires
 * Une réservation lie une table, un créneau et une date spécifique
 *
 * @author Nancy Spot Team
 * @version 2.0 - Avec support des créneaux
 */
public class Reservation implements Serializable {
    private static final long serialVersionUID = 2L; // Incrémenté pour la nouvelle version

    private int id;
    private int tableId;
    private int creneauId;           // Nouveau : référence au créneau
    private Date dateReservation;    // Date uniquement (sans heure)
    private String nomClient;
    private String prenomClient;
    private String telephone;
    private int nbConvives;
    private Date dateCreation;       // Nouveau : horodatage de création
    private String statut;           // Nouveau : confirmee, annulee

    // Propriétés calculées (non persistées)
    private Creneau creneau;         // Objet créneau complet (pour affichage)
    private TableResto table;        // Objet table complet (pour affichage)

    /**
     * Constructeur par défaut
     */
    public Reservation() {
        this.dateCreation = new Date();
        this.statut = "confirmee";
    }

    /**
     * Constructeur complet pour nouvelle réservation
     *
     * @param tableId Identifiant de la table
     * @param creneauId Identifiant du créneau
     * @param dateReservation Date de la réservation
     * @param nomClient Nom du client
     * @param prenomClient Prénom du client
     * @param telephone Téléphone du client
     * @param nbConvives Nombre de convives
     */
    public Reservation(int tableId, int creneauId, Date dateReservation,
                       String nomClient, String prenomClient, String telephone, int nbConvives) {
        this();
        this.tableId = tableId;
        this.creneauId = creneauId;
        this.dateReservation = dateReservation;
        this.nomClient = nomClient;
        this.prenomClient = prenomClient;
        this.telephone = telephone;
        this.nbConvives = nbConvives;
    }

    /**
     * Constructeur complet avec tous les paramètres
     */
    public Reservation(int id, int tableId, int creneauId, Date dateReservation,
                       String nomClient, String prenomClient, String telephone,
                       int nbConvives, Date dateCreation, String statut) {
        this.id = id;
        this.tableId = tableId;
        this.creneauId = creneauId;
        this.dateReservation = dateReservation;
        this.nomClient = nomClient;
        this.prenomClient = prenomClient;
        this.telephone = telephone;
        this.nbConvives = nbConvives;
        this.dateCreation = dateCreation;
        this.statut = statut;
    }

    // ==================== GETTERS ET SETTERS ====================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public int getCreneauId() {
        return creneauId;
    }

    public void setCreneauId(int creneauId) {
        this.creneauId = creneauId;
    }

    public Date getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(Date dateReservation) {
        this.dateReservation = dateReservation;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public String getPrenomClient() {
        return prenomClient;
    }

    public void setPrenomClient(String prenomClient) {
        this.prenomClient = prenomClient;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public int getnbConvives() {
        return nbConvives;
    }

    public void setnbConvives(int nbConvives) {
        this.nbConvives = nbConvives;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    // Propriétés calculées
    public Creneau getCreneau() {
        return creneau;
    }

    public void setCreneau(Creneau creneau) {
        this.creneau = creneau;
    }

    public TableResto getTable() {
        return table;
    }

    public void setTable(TableResto table) {
        this.table = table;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Retourne le nom complet du client
     *
     * @return Prénom + Nom
     */
    public String getNomComplet() {
        return prenomClient + " " + nomClient;
    }

    /**
     * Vérifie si la réservation est confirmée
     *
     * @return true si la réservation est confirmée
     */
    public boolean isConfirmee() {
        return "confirmee".equals(statut);
    }

    /**
     * Vérifie si la réservation est annulée
     *
     * @return true si la réservation est annulée
     */
    public boolean isAnnulee() {
        return "annulee".equals(statut);
    }

    /**
     * Annule la réservation
     */
    public void annuler() {
        this.statut = "annulee";
    }

    /**
     * Confirme la réservation
     */
    public void confirmer() {
        this.statut = "confirmee";
    }

    /**
     * Retourne une description lisible de la réservation
     *
     * @return Description formatée
     */
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Réservation de ").append(getNomComplet());
        sb.append(" pour ").append(nbConvives).append(" personne(s)");

        if (creneau != null) {
            sb.append(" - ").append(creneau.getLibelle());
            sb.append(" (").append(creneau.getPlageHoraire()).append(")");
        }

        if (table != null) {
            sb.append(" - Table ").append(table.getNumeroTable());
        }

        return sb.toString();
    }

    /**
     * Vérifie si la réservation est valide
     *
     * @return true si toutes les données obligatoires sont renseignées
     */
    public boolean isValide() {
        return tableId > 0
                && creneauId > 0
                && dateReservation != null
                && nomClient != null && !nomClient.trim().isEmpty()
                && prenomClient != null && !prenomClient.trim().isEmpty()
                && telephone != null && !telephone.trim().isEmpty()
                && nbConvives > 0;
    }

    /**
     * Retourne la clé unique pour éviter les doublons
     * Basée sur table + créneau + date
     *
     * @return Clé unique de la réservation
     */
    public String getCleUnique() {
        return tableId + "_" + creneauId + "_" +
                (dateReservation != null ? dateReservation.getTime() : "null");
    }

    // ==================== MÉTHODES STANDARD ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Reservation that = (Reservation) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", tableId=" + tableId +
                ", creneauId=" + creneauId +
                ", dateReservation=" + dateReservation +
                ", nomClient='" + nomClient + '\'' +
                ", prenomClient='" + prenomClient + '\'' +
                ", telephone='" + telephone + '\'' +
                ", nbConvives=" + nbConvives +
                ", statut='" + statut + '\'' +
                '}';
    }

    /**
     * Conversion vers une représentation simple pour les logs
     *
     * @return Représentation simplifiée
     */
    public String toLogString() {
        return String.format("Réservation #%d - %s (%d pers.) - Table %d - Créneau %d - %s",
                id, getNomComplet(), nbConvives, tableId, creneauId, statut);
    }
}