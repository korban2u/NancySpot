package model;

import java.io.Serializable;

/**
 * Modèle représentant un créneau horaire pour les réservations
 * Un créneau définit une plage horaire pendant laquelle les réservations sont possibles
 *
 * @author Nancy Spot Team
 * @version 1.0
 */
public class Creneau implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String libelle;
    private String heureDebut;  // Format HH:MM
    private String heureFin;    // Format HH:MM
    private boolean actif;
    private int ordreAffichage;

    /**
     * Constructeur par défaut
     */
    public Creneau() {
        this.actif = true;
        this.ordreAffichage = 1;
    }

    /**
     * Constructeur avec tous les paramètres
     *
     * @param id Identifiant unique du créneau
     * @param libelle Nom du créneau (ex: "Déjeuner", "Dîner")
     * @param heureDebut Heure de début au format HH:MM
     * @param heureFin Heure de fin au format HH:MM
     * @param actif Indique si le créneau est actif
     * @param ordreAffichage Ordre d'affichage dans l'interface
     */
    public Creneau(int id, String libelle, String heureDebut, String heureFin,
                   boolean actif, int ordreAffichage) {
        this.id = id;
        this.libelle = libelle;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.actif = actif;
        this.ordreAffichage = ordreAffichage;
    }

    /**
     * Constructeur simplifié pour créer un créneau actif
     *
     * @param libelle Nom du créneau
     * @param heureDebut Heure de début au format HH:MM
     * @param heureFin Heure de fin au format HH:MM
     */
    public Creneau(String libelle, String heureDebut, String heureFin) {
        this.libelle = libelle;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.actif = true;
        this.ordreAffichage = 1;
    }

    // ==================== GETTERS ET SETTERS ====================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(String heureDebut) {
        this.heureDebut = heureDebut;
    }

    public String getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(String heureFin) {
        this.heureFin = heureFin;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public int getOrdreAffichage() {
        return ordreAffichage;
    }

    public void setOrdreAffichage(int ordreAffichage) {
        this.ordreAffichage = ordreAffichage;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Retourne une description complète du créneau
     *
     * @return Description formatée (ex: "Déjeuner (12:00 - 14:30)")
     */
    public String getDescription() {
        return libelle + " (" + heureDebut + " - " + heureFin + ")";
    }

    /**
     * Retourne la plage horaire du créneau
     *
     * @return Plage horaire formatée (ex: "12:00 - 14:30")
     */
    public String getPlageHoraire() {
        return heureDebut + " - " + heureFin;
    }

    /**
     * Vérifie si le créneau est valide (heure début < heure fin)
     *
     * @return true si le créneau est valide, false sinon
     */
    public boolean isValide() {
        if (heureDebut == null || heureFin == null) {
            return false;
        }

        try {
            // Conversion simple pour comparaison HH:MM
            String[] debut = heureDebut.split(":");
            String[] fin = heureFin.split(":");

            int heuresDebut = Integer.parseInt(debut[0]);
            int minutesDebut = Integer.parseInt(debut[1]);
            int heuresFin = Integer.parseInt(fin[0]);
            int minutesFin = Integer.parseInt(fin[1]);

            int totalMinutesDebut = heuresDebut * 60 + minutesDebut;
            int totalMinutesFin = heuresFin * 60 + minutesFin;

            return totalMinutesDebut < totalMinutesFin;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérifie si le créneau peut être utilisé pour les réservations
     *
     * @return true si le créneau est actif et valide
     */
    public boolean isDisponiblePourReservation() {
        return actif && isValide();
    }

    // ==================== MÉTHODES STANDARD ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Creneau creneau = (Creneau) obj;
        return id == creneau.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Creneau{" +
                "id=" + id +
                ", libelle='" + libelle + '\'' +
                ", heureDebut='" + heureDebut + '\'' +
                ", heureFin='" + heureFin + '\'' +
                ", actif=" + actif +
                ", ordreAffichage=" + ordreAffichage +
                '}';
    }

    /**
     * Retourne une représentation JSON-friendly du créneau
     * Utile pour les réponses API
     *
     * @return Représentation simple du créneau
     */
    public String toDisplayString() {
        return libelle + " (" + heureDebut + "-" + heureFin + ")";
    }
}