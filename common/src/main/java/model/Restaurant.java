package model;

import java.io.Serializable;

/**
 * Classe représentant un restaurant
 */
public class Restaurant implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nom;
    private String adresse;
    private String telephone;
    private double latitude;
    private double longitude;

    // Constructeur par défaut
    public Restaurant() {
    }

    // Constructeur avec tous les paramètres
    public Restaurant(int id, String nom, String adresse, String telephone,
                      double latitude, double longitude) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", adresse='" + adresse + '\'' +
                ", telephone='" + telephone + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}