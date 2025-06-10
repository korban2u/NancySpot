package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface RMI pour le service proxy
 * Récupère les données externes (Vélib, incidents, etc.)
 */
public interface IServiceProxy extends Remote {

    /**
     * Récupère les données des stations Vélib
     * @return JSON contenant les stations et leur état
     * @throws RemoteException en cas d'erreur RMI
     */
    String getVelibData() throws RemoteException;

    /**
     * Récupère les incidents routiers
     * @return JSON contenant la liste des incidents
     * @throws RemoteException en cas d'erreur RMI
     */
    String getIncidents() throws RemoteException;
}