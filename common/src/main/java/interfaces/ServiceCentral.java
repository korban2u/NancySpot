package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface RMI du service central de l'application Nancy Spot.
 * Le service central fait office de registre et de coordinateur pour tous
 * les autres services de l'architecture distribuée.
 *
 * Il maintient un registre des services disponibles et permet aux autres
 * services de s'enregistrer ou se désenregistrer dynamiquement.
 *
 * @author Nancy Spot Team
 * @version 1.0
 * @since 1.0
 */
public interface ServiceCentral extends Remote {

    /**
     * Enregistre un service de base de données auprès du service central.
     * Le service BD doit être accessible via RMI et répondre au ping
     * pour que l'enregistrement soit accepté.
     *
     * @param serviceBD l'instance du service de base de données à enregistrer
     * @return true si l'enregistrement a réussi, false sinon
     * @throws RemoteException en cas d'erreur de communication RMI
     */
    boolean enregistrerServiceBD(ServiceBD serviceBD) throws RemoteException;

    /**
     * Enregistre un service proxy auprès du service central.
     * Le service proxy doit être accessible via RMI et répondre au ping
     * pour que l'enregistrement soit accepté.
     *
     * @param serviceProxy l'instance du service proxy à enregistrer
     * @return true si l'enregistrement a réussi, false sinon
     * @throws RemoteException en cas d'erreur de communication RMI
     */
    boolean enregistrerServiceProxy(ServiceProxy serviceProxy) throws RemoteException;

    /**
     * Supprime un service du registre central.
     * Permet à un service de se désenregistrer proprement ou de forcer
     * la suppression d'un service.
     *
     * @param serviceType le type de service à supprimer ("BD" ou "PROXY")
     * @return true si la suppression a réussi, false si le type est inconnu
     * @throws RemoteException en cas d'erreur de communication RMI
     */
    boolean supprimerService(String serviceType) throws RemoteException;

    /**
     * Retourne l'état de tous les services enregistrés.
     * Effectue un test de connectivité en temps réel sur chaque service
     * et retourne un rapport détaillé au format JSON.
     *
     * @return un JSON contenant l'état de disponibilité de chaque service
     * @throws RemoteException en cas d'erreur de communication RMI
     */
    String getEtatServices() throws RemoteException;
}