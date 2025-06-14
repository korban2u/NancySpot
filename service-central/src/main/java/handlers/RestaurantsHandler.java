package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import rmi.Serveur;
import utils.HttpUtils;

import java.io.IOException;

/**
 * Handler HTTP pour la gestion des restaurants.
 *
 * Ce handler traite les requêtes relatives aux restaurants du système.
 * Il expose l'endpoint GET /restaurants qui retourne la liste complète
 * des restaurants disponibles avec leurs informations détaillées.
 *
 * Endpoint géré :
 * - GET /restaurants : Récupère la liste de tous les restaurants
 *
 * Chaque restaurant retourné contient :
 * - Identifiant unique
 * - Nom du restaurant
 * - Adresse complète
 * - Numéro de téléphone
 * - Coordonnées géographiques (latitude, longitude)
 */
public class RestaurantsHandler implements HttpHandler {

    private final Serveur serviceCentral;

    /**
     * Constructeur du handler des restaurants.
     *
     * @param serviceCentral l'instance du service central qui fait le lien avec le service BD
     */
    public RestaurantsHandler(Serveur serviceCentral) {
        this.serviceCentral = serviceCentral;
    }

    /**
     * Traite les requêtes HTTP pour l'endpoint /restaurants.
     *
     * Cette méthode ne supporte que les requêtes GET. Elle délègue le traitement
     * aux utilitaires HTTP qui gèrent automatiquement :
     * - La validation de la méthode HTTP
     * - L'appel au service backend
     * - Le formatage de la réponse JSON
     * - La gestion des erreurs
     *
     * Réponse en cas de succès :
     * - Code HTTP 200
     * - Content-Type: application/json
     * - Corps : JSON contenant la liste des restaurants
     *
     * Réponses d'erreur possibles :
     * - 405 Method Not Allowed : Si la méthode n'est pas GET
     * - 500 Internal Server Error : Si le service BD n'est pas disponible
     *
     * @param exchange l'échange HTTP contenant la requête et permettant d'envoyer la réponse
     * @throws IOException en cas d'erreur lors de la lecture de la requête ou l'envoi de la réponse
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpUtils.handleGetRequest(exchange, "/restaurants", serviceCentral::getAllRestaurants);
    }
}