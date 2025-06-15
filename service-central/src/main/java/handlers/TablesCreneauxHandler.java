package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import rmi.Serveur;
import utils.HttpUtils;

import java.io.IOException;

/**
 * Handler HTTP pour la gestion des tables avec support des créneaux horaires.
 *
 * Ce handler traite les requêtes en rapport avec la disponibilité des tables
 * des créneaux pour une gestion fine des réservations.
 *
 * Endpoints gérés :
 * - GET /tables/libres/{restaurantId}/{date}/{creneauId} : Tables libres pour un créneau
 * - GET /tables/statut/{restaurantId}/{date}/{creneauId} : Statut de toutes les tables
 * - GET /tables/disponibilite/{tableId}/{date}/{creneauId} : Vérifier une table spécifique
 */
public class TablesCreneauxHandler implements HttpHandler {

    private final Serveur serviceCentral;

    /**
     * Constructeur du handler des tables avec créneaux.
     *
     * @param serviceCentral l'instance du service central qui fait le lien avec le service BD
     */
    public TablesCreneauxHandler(Serveur serviceCentral) {
        this.serviceCentral = serviceCentral;
    }

    /**
     * Traite les requêtes HTTP pour les endpoints /tables/*.
     *
     * Cette méthode route les requêtes selon le pattern d'URL pour supporter
     * différents types de consultation des tables :
     * - Tables libres pour un créneau spécifique
     * - Statut complet de toutes les tables
     * - Vérification de disponibilité d'une table
     * - Endpoints legacy pour la compatibilité
     *
     *
     * @param exchange l'échange HTTP contenant la requête et permettant d'envoyer la réponse
     * @throws IOException en cas d'erreur lors de la lecture de la requête ou l'envoi de la réponse
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (path.startsWith("/tables/libres/")) {
            handleTablesLibresCreneau(exchange);
        } else if (path.startsWith("/tables/statut/")) {
            handleTablesStatut(exchange);
        } else if (path.startsWith("/tables/disponibilite/")) {
            handleVerifierDisponibilite(exchange);
        } else {
            HttpUtils.sendError(exchange, 404, "Endpoint non trouvé");
        }
    }

    /**
     * Gère l'endpoint GET /tables/libres/{restaurantId}/{date}/{creneauId}.
     *
     * Récupère uniquement les tables disponibles (libres) pour un restaurant,
     * une date et un créneau spécifiques. Cette méthode est optimisée pour
     * les interfaces de réservation où seules les tables disponibles importent.
     *
     * Format d'URL : /tables/libres/{restaurantId}/{date}/{creneauId}
     * - restaurantId : Identifiant numérique du restaurant
     * - date : Date au format yyyy-MM-dd
     * - creneauId : Identifiant numérique du créneau horaire
     *
     * Exemple : /tables/libres/1/2025-12-25/2
     *
     * Filtrage appliqué :
     * - Tables sans réservation confirmée pour le créneau/date
     * - Tables ayant une capacité suffisante (selon le contexte)
     * - Tables en statut "libre" dans la base
     *
     * Réponse en cas de succès :
     * - Code HTTP 200
     * - Content-Type: application/json
     * - Corps : JSON avec les tables disponibles
     *
     * Structure de réponse :
     * {
     *   "tables": [
     *     {
     *       "id": 15,
     *       "numeroTable": 5,
     *       "nbPlaces": 4,
     *       "statut": "libre"
     *     }
     *   ],
     *   "restaurantId": 1,
     *   "date": "2025-12-25",
     *   "creneauId": 2,
     *   "disponibles": 3
     * }
     *
     * @param exchange l'échange HTTP en cours de traitement
     * @throws IOException en cas d'erreur lors du traitement
     */
    private void handleTablesLibresCreneau(HttpExchange exchange) throws IOException {
        try {
            String[] pathParts = extractPathParts(exchange.getRequestURI().getPath(), "/tables/libres/");
            if (pathParts.length != 3) {
                HttpUtils.sendError(exchange, 400, "Format: /tables/libres/{restaurantId}/{date}/{creneauId}");
                return;
            }

            int restaurantId = Integer.parseInt(pathParts[0]);
            String date = pathParts[1];
            int creneauId = Integer.parseInt(pathParts[2]);

            HttpUtils.handleGetRequest(exchange, "/tables/libres",
                    () -> serviceCentral.getTablesLibresPourCreneau(restaurantId, date, creneauId));

        } catch (NumberFormatException e) {
            HttpUtils.sendError(exchange, 400, "IDs restaurant et créneau doivent être des nombres");
        }
    }

    /**
     * Gère l'endpoint GET /tables/statut/{restaurantId}/{date}/{creneauId}.
     *
     * Récupère toutes les tables d'un restaurant avec leur statut de disponibilité
     * pour une date et un créneau donnés. Contrairement à l'endpoint /libres,
     * celui-ci retourne toutes les tables avec indication de leur disponibilité.
     *
     * Format d'URL : /tables/statut/{restaurantId}/{date}/{creneauId}
     * - restaurantId : Identifiant numérique du restaurant
     * - date : Date au format yyyy-MM-dd
     * - creneauId : Identifiant numérique du créneau horaire
     *
     * Exemple : /tables/statut/1/2025-12-25/2
     *
     * Informations fournies :
     * - Toutes les tables du restaurant
     * - Statut de disponibilité pour le créneau/date
     * - Détails des réservations existantes (anonymisées)
     * - Capacité et numéro de chaque table
     *
     *
     * Réponse en cas de succès :
     * - Code HTTP 200
     * - Content-Type: application/json
     * - Corps : JSON avec toutes les tables et leur statut
     *
     * Structure de réponse :
     * {
     *   "tables": [
     *     {
     *       "id": 15,
     *       "numeroTable": 5,
     *       "nbPlaces": 4,
     *       "statut": "libre"
     *     },
     *     {
     *       "id": 16,
     *       "numeroTable": 6,
     *       "nbPlaces": 2,
     *       "statut": "occupee",
     *       "reservationInfo": {
     *         "heureReservation": "12:00",
     *         "nbConvives": 2
     *       }
     *     }
     *   ],
     *   "restaurantId": 1,
     *   "date": "2025-12-25",
     *   "creneauId": 2,
     *   "totalTables": 15,
     *   "tablesLibres": 12,
     *   "tablesOccupees": 3
     * }
     *
     * @param exchange l'échange HTTP en cours de traitement
     * @throws IOException en cas d'erreur lors du traitement
     */
    private void handleTablesStatut(HttpExchange exchange) throws IOException {
        try {
            String[] pathParts = extractPathParts(exchange.getRequestURI().getPath(), "/tables/statut/");
            if (pathParts.length != 3) {
                HttpUtils.sendError(exchange, 400, "Format: /tables/statut/{restaurantId}/{date}/{creneauId}");
                return;
            }

            int restaurantId = Integer.parseInt(pathParts[0]);
            String date = pathParts[1];
            int creneauId = Integer.parseInt(pathParts[2]);

            HttpUtils.handleGetRequest(exchange, "/tables/statut",
                    () -> serviceCentral.getTablesAvecStatut(restaurantId, date, creneauId));

        } catch (NumberFormatException e) {
            HttpUtils.sendError(exchange, 400, "IDs restaurant et créneau doivent être des nombres");
        }
    }

    /**
     * Gère l'endpoint GET /tables/disponibilite/{tableId}/{date}/{creneauId}.
     *
     * Vérifie la disponibilité d'une table spécifique pour un créneau et une date.
     * Cette méthode est utilisée pour valider une réservation avant confirmation
     * ou pour vérifier l'état d'une table particulière.
     *
     * Format d'URL : /tables/disponibilite/{tableId}/{date}/{creneauId}
     * - tableId : Identifiant numérique de la table à vérifier
     * - date : Date au format yyyy-MM-dd
     * - creneauId : Identifiant numérique du créneau horaire
     *
     * Exemple : /tables/disponibilite/15/2025-12-25/2
     *
     * Réponse en cas de succès :
     * - Code HTTP 200
     * - Content-Type: application/json
     * - Corps : JSON avec le statut de disponibilité
     *
     * Structure de réponse :
     * {
     *   "disponible": true,
     *   "tableId": 15,
     *   "date": "2025-12-25",
     *   "creneauId": 2,
     *   "table": {
     *     "numeroTable": 5,
     *     "nbPlaces": 4,
     *     "statut": "libre"
     *   },
     *   "creneau": {
     *     "libelle": "Déjeuner",
     *     "heureDebut": "12:00",
     *     "heureFin": "14:30"
     *   }
     * }
     *
     * @param exchange l'échange HTTP en cours de traitement
     * @throws IOException en cas d'erreur lors du traitement
     */
    private void handleVerifierDisponibilite(HttpExchange exchange) throws IOException {
        try {
            String[] pathParts = extractPathParts(exchange.getRequestURI().getPath(), "/tables/disponibilite/");
            if (pathParts.length != 3) {
                HttpUtils.sendError(exchange, 400, "Format: /tables/disponibilite/{tableId}/{date}/{creneauId}");
                return;
            }

            int tableId = Integer.parseInt(pathParts[0]);
            String date = pathParts[1];
            int creneauId = Integer.parseInt(pathParts[2]);

            HttpUtils.handleGetRequest(exchange, "/tables/disponibilite",
                    () -> serviceCentral.verifierDisponibilite(tableId, date, creneauId));

        } catch (NumberFormatException e) {
            HttpUtils.sendError(exchange, 400, "IDs table et créneau doivent être des nombres");
        }
    }

    /**
     * Extrait les parties du chemin d'URL après un préfixe donné.
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