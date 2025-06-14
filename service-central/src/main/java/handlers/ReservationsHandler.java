package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import rmi.Serveur;
import utils.HttpUtils;

import java.io.IOException;

/**
 * Handler HTTP pour la gestion avancée des réservations.
 *
 * Ce handler traite les requêtes relatives à la consultation et la gestion
 * des réservations existantes. Il complète le ReserverHandler en proposant
 * des fonctionnalités de suivi, consultation et annulation des réservations.
 *
 * Endpoints gérés :
 * - GET /reservations/date/{restaurantId}/{date} : Réservations d'une date
 * - POST /reservations/annuler/{reservationId} : Annulation d'une réservation
 *
 * Ces endpoints sont particulièrement utiles pour :
 * - Les interfaces d'administration des restaurants
 * - Le suivi des réservations par les clients
 * - La gestion des plannings par date
 * - L'annulation de réservations existantes
 *
 * @author Nancy Spot Team
 * @version 1.0
 * @since 2.0
 */
public class ReservationsHandler implements HttpHandler {

    private final Serveur serviceCentral;

    /**
     * Constructeur du handler de gestion des réservations.
     *
     * @param serviceCentral l'instance du service central qui fait le lien avec le service BD
     */
    public ReservationsHandler(Serveur serviceCentral) {
        this.serviceCentral = serviceCentral;
    }

    /**
     * Traite les requêtes HTTP pour les endpoints /reservations/*.
     *
     * Cette méthode route les requêtes selon le pattern d'URL :
     * - /reservations/date/{restaurantId}/{date} : Consultation par date
     * - /reservations/annuler/{reservationId} : Annulation d'une réservation
     *
     * Le routage est basé sur l'analyse du chemin d'URL pour déterminer
     * l'action demandée et extraire les paramètres nécessaires.
     *
     * @param exchange l'échange HTTP contenant la requête et permettant d'envoyer la réponse
     * @throws IOException en cas d'erreur lors de la lecture de la requête ou l'envoi de la réponse
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (path.startsWith("/reservations/date/")) {
            handleReservationsDate(exchange);
        } else if (path.startsWith("/reservations/annuler/")) {
            handleAnnulerReservation(exchange);
        } else {
            HttpUtils.sendError(exchange, 404, "Endpoint non trouvé");
        }
    }

    /**
     * Gère l'endpoint GET /reservations/date/{restaurantId}/{date}.
     *
     * Récupère toutes les réservations confirmées pour un restaurant
     * et une date donnés. Les résultats incluent les détails complets
     * des réservations avec les informations des créneaux et tables.
     *
     * Format d'URL : /reservations/date/{restaurantId}/{date}
     * - restaurantId : Identifiant numérique du restaurant
     * - date : Date au format yyyy-MM-dd
     *
     * Exemple : /reservations/date/1/2024-12-25
     *
     * Réponse en cas de succès :
     * - Code HTTP 200
     * - Content-Type: application/json
     * - Corps : JSON avec la liste des réservations
     *
     * Structure de réponse :
     * {
     *   "reservations": [
     *     {
     *       "id": 123,
     *       "tableId": 5,
     *       "creneauId": 1,
     *       "nomClient": "Dupont",
     *       "prenomClient": "Jean",
     *       "telephone": "0383...",
     *       "nbConvives": 4,
     *       "statut": "confirmee",
     *       "creneau": { "libelle": "Déjeuner", ... },
     *       "table": { "numeroTable": 12, ... }
     *     }
     *   ],
     *   "date": "2024-12-25",
     *   "restaurantId": 1
     * }
     *
     * Réponses d'erreur possibles :
     * - 400 Bad Request : Format d'URL invalide ou ID restaurant non numérique
     * - 500 Internal Server Error : Service BD indisponible
     *
     * @param exchange l'échange HTTP en cours de traitement
     * @throws IOException en cas d'erreur lors du traitement
     */
    private void handleReservationsDate(HttpExchange exchange) throws IOException {
        try {
            String[] pathParts = extractPathParts(exchange.getRequestURI().getPath(), "/reservations/date/");
            if (pathParts.length != 2) {
                HttpUtils.sendError(exchange, 400, "Format: /reservations/date/{restaurantId}/{date}");
                return;
            }

            int restaurantId = Integer.parseInt(pathParts[0]);
            String date = pathParts[1];

            HttpUtils.handleGetRequest(exchange, "/reservations/date",
                    () -> serviceCentral.getReservationsPourDate(restaurantId, date));

        } catch (NumberFormatException e) {
            HttpUtils.sendError(exchange, 400, "ID restaurant doit être un nombre");
        }
    }

    /**
     * Gère l'endpoint POST /reservations/annuler/{reservationId}. On a pas eu le temps de l'implementer... :(
     *
     * Annule une réservation existante en changeant son statut à "annulee"
     * et en libérant la table pour le créneau concerné. L'annulation est
     * définitive et ne peut pas être annulée.
     *
     * Format d'URL : /reservations/annuler/{reservationId}
     * - reservationId : Identifiant numérique de la réservation à annuler
     *
     * Exemple : /reservations/annuler/123
     *
     * Méthode HTTP requise : POST (pour éviter les annulations accidentelles)
     *
     * Réponse en cas de succès :
     * - Code HTTP 200
     * - Content-Type: application/json
     * - Corps : JSON confirmant l'annulation
     *
     * Structure de réponse :
     * {
     *   "success": true,
     *   "message": "Réservation annulée avec succès",
     *   "reservationId": 123,
     *   "timestamp": 1234567890123
     * }
     *
     * Réponses d'erreur possibles :
     * - 400 Bad Request : Format d'URL invalide ou ID non numérique
     * - 404 Not Found : Réservation inexistante
     * - 405 Method Not Allowed : Si la méthode n'est pas POST
     * - 409 Conflict : Réservation déjà annulée
     * - 500 Internal Server Error : Service BD indisponible
     *
     * @param exchange l'échange HTTP en cours de traitement
     * @throws IOException en cas d'erreur lors du traitement
     */
    private void handleAnnulerReservation(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtils.sendError(exchange, 405, "Méthode POST requise");
            return;
        }

        try {
            String[] pathParts = extractPathParts(exchange.getRequestURI().getPath(), "/reservations/annuler/");
            if (pathParts.length != 1) {
                HttpUtils.sendError(exchange, 400, "Format: /reservations/annuler/{reservationId}");
                return;
            }

            int reservationId = Integer.parseInt(pathParts[0]);

            HttpUtils.handleGetRequest(exchange, "/reservations/annuler",
                    () -> serviceCentral.annulerReservation(reservationId));

        } catch (NumberFormatException e) {
            HttpUtils.sendError(exchange, 400, "ID réservation doit être un nombre");
        }
    }

    /**
     * Extrait les parties du chemin d'URL après un préfixe donné.
     *
     * Utilitaire pour parser les URLs paramétrées et extraire les valeurs
     * des paramètres d'URL de manière propre.
     *
     * @param path le chemin complet de l'URL
     * @param prefix le préfixe à retirer du chemin
     * @return un tableau contenant les parties du chemin après le préfixe
     */
    private String[] extractPathParts(String path, String prefix) {
        String remaining = path.substring(prefix.length());
        return remaining.split("/");
    }
}