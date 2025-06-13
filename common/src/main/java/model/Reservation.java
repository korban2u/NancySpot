package model;

import java.io.Serializable;
import java.util.Date;


public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int tableId;
    private String nomClient;
    private String prenomClient;
    private String telephone;
    private int nbConvives;
    private Date dateReservation;

    public Reservation() {
    }

    public Reservation(int id, int tableId, String nomClient, String prenomClient,
                       String telephone, int nbConvives, Date dateReservation) {
        this.id = id;
        this.tableId = tableId;
        this.nomClient = nomClient;
        this.prenomClient = prenomClient;
        this.telephone = telephone;
        this.nbConvives = nbConvives;
        this.dateReservation = dateReservation;
    }

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

    public int getNbConvives() {
        return nbConvives;
    }

    public void setNbConvives(int nbConvives) {
        this.nbConvives = nbConvives;
    }

    public Date getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(Date dateReservation) {
        this.dateReservation = dateReservation;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", tableId=" + tableId +
                ", nomClient='" + nomClient + '\'' +
                ", prenomClient='" + prenomClient + '\'' +
                ", telephone='" + telephone + '\'' +
                ", nbConvives=" + nbConvives +
                ", dateReservation=" + dateReservation +
                '}';
    }
}