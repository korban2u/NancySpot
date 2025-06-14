package model;

import java.io.Serializable;

/**
 * Représente une table de restaurant dans le système de réservation.
 * Chaque table appartient à un restaurant spécifique et possède une capacité
 * et un statut de disponibilité.
 */
public class TableResto implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int restaurantId;
    private int numeroTable;
    private int nbPlaces;
    private String statut;

    /**
     * Constructeur par défaut.
     * Crée une table avec des valeurs par défaut.
     */
    public TableResto() {
    }

    /**
     * Constructeur complet pour créer une table avec toutes ses informations.
     *
     * @param id l'identifiant unique de la table
     * @param restaurantId l'identifiant du restaurant auquel appartient la table
     * @param numeroTable le numéro de la table dans le restaurant
     * @param nbPlaces le nombre de places assises à cette table
     * @param statut le statut de la table ("libre" ou "occupee")
     */
    public TableResto(int id, int restaurantId, int numeroTable,
                      int nbPlaces, String statut) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.numeroTable = numeroTable;
        this.nbPlaces = nbPlaces;
        this.statut = statut;
    }

    /**
     * Retourne l'identifiant unique de la table.
     *
     * @return l'identifiant de la table
     */
    public int getId() {
        return id;
    }

    /**
     * Définit l'identifiant unique de la table.
     *
     * @param id l'identifiant à assigner à la table
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Retourne l'identifiant du restaurant auquel appartient cette table.
     *
     * @return l'identifiant du restaurant
     */
    public int getRestaurantId() {
        return restaurantId;
    }

    /**
     * Définit l'identifiant du restaurant auquel appartient cette table.
     *
     * @param restaurantId l'identifiant du restaurant
     */
    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    /**
     * Retourne le numéro de la table dans le restaurant.
     *
     * @return le numéro de la table
     */
    public int getNumeroTable() {
        return numeroTable;
    }

    /**
     * Définit le numéro de la table dans le restaurant.
     *
     * @param numeroTable le numéro à assigner à la table
     */
    public void setNumeroTable(int numeroTable) {
        this.numeroTable = numeroTable;
    }

    /**
     * Retourne le nombre de places assises à cette table.
     *
     * @return le nombre de places
     */
    public int getNbPlaces() {
        return nbPlaces;
    }

    /**
     * Définit le nombre de places assises à cette table.
     *
     * @param nbPlaces le nombre de places à assigner à la table
     */
    public void setNbPlaces(int nbPlaces) {
        this.nbPlaces = nbPlaces;
    }

    /**
     * Retourne le statut actuel de la table.
     *
     * @return le statut de la table ("libre" ou "occupee")
     */
    public String getStatut() {
        return statut;
    }

    /**
     * Définit le statut de la table.
     *
     * @param statut le statut à assigner à la table ("libre" ou "occupee")
     */
    public void setStatut(String statut) {
        this.statut = statut;
    }

    /**
     * Retourne une représentation textuelle de la table.
     *
     * @return une chaîne de caractères décrivant la table
     */
    @Override
    public String toString() {
        return "TableResto{" +
                "id=" + id +
                ", restaurantId=" + restaurantId +
                ", numeroTable=" + numeroTable +
                ", nbPlaces=" + nbPlaces +
                ", statut='" + statut + '\'' +
                '}';
    }
}