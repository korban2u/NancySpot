package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import rmi.Serveur;
import utils.HttpUtils;

import java.io.IOException;

/**
 * Handler HTTP pour la création de réservations.
 *
 * Ce handler traite les requêtes de réservation de tables dans les restaurants.
 * Il expose l'endpoint POST /reserver qui permet aux clients de créer
 * une nouvelle réservation avec validation automatique des données.
 *
 * Endpoint géré :
 * - POST /reserver : Crée une nouvelle réservation
 *
 *
 * @author Nancy Spot Team
 * @version 1.0
 * @since 1.0
 */
public class ReserverHandler implements HttpHandler {

    private final Serveur serviceCentral;

    /**
     * Constructeur du handler de réservation.
     *
     * @param serviceCentral l'instance du service central qui fait le lien avec le service BD
     */
    public ReserverHandler(Serveur serviceCentral) {
        this.serviceCentral = serviceCentral;
    }

    /**
     * Traite les requêtes HTTP pour l'endpoint /reserver.
     *
     * Cette méthode ne supporte que les requêtes POST contenant les données
     * de réservation au format JSON dans le corps de la requête.
     *
     * Format JSON attendu :
     * {
     *   "tableId": number,
     *   "creneauId": number,
     *   "dateReservation": "yyyy-MM-dd",
     *   "nomClient": "string",
     *   "prenomClient": "string",
     *   "telephone": "string",
     *   "nbConvives": number
     * }
     *
     * Le handler utilise un validateur automatique qui vérifie :
     * - La présence de tous les champs obligatoires
     * - Le format valide du JSON
     * - La cohérence des types de données
     *
     * Réponse en cas de succès :
     * - Code HTTP 200
     * - Content-Type: application/json
     * - Corps : JSON avec les détails de la réservation créée
     *
     * Réponses d'erreur possibles :
     * - 400 Bad Request : Données manquantes ou invalides
     * - 405 Method Not Allowed : Si la méthode n'est pas POST
     * - 409 Conflict : Table déjà réservée pour ce créneau
     * - 500 Internal Server Error : Si le service BD n'est pas disponible
     *
     * @param exchange l'échange HTTP contenant la requête et permettant d'envoyer la réponse
     * @throws IOException en cas d'erreur lors de la lecture de la requête ou l'envoi de la réponse
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpUtils.handlePostRequest(exchange, "/reserver",
                serviceCentral::reserverTable,
                new HttpUtils.ReservationValidator());
    }
}