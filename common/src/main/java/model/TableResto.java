package model;

import java.io.Serializable;


public class TableResto implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int restaurantId;
    private int numeroTable;
    private int nbPlaces;
    private String statut; // "libre" ou "occupee"

    public TableResto() {
    }

    public TableResto(int id, int restaurantId, int numeroTable,
                      int nbPlaces, String statut) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.numeroTable = numeroTable;
        this.nbPlaces = nbPlaces;
        this.statut = statut;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public int getNumeroTable() {
        return numeroTable;
    }

    public void setNumeroTable(int numeroTable) {
        this.numeroTable = numeroTable;
    }

    public int getNbPlaces() {
        return nbPlaces;
    }

    public void setNbPlaces(int nbPlaces) {
        this.nbPlaces = nbPlaces;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

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