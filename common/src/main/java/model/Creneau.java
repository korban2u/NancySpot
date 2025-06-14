package model;

import java.io.Serializable;

/**
 * Représente un créneau horaire pour les réservations de restaurant.
 * Un créneau définit une plage horaire pendant laquelle les réservations sont possibles,
 * comme le déjeuner ou le dîner.
 *
 * @author Nancy Spot Team
 * @version 1.0
 * @since 1.0
 */
public class Creneau implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String libelle;
    private String heureDebut;
    private String heureFin;
    private boolean actif;
    private int ordreAffichage;

    /**
     * Constructeur par défaut.
     * Crée un créneau actif avec un ordre d'affichage de 1.
     */
    public Creneau() {
        this.actif = true;
        this.ordreAffichage = 1;
    }

    /**
     * Constructeur complet pour créer un créneau avec tous ses paramètres.
     *
     * @param id l'identifiant unique du créneau
     * @param libelle le nom du créneau (ex: "Déjeuner", "Dîner")
     * @param heureDebut l'heure de début au format HH:MM
     * @param heureFin l'heure de fin au format HH:MM
     * @param actif indique si le créneau est actif
     * @param ordreAffichage l'ordre d'affichage dans l'interface
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
     * Constructeur simplifié pour créer un créneau actif.
     *
     * @param libelle le nom du créneau
     * @param heureDebut l'heure de début au format HH:MM
     * @param heureFin l'heure de fin au format HH:MM
     */
    public Creneau(String libelle, String heureDebut, String heureFin) {
        this.libelle = libelle;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.actif = true;
        this.ordreAffichage = 1;
    }

    /**
     * Retourne l'identifiant unique du créneau.
     *
     * @return l'identifiant du créneau
     */
    public int getId() {
        return id;
    }

    /**
     * Définit l'identifiant unique du créneau.
     *
     * @param id l'identifiant à assigner au créneau
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Retourne le libellé du créneau.
     *
     * @return le libellé du créneau
     */
    public String getLibelle() {
        return libelle;
    }

    /**
     * Définit le libellé du créneau.
     *
     * @param libelle le libellé à assigner au créneau
     */
    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    /**
     * Retourne l'heure de début du créneau.
     *
     * @return l'heure de début au format HH:MM
     */
    public String getHeureDebut() {
        return heureDebut;
    }

    /**
     * Définit l'heure de début du créneau.
     *
     * @param heureDebut l'heure de début au format HH:MM
     */
    public void setHeureDebut(String heureDebut) {
        this.heureDebut = heureDebut;
    }

    /**
     * Retourne l'heure de fin du créneau.
     *
     * @return l'heure de fin au format HH:MM
     */
    public String getHeureFin() {
        return heureFin;
    }

    /**
     * Définit l'heure de fin du créneau.
     *
     * @param heureFin l'heure de fin au format HH:MM
     */
    public void setHeureFin(String heureFin) {
        this.heureFin = heureFin;
    }

    /**
     * Indique si le créneau est actif.
     *
     * @return true si le créneau est actif, false sinon
     */
    public boolean isActif() {
        return actif;
    }

    /**
     * Définit si le créneau est actif.
     *
     * @param actif true pour activer le créneau, false pour le désactiver
     */
    public void setActif(boolean actif) {
        this.actif = actif;
    }

    /**
     * Retourne l'ordre d'affichage du créneau.
     *
     * @return l'ordre d'affichage
     */
    public int getOrdreAffichage() {
        return ordreAffichage;
    }

    /**
     * Définit l'ordre d'affichage du créneau.
     *
     * @param ordreAffichage l'ordre d'affichage à assigner
     */
    public void setOrdreAffichage(int ordreAffichage) {
        this.ordreAffichage = ordreAffichage;
    }

    /**
     * Retourne une description complète du créneau.
     *
     * @return une description formatée (ex: "Déjeuner (12:00 - 14:30)")
     */
    public String getDescription() {
        return libelle + " (" + heureDebut + " - " + heureFin + ")";
    }

    /**
     * Retourne la plage horaire du créneau.
     *
     * @return la plage horaire formatée (ex: "12:00 - 14:30")
     */
    public String getPlageHoraire() {
        return heureDebut + " - " + heureFin;
    }

    /**
     * Vérifie si le créneau est valide (heure début antérieure à heure fin).
     *
     * @return true si le créneau est valide, false sinon
     */
    public boolean isValide() {
        if (heureDebut == null || heureFin == null) {
            return false;
        }

        try {
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
     * Vérifie si le créneau peut être utilisé pour les réservations.
     *
     * @return true si le créneau est actif et valide
     */
    public boolean isDisponiblePourReservation() {
        return actif && isValide();
    }

    /**
     * Compare deux créneaux pour l'égalité basée sur l'identifiant.
     *
     * @param obj l'objet à comparer
     * @return true si les objets sont égaux, false sinon
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Creneau creneau = (Creneau) obj;
        return id == creneau.id;
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
     * Retourne une représentation textuelle du créneau.
     *
     * @return une chaîne de caractères décrivant le créneau
     */
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
     * Retourne une représentation simple du créneau pour l'affichage.
     *
     * @return une représentation simple du créneau
     */
    public String toDisplayString() {
        return libelle + " (" + heureDebut + "-" + heureFin + ")";
    }
}