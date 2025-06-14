package model;

import java.io.Serializable;

/**
 * Représente un restaurant dans le système de réservation Nancy Spot.
 * Cette classe contient toutes les informations nécessaires pour identifier
 * et localiser un restaurant, incluant ses coordonnées géographiques.
 *
 * @author Nancy Spot Team
 * @version 1.0
 * @since 1.0
 */
public class Restaurant implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nom;
    private String adresse;
    private String telephone;
    private double latitude;
    private double longitude;

    /**
     * Constructeur par défaut.
     * Crée un restaurant avec des valeurs par défaut.
     */
    public Restaurant() {
    }

    /**
     * Constructeur complet pour créer un restaurant avec toutes ses informations.
     *
     * @param id l'identifiant unique du restaurant
     * @param nom le nom du restaurant
     * @param adresse l'adresse complète du restaurant
     * @param telephone le numéro de téléphone du restaurant
     * @param latitude la latitude géographique du restaurant
     * @param longitude la longitude géographique du restaurant
     */
    public Restaurant(int id, String nom, String adresse, String telephone,
                      double latitude, double longitude) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Retourne l'identifiant unique du restaurant.
     *
     * @return l'identifiant du restaurant
     */
    public int getId() {
        return id;
    }

    /**
     * Définit l'identifiant unique du restaurant.
     *
     * @param id l'identifiant à assigner au restaurant
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Retourne le nom du restaurant.
     *
     * @return le nom du restaurant
     */
    public String getNom() {
        return nom;
    }

    /**
     * Définit le nom du restaurant.
     *
     * @param nom le nom à assigner au restaurant
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * Retourne l'adresse complète du restaurant.
     *
     * @return l'adresse du restaurant
     */
    public String getAdresse() {
        return adresse;
    }

    /**
     * Définit l'adresse complète du restaurant.
     *
     * @param adresse l'adresse à assigner au restaurant
     */
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    /**
     * Retourne le numéro de téléphone du restaurant.
     *
     * @return le numéro de téléphone du restaurant
     */
    public String getTelephone() {
        return telephone;
    }

    /**
     * Définit le numéro de téléphone du restaurant.
     *
     * @param telephone le numéro de téléphone à assigner au restaurant
     */
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    /**
     * Retourne la latitude géographique du restaurant.
     *
     * @return la latitude du restaurant
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Définit la latitude géographique du restaurant.
     *
     * @param latitude la latitude à assigner au restaurant
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Retourne la longitude géographique du restaurant.
     *
     * @return la longitude du restaurant
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Définit la longitude géographique du restaurant.
     *
     * @param longitude la longitude à assigner au restaurant
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Retourne une représentation textuelle du restaurant.
     *
     * @return une chaîne de caractères décrivant le restaurant
     */
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