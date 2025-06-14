package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface RMI pour le service proxy du système Nancy Spot.
 * Ce service fait office d'intermédiaire pour accéder aux APIs externes,
 * notamment pour récupérer les informations de circulation et d'incidents.
 *
 * Le service proxy gère automatiquement la configuration réseau
 * (proxy IUT si nécessaire) et fournit une interface pour les données externes.
 *
 * @author Nancy Spot Team
 * @version 1.0
 * @since 1.0
 */
public interface ServiceProxy extends Remote {

    /**
     * Récupère les incidents de circulation dans la région de Nancy.
     * Interroge les APIs externes pour obtenir les informations en temps réel
     * sur les perturbations du trafic, travaux, accidents, etc.
     *
     * Les données sont formatées et filtrées pour ne retourner que les incidents
     * pertinents pour la zone géographique de Nancy.
     *
     * @return un JSON contenant la liste des incidents de circulation avec
     *         leurs détails (localisation, type, description, niveau de gravité)
     * @throws RemoteException en cas d'erreur de communication RMI ou d'accès aux APIs externes
     */
    String getIncidents() throws RemoteException;

    /**
     * Test de connectivité du service proxy.
     * Vérifie que le service est opérationnel et peut accéder aux APIs externes.
     *
     * @return true si le service répond correctement et peut accéder aux ressources externes
     * @throws RemoteException en cas d'erreur de communication RMI
     */
    boolean ping() throws RemoteException;
}