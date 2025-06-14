package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import rmi.Serveur;
import utils.HttpUtils;

import java.io.IOException;

/**
 * Handler HTTP pour la récupération des incidents de circulation.
 *
 * Ce handler traite les requêtes relatives aux incidents de circulation
 * dans la région de Nancy. Il fait appel au service proxy qui interroge
 * les APIs externes pour obtenir des informations en temps réel.
 *
 * Endpoint géré :
 * - GET /incidents : Récupère la liste des incidents de circulation actuels
 *
 * Les incidents incluent typiquement :
 * - Accidents de la route
 * - Travaux en cours
 * - Fermetures de voies
 * - Embouteillages importants
 * - Événements perturbant la circulation
 *
 * Les données sont filtrées pour ne retourner que les incidents
 * pertinents dans les envitons de Nancy.
 *
 * @author Nancy Spot Team
 * @version 1.0
 * @since 1.0
 */
public class IncidentsHandler implements HttpHandler {

    private final Serveur serviceCentral;

    /**
     * Constructeur du handler des incidents.
     *
     * @param serviceCentral l'instance du service central qui fait le lien avec le service proxy
     */
    public IncidentsHandler(Serveur serviceCentral) {
        this.serviceCentral = serviceCentral;
    }

    /**
     * Traite les requêtes HTTP pour l'endpoint /incidents.
     *
     * Cette méthode ne supporte que les requêtes GET. Elle délègue le traitement
     * au service central qui interroge le service proxy pour récupérer
     * les données d'incidents depuis les APIs externes.
     *
     * Le service proxy gère automatiquement :
     * - La configuration réseau (proxy IUT si nécessaire)
     * - L'accès aux APIs externes de trafic
     * - Le filtrage géographique pour Nancy
     * - La mise en cache temporaire des données
     *
     * Réponse en cas de succès :
     * - Code HTTP 200
     * - Content-Type: application/json
     * - Corps : JSON contenant la liste des incidents avec détails
     *
     * Structure des incidents retournés :
     * - Type d'incident (accident, travaux, fermeture, etc.)
     * - Localisation précise
     * - Description de la perturbation
     * - Niveau de gravité ou d'impact
     * - Horaires si applicable
     *
     * Réponses d'erreur possibles :
     * - 405 Method Not Allowed : Si la méthode n'est pas GET
     * - 500 Internal Server Error : Si le service proxy n'est pas disponible
     * - 503 Service Unavailable : Si les APIs externes sont inaccessibles
     *
     * @param exchange l'échange HTTP contenant la requête et permettant d'envoyer la réponse
     * @throws IOException en cas d'erreur lors de la lecture de la requête ou l'envoi de la réponse
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpUtils.handleGetRequest(exchange, "/incidents", serviceCentral::getIncidents);
    }
}