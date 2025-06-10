package common.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface RMI pour le service de base de données
 * Gère les restaurants et les réservations
 */
public interface IServiceBD extends Remote {

    /**
     * Récupère tous les restaurants de la base de données
     * @return JSON contenant la liste des restaurants
     * @throws RemoteException en cas d'erreur RMI
     */
    String getAllRestaurants() throws RemoteException;

    /**
     * Effectue une réservation de table
     * @param jsonReservation JSON contenant les détails de la réservation
     * @return JSON avec le résultat de la réservation
     * @throws RemoteException en cas d'erreur RMI
     */
    String reserverTable(String jsonReservation) throws RemoteException;

    /**
     * Inscrit le service central pour les callbacks
     * @param host adresse IP du service central
     * @param port port du service central
     * @return true si inscription réussie
     * @throws RemoteException en cas d'erreur RMI
     */
    boolean inscrireServiceCentral(String host, int port) throws RemoteException;
}