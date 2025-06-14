package model;

import java.io.Serializable;
import java.util.Date;

/**
 * Représente une réservation de table avec gestion des créneaux horaires.
 * Une réservation lie une table, un créneau et une date spécifique avec
 * les informations du client.
 *
 * @author Nancy Spot Team
 * @version 2.0
 * @since 1.0
 */
public class Reservation implements Serializable {
    private static final long serialVersionUID = 2L;

    private int id;
    private int tableId;
    private int creneauId;
    private Date dateReservation;
    private String nomClient;
    private String prenomClient;
    private String telephone;
    private int nbConvives;
    private Date dateCreation;
    private String statut;

    private Creneau creneau;
    private TableResto table;

    /**
     * Constructeur par défaut.
     * Crée une réservation avec un statut confirmé et la date de création actuelle.
     */
    public Reservation() {
        this.dateCreation = new Date();
        this.statut = "confirmee";
    }

    /**
     * Constructeur pour nouvelle réservation.
     *
     * @param tableId l'identifiant de la table
     * @param creneauId l'identifiant du créneau
     * @param dateReservation la date de la réservation
     * @param nomClient le nom du client
     * @param prenomClient le prénom du client
     * @param telephone le téléphone du client
     * @param nbConvives le nombre de convives
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
     * Constructeur complet avec tous les paramètres.
     *
     * @param id l'identifiant de la réservation
     * @param tableId l'identifiant de la table
     * @param creneauId l'identifiant du créneau
     * @param dateReservation la date de la réservation
     * @param nomClient le nom du client
     * @param prenomClient le prénom du client
     * @param telephone le téléphone du client
     * @param nbConvives le nombre de convives
     * @param dateCreation la date de création de la réservation
     * @param statut le statut de la réservation
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

    /**
     * Retourne l'identifiant unique de la réservation.
     *
     * @return l'identifiant de la réservation
     */
    public int getId() {
        return id;
    }

    /**
     * Définit l'identifiant unique de la réservation.
     *
     * @param id l'identifiant à assigner
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Retourne l'identifiant de la table réservée.
     *
     * @return l'identifiant de la table
     */
    public int getTableId() {
        return tableId;
    }

    /**
     * Définit l'identifiant de la table réservée.
     *
     * @param tableId l'identifiant de la table
     */
    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    /**
     * Retourne l'identifiant du créneau horaire.
     *
     * @return l'identifiant du créneau
     */
    public int getCreneauId() {
        return creneauId;
    }

    /**
     * Définit l'identifiant du créneau horaire.
     *
     * @param creneauId l'identifiant du créneau
     */
    public void setCreneauId(int creneauId) {
        this.creneauId = creneauId;
    }

    /**
     * Retourne la date de la réservation.
     *
     * @return la date de réservation
     */
    public Date getDateReservation() {
        return dateReservation;
    }

    /**
     * Définit la date de la réservation.
     *
     * @param dateReservation la date de réservation
     */
    public void setDateReservation(Date dateReservation) {
        this.dateReservation = dateReservation;
    }

    /**
     * Retourne le nom du client.
     *
     * @return le nom du client
     */
    public String getNomClient() {
        return nomClient;
    }

    /**
     * Définit le nom du client.
     *
     * @param nomClient le nom du client
     */
    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    /**
     * Retourne le prénom du client.
     *
     * @return le prénom du client
     */
    public String getPrenomClient() {
        return prenomClient;
    }

    /**
     * Définit le prénom du client.
     *
     * @param prenomClient le prénom du client
     */
    public void setPrenomClient(String prenomClient) {
        this.prenomClient = prenomClient;
    }

    /**
     * Retourne le numéro de téléphone du client.
     *
     * @return le numéro de téléphone
     */
    public String getTelephone() {
        return telephone;
    }

    /**
     * Définit le numéro de téléphone du client.
     *
     * @param telephone le numéro de téléphone
     */
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    /**
     * Retourne le nombre de convives.
     *
     * @return le nombre de convives
     */
    public int getnbConvives() {
        return nbConvives;
    }

    /**
     * Définit le nombre de convives.
     *
     * @param nbConvives le nombre de convives
     */
    public void setnbConvives(int nbConvives) {
        this.nbConvives = nbConvives;
    }

    /**
     * Retourne la date de création de la réservation.
     *
     * @return la date de création
     */
    public Date getDateCreation() {
        return dateCreation;
    }

    /**
     * Définit la date de création de la réservation.
     *
     * @param dateCreation la date de création
     */
    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    /**
     * Retourne le statut de la réservation.
     *
     * @return le statut ("confirmee" ou "annulee")
     */
    public String getStatut() {
        return statut;
    }

    /**
     * Définit le statut de la réservation.
     *
     * @param statut le statut à assigner
     */
    public void setStatut(String statut) {
        this.statut = statut;
    }

    /**
     * Retourne l'objet créneau associé (propriété calculée).
     *
     * @return le créneau horaire
     */
    public Creneau getCreneau() {
        return creneau;
    }

    /**
     * Définit l'objet créneau associé.
     *
     * @param creneau le créneau horaire
     */
    public void setCreneau(Creneau creneau) {
        this.creneau = creneau;
    }

    /**
     * Retourne l'objet table associé (propriété calculée).
     *
     * @return la table réservée
     */
    public TableResto getTable() {
        return table;
    }

    /**
     * Définit l'objet table associé.
     *
     * @param table la table réservée
     */
    public void setTable(TableResto table) {
        this.table = table;
    }

    /**
     * Retourne le nom complet du client.
     *
     * @return le prénom et nom du client
     */
    public String getNomComplet() {
        return prenomClient + " " + nomClient;
    }

    /**
     * Vérifie si la réservation est confirmée.
     *
     * @return true si la réservation est confirmée
     */
    public boolean isConfirmee() {
        return "confirmee".equals(statut);
    }

    /**
     * Vérifie si la réservation est annulée.
     *
     * @return true si la réservation est annulée
     */
    public boolean isAnnulee() {
        return "annulee".equals(statut);
    }

    /**
     * Annule la réservation en changeant son statut.
     */
    public void annuler() {
        this.statut = "annulee";
    }

    /**
     * Confirme la réservation en changeant son statut.
     */
    public void confirmer() {
        this.statut = "confirmee";
    }

    /**
     * Retourne une description lisible de la réservation.
     *
     * @return une description formatée de la réservation
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
     * Vérifie si la réservation est valide (toutes les données obligatoires présentes).
     *
     * @return true si la réservation est valide
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
     * Retourne la clé unique pour éviter les doublons.
     * Basée sur table + créneau + date.
     *
     * @return la clé unique de la réservation
     */
    public String getCleUnique() {
        return tableId + "_" + creneauId + "_" +
                (dateReservation != null ? dateReservation.getTime() : "null");
    }

    /**
     * Compare deux réservations pour l'égalité basée sur l'identifiant.
     *
     * @param obj l'objet à comparer
     * @return true si les objets sont égaux
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Reservation that = (Reservation) obj;
        return id == that.id;
    }

    /**
     * Retourne le code de hachage basé sur l'identifiant.
     *
     * @return le code de hachage
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    /**
     * Retourne une représentation textuelle de la réservation.
     *
     * @return une chaîne de caractères décrivant la réservation
     */
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
     * Conversion vers une représentation simple pour les logs.
     *
     * @return une représentation simplifiée
     */
    public String toLogString() {
        return String.format("Réservation #%d - %s (%d pers.) - Table %d - Créneau %d - %s",
                id, getNomComplet(), nbConvives, tableId, creneauId, statut);
    }
}