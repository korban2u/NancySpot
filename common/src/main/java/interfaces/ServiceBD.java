package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface RMI pour le service de base de données du système Nancy Spot.
 * Ce service gère toutes les opérations de persistance des données,
 * incluant les restaurants, tables, créneaux horaires et réservations.
 */
public interface ServiceBD extends Remote {

    /**
     * Récupère la liste complète des restaurants.
     * Retourne tous les restaurants avec leurs informations de base
     * incluant nom, adresse, téléphone et coordonnées géographiques.
     *
     * @return un JSON contenant la liste des restaurants
     * @throws RemoteException en cas d'erreur de communication RMI
     */
    String getAllRestaurants() throws RemoteException;

    /**
     * Récupère la liste des créneaux horaires disponibles.
     * Retourne uniquement les créneaux actifs, triés par ordre d'affichage.
     * Chaque créneau contient son libellé et sa plage horaire.
     *
     * @return un JSON contenant la liste des créneaux actifs
     * @throws RemoteException en cas d'erreur de communication RMI
     */
    String getCreneauxDisponibles() throws RemoteException;

    /**
     * Récupère un créneau spécifique par son identifiant.
     *
     * @param creneauId l'identifiant du créneau recherché
     * @return un JSON contenant les données du créneau ou une erreur si non trouvé
     * @throws RemoteException en cas d'erreur de communication RMI
     */
    String getCreneauById(int creneauId) throws RemoteException;

    /**
     * Récupère les tables libres pour un restaurant donné.
     * Version sans créneau maintenue pour compatibilité descendante.
     *
     * @param restaurantId l'identifiant du restaurant
     * @return un JSON contenant la liste des tables
     * @throws RemoteException en cas d'erreur de communication RMI
     * @deprecated Utiliser getTablesLibresPourCreneau() à la place
     */
    @Deprecated
    String getTablesLibres(int restaurantId) throws RemoteException;

    /**
     * Récupère les tables libres pour un restaurant, une date et un créneau donnés.
     * Cette méthode remplace getTablesLibres() pour la gestion des créneaux.
     * Vérifie la disponibilité en temps réel pour le créneau spécifié.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param dateReservation la date de réservation au format "yyyy-MM-dd"
     * @param creneauId l'identifiant du créneau
     * @return un JSON contenant la liste des tables disponibles pour ce créneau
     * @throws RemoteException en cas d'erreur de communication RMI
     */
    String getTablesLibresPourCreneau(int restaurantId, String dateReservation, int creneauId) throws RemoteException;

    /**
     * Récupère toutes les tables d'un restaurant avec leur statut pour une date et un créneau.
     * Permet d'afficher toutes les tables avec indication de disponibilité,
     * utile pour les interfaces d'administration.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param dateReservation la date de réservation au format "yyyy-MM-dd"
     * @param creneauId l'identifiant du créneau
     * @return un JSON contenant toutes les tables avec leur statut (libre/occupee)
     * @throws RemoteException en cas d'erreur de communication RMI
     */
    String getTablesAvecStatut(int restaurantId, String dateReservation, int creneauId) throws RemoteException;

    /**
     * Effectue une réservation de table pour un créneau spécifique.
     * Vérifie la disponibilité avant de créer la réservation.
     *
     * @param jsonReservation un JSON contenant les données de réservation :
     *                       tableId, creneauId, dateReservation,
     *                       nomClient, prenomClient, telephone, nbConvives
     * @return un JSON contenant le résultat de la réservation (succès ou erreur)
     * @throws RemoteException en cas d'erreur de communication RMI
     */
    String reserverTable(String jsonReservation) throws RemoteException;

    /**
     * Vérifie la disponibilité d'une table pour un créneau et une date donnés.
     * Permet de valider une réservation avant de la confirmer.
     *
     * @param tableId l'identifiant de la table
     * @param dateReservation la date de réservation au format "yyyy-MM-dd"
     * @param creneauId l'identifiant du créneau
     * @return un JSON contenant le statut de disponibilité
     * @throws RemoteException en cas d'erreur de communication RMI
     */
    String verifierDisponibilite(int tableId, String dateReservation, int creneauId) throws RemoteException;

    /**
     * Récupère les réservations d'un restaurant pour une date donnée.
     * Utile pour l'administration et la visualisation des plannings.
     * Inclut toutes les réservations confirmées et leurs détails.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param dateReservation la date au format "yyyy-MM-dd"
     * @return un JSON contenant la liste des réservations avec détails complets
     * @throws RemoteException en cas d'erreur de communication RMI
     */
    String getReservationsPourDate(int restaurantId, String dateReservation) throws RemoteException;

    /**
     * Annule une réservation existante.
     * Change le statut de la réservation à "annulee" et libère la table.
     *
     * @param reservationId l'identifiant de la réservation à annuler
     * @return un JSON contenant le résultat de l'annulation
     * @throws RemoteException en cas d'erreur de communication RMI
     */
    String annulerReservation(int reservationId) throws RemoteException;

    /**
     * Récupère les statistiques de réservation pour un restaurant.
     * Fournit des métriques sur les réservations par créneau, taux d'occupation,
     * et autres indicateurs de performance sur une période donnée.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param dateDebut la date de début de la période au format "yyyy-MM-dd"
     * @param dateFin la date de fin de la période au format "yyyy-MM-dd"
     * @return un JSON contenant les statistiques détaillées
     * @throws RemoteException en cas d'erreur de communication RMI
     */
    String getStatistiquesReservations(int restaurantId, String dateDebut, String dateFin) throws RemoteException;

    /**
     * Récupère le planning complet d'un restaurant pour une période donnée.
     * Retourne un planning détaillé par date et créneau avec toutes les réservations.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param dateDebut la date de début au format "yyyy-MM-dd"
     * @param dateFin la date de fin au format "yyyy-MM-dd"
     * @return un JSON contenant le planning détaillé
     * @throws RemoteException en cas d'erreur de communication RMI
     */
    String getPlanningRestaurant(int restaurantId, String dateDebut, String dateFin) throws RemoteException;

    /**
     * Test de connectivité du service de base de données.
     * Permet de vérifier que le service est opérationnel et accessible.
     *
     * @return true si le service répond correctement
     * @throws RemoteException en cas d'erreur de communication RMI
     */
    boolean ping() throws RemoteException;
}