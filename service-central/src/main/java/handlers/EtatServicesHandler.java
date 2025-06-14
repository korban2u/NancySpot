package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import rmi.Serveur;
import utils.HttpUtils;

import java.io.IOException;

/**
 * Handler HTTP pour la surveillance de l'état des services.
 *
 * Ce handler expose un endpoint de monitoring qui permet de vérifier
 * en temps réel l'état de tous les services backend de l'architecture
 * distribuée. Il est utilisé pour le debugging, la surveillance système
 * et les health checks automatiques.
 *
 * Endpoint géré :
 * - GET /services/etat : État en temps réel de tous les services backend
 *
 * Services surveillés :
 * - Service BD : Connectivité à la base de données Oracle
 * - Service Proxy : Accès aux APIs externes de circulation
 *
 * Chaque service est testé individuellement avec un ping RMI
 * pour vérifier sa disponibilité effective.
 */
public class EtatServicesHandler implements HttpHandler {

    private final Serveur serviceCentral;

    /**
     * Constructeur du handler d'état des services.
     *
     * @param serviceCentral l'instance du service central qui maintient le registre des services
     */
    public EtatServicesHandler(Serveur serviceCentral) {
        this.serviceCentral = serviceCentral;
    }

    /**
     * Traite les requêtes HTTP pour l'endpoint /services/etat.
     *
     * Cette méthode effectue un test de connectivité en temps réel
     * sur tous les services enregistrés auprès du service central.
     * Elle ne se contente pas de vérifier l'enregistrement mais
     * effectue un véritable test de communication RMI.
     *
     * Processus de vérification :
     * 1. Parcours de tous les services enregistrés
     * 2. Appel ping() sur chaque service via RMI
     * 3. Désenregistrement automatique des services non répondants
     * 4. Compilation d'un rapport détaillé
     *
     * Réponse en cas de succès :
     * - Code HTTP 200
     * - Content-Type: application/json
     * - Corps : JSON avec l'état détaillé de chaque service
     *
     * Structure de la réponse :
     * {
     *   "serviceBD": {
     *     "disponible": true/false
     *   },
     *   "serviceProxy": {
     *     "disponible": true/false
     *   },
     *   "timestamp": 1234567890123
     * }
     *
     * Comportement en cas de service défaillant :
     * - Le service est automatiquement désenregistré
     * - Son statut passe à "disponible": false
     * - Un message de warning est loggé
     *
     * Ce endpoint ne retourne jamais d'erreur 500, même si tous
     * les services sont défaillants. Il retourne toujours un rapport
     * d'état, permettant au monitoring de distinguer une panne
     * du service central d'une panne des services backend.
     *
     * @param exchange l'échange HTTP contenant la requête et permettant d'envoyer la réponse
     * @throws IOException en cas d'erreur lors de la lecture de la requête ou l'envoi de la réponse
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpUtils.handleGetRequest(exchange, "/services/etat", serviceCentral::getEtatServices);
    }
}