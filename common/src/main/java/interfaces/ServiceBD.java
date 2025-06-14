package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface RMI pour le service de base de données
 * Version améliorée avec support des créneaux horaires
 *
 * @author Nancy Spot Team
 * @version 2.0 - Avec gestion des créneaux
 */
public interface ServiceBD extends Remote {

    // ==================== MÉTHODES RESTAURANTS (inchangées) ====================

    /**
     * Récupère la liste complète des restaurants
     *
     * @return JSON contenant la liste des restaurants
     * @throws RemoteException En cas d'erreur RMI
     */
    String getAllRestaurants() throws RemoteException;

    // ==================== MÉTHODES CRÉNEAUX (nouvelles) ====================

    /**
     * Récupère la liste des créneaux horaires disponibles
     * Retourne uniquement les créneaux actifs, triés par ordre d'affichage
     *
     * @return JSON contenant la liste des créneaux actifs
     * @throws RemoteException En cas d'erreur RMI
     */
    String getCreneauxDisponibles() throws RemoteException;

    /**
     * Récupère un créneau spécifique par son ID
     *
     * @param creneauId Identifiant du créneau
     * @return JSON contenant les données du créneau ou erreur si non trouvé
     * @throws RemoteException En cas d'erreur RMI
     */
    String getCreneauById(int creneauId) throws RemoteException;

    // ==================== MÉTHODES TABLES (améliorées avec créneaux) ====================

    /**
     * Récupère les tables libres pour un restaurant donné
     * Version sans créneau (compatibilité descendante)
     *
     * @param restaurantId Identifiant du restaurant
     * @return JSON contenant la liste des tables
     * @throws RemoteException En cas d'erreur RMI
     * @deprecated Utiliser getTablesLibresPourCreneau() à la place
     */
    @Deprecated
    String getTablesLibres(int restaurantId) throws RemoteException;

    /**
     * Récupère les tables libres pour un restaurant, une date et un créneau donnés
     * Cette méthode remplace getTablesLibres() pour la gestion des créneaux
     *
     * @param restaurantId Identifiant du restaurant
     * @param dateReservation Date de réservation au format "yyyy-MM-dd"
     * @param creneauId Identifiant du créneau
     * @return JSON contenant la liste des tables disponibles pour ce créneau
     * @throws RemoteException En cas d'erreur RMI
     */
    String getTablesLibresPourCreneau(int restaurantId, String dateReservation, int creneauId) throws RemoteException;

    /**
     * Récupère toutes les tables d'un restaurant avec leur statut pour une date et un créneau
     * Permet d'afficher toutes les tables avec indication de disponibilité
     *
     * @param restaurantId Identifiant du restaurant
     * @param dateReservation Date de réservation au format "yyyy-MM-dd"
     * @param creneauId Identifiant du créneau
     * @return JSON contenant toutes les tables avec leur statut (libre/occupee)
     * @throws RemoteException En cas d'erreur RMI
     */
    String getTablesAvecStatut(int restaurantId, String dateReservation, int creneauId) throws RemoteException;

    // ==================== MÉTHODES RÉSERVATIONS (améliorées) ====================

    /**
     * Effectue une réservation de table pour un créneau spécifique
     * Version améliorée avec gestion des créneaux
     *
     * @param jsonReservation JSON contenant les données de réservation
     *                       Doit inclure: tableId, creneauId, dateReservation,
     *                       nomClient, prenomClient, telephone, nbConvives
     * @return JSON contenant le résultat de la réservation
     * @throws RemoteException En cas d'erreur RMI
     */
    String reserverTable(String jsonReservation) throws RemoteException;

    /**
     * Vérifie la disponibilité d'une table pour un créneau et une date donnés
     *
     * @param tableId Identifiant de la table
     * @param dateReservation Date de réservation au format "yyyy-MM-dd"
     * @param creneauId Identifiant du créneau
     * @return JSON contenant le statut de disponibilité
     * @throws RemoteException En cas d'erreur RMI
     */
    String verifierDisponibilite(int tableId, String dateReservation, int creneauId) throws RemoteException;

    /**
     * Récupère les réservations d'un restaurant pour une date donnée
     * Utile pour l'administration et la visualisation des plannings
     *
     * @param restaurantId Identifiant du restaurant
     * @param dateReservation Date au format "yyyy-MM-dd"
     * @return JSON contenant la liste des réservations
     * @throws RemoteException En cas d'erreur RMI
     */
    String getReservationsPourDate(int restaurantId, String dateReservation) throws RemoteException;

    /**
     * Annule une réservation existante
     *
     * @param reservationId Identifiant de la réservation à annuler
     * @return JSON contenant le résultat de l'annulation
     * @throws RemoteException En cas d'erreur RMI
     */
    String annulerReservation(int reservationId) throws RemoteException;

    // ==================== MÉTHODES STATISTIQUES (nouvelles) ====================

    /**
     * Récupère les statistiques de réservation pour un restaurant
     *
     * @param restaurantId Identifiant du restaurant
     * @param dateDebut Date de début de la période au format "yyyy-MM-dd"
     * @param dateFin Date de fin de la période au format "yyyy-MM-dd"
     * @return JSON contenant les statistiques (nb réservations par créneau, taux occupation, etc.)
     * @throws RemoteException En cas d'erreur RMI
     */
    String getStatistiquesReservations(int restaurantId, String dateDebut, String dateFin) throws RemoteException;

    /**
     * Récupère le planning complet d'un restaurant pour une période donnée
     *
     * @param restaurantId Identifiant du restaurant
     * @param dateDebut Date de début au format "yyyy-MM-dd"
     * @param dateFin Date de fin au format "yyyy-MM-dd"
     * @return JSON contenant le planning détaillé par date et créneau
     * @throws RemoteException En cas d'erreur RMI
     */
    String getPlanningRestaurant(int restaurantId, String dateDebut, String dateFin) throws RemoteException;

    // ==================== MÉTHODES SYSTÈME (inchangées) ====================

    /**
     * Test de connectivité du service
     *
     * @return true si le service est opérationnel
     * @throws RemoteException En cas d'erreur RMI
     */
    boolean ping() throws RemoteException;
}