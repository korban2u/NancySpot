package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import rmi.Serveur;
import utils.HttpUtils;

import java.io.IOException;

/**
 * Handler HTTP pour la gestion des créneaux horaires.
 *
 * Ce handler traite les requêtes relatives aux créneaux horaires du système
 * de réservation. Il expose deux endpoints pour consulter les créneaux
 * disponibles, soit en liste complète soit individuellement.
 *
 * Endpoints gérés :
 * - GET /creneaux : Liste de tous les créneaux horaires actifs
 * - GET /creneaux/{id} : Détails d'un créneau spécifique
 *
 * Les créneaux permettent de définir des plages horaires pour les réservations
 * (exemple : "Déjeuner 12:00-14:30", "Dîner 19:00-22:00").
 *
 * @author Nancy Spot Team
 * @version 1.0
 * @since 2.0
 */
public class CreneauxHandler implements HttpHandler {

    private final Serveur serviceCentral;

    /**
     * Constructeur du handler des créneaux.
     *
     * @param serviceCentral l'instance du service central qui fait le lien avec le service BD
     */
    public CreneauxHandler(Serveur serviceCentral) {
        this.serviceCentral = serviceCentral;
    }

    /**
     * Traite les requêtes HTTP pour les endpoints /creneaux.
     *
     * Cette méthode route les requêtes selon le pattern d'URL :
     * - /creneaux : Retourne la liste complète des créneaux actifs
     * - /creneaux/{id} : Retourne les détails du créneau spécifié
     *
     * Seules les requêtes GET sont supportées. Les créneaux inactifs
     * ne sont pas retournés dans les listes.
     *
     * Réponses en cas de succès :
     * - Code HTTP 200
     * - Content-Type: application/json
     * - Corps : JSON contenant les créneaux demandés
     *
     * Réponses d'erreur possibles :
     * - 400 Bad Request : ID de créneau invalide
     * - 404 Not Found : Endpoint ou créneau non trouvé
     * - 405 Method Not Allowed : Si la méthode n'est pas GET
     * - 500 Internal Server Error : Si le service BD n'est pas disponible
     *
     * @param exchange l'échange HTTP contenant la requête et permettant d'envoyer la réponse
     * @throws IOException en cas d'erreur lors de la lecture de la requête ou l'envoi de la réponse
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (path.equals("/creneaux")) {
            // GET /creneaux - Liste de tous les créneaux
            HttpUtils.handleGetRequest(exchange, "/creneaux", serviceCentral::getCreneauxDisponibles);
        } else if (path.startsWith("/creneaux/")) {
            // GET /creneaux/{id} - Créneau spécifique
            int creneauId = extractCreneauId(exchange);
            if (creneauId > 0) {
                HttpUtils.handleGetRequest(exchange, "/creneaux/" + creneauId,
                        () -> serviceCentral.getCreneauById(creneauId));
            } else {
                HttpUtils.sendError(exchange, 400, "ID de créneau invalide");
            }
        } else {
            HttpUtils.sendError(exchange, 404, "Endpoint non trouvé");
        }
    }

    /**
     * Extrait l'identifiant du créneau depuis l'URL.
     *
     * Parse l'URL pour récupérer l'ID numérique du créneau
     * à partir du pattern /creneaux/{id}.
     *
     * @param exchange l'échange HTTP contenant l'URL à parser
     * @return l'identifiant du créneau ou -1 si invalide
     */
    private int extractCreneauId(HttpExchange exchange) {
        try {
            String path = exchange.getRequestURI().getPath();
            String idStr = path.substring("/creneaux/".length());
            return Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}