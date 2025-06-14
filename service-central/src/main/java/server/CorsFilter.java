package server;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Filtre CORS (Cross-Origin Resource Sharing) pour le serveur HTTP central.
 *
 * Ce filtre permet aux applications web hébergées sur des domaines différents
 * d'accéder aux APIs du service central. Il est nécessaire
 * pour permettre au site sur webetu d'accéder aux services backend.
 *
 * Le filtre ajoute automatiquement les en-têtes CORS appropriés à toutes les réponses
 * et gère les requêtes preflight OPTIONS envoyées par les navigateurs.
 *
 * En-têtes CORS configurés :
 * - Access-Control-Allow-Origin: * (accepte toutes les origines)
 * - Access-Control-Allow-Methods: GET, POST, OPTIONS
 * - Access-Control-Allow-Headers: Content-Type, Accept
 * - Access-Control-Max-Age: 3600 (cache preflight pendant 1 heure)
 */
public class CorsFilter extends Filter {

    private static final Logger LOGGER = Logger.getLogger(CorsFilter.class.getName());

    /**
     * Retourne la description du filtre pour les logs et le debugging.
     *
     * @return une description textuelle du filtre
     */
    @Override
    public String description() {
        return "Filtre CORS pour permettre les requêtes depuis webetu";
    }

    /**
     * Applique le filtre CORS à l'échange HTTP.
     *
     * Cette méthode ajoute les en-têtes CORS nécessaires à toutes les réponses
     * et traite spécialement les requêtes OPTIONS (preflight) envoyées par
     * les navigateurs avant les vraies requêtes cross-origin.
     *
     * Pour les requêtes OPTIONS, le filtre retourne immédiatement une réponse 200
     * sans passer à la chaîne de traitement suivante.
     *
     * Pour toutes les autres requêtes, il ajoute les en-têtes CORS puis
     * continue le traitement normal.
     *
     * @param exchange l'échange HTTP en cours de traitement
     * @param chain la chaîne de filtres à continuer si ce n'est pas une requête OPTIONS
     * @throws IOException en cas d'erreur lors du traitement de la requête
     */
    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        // Ajout des en-têtes CORS pour toutes les réponses
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Accept");
        exchange.getResponseHeaders().add("Access-Control-Max-Age", "3600");

        // Gestion spéciale des requêtes preflight OPTIONS
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            // Réponse immédiate pour les requêtes preflight
            exchange.sendResponseHeaders(200, -1);
            return;
        }

        // Pour toutes les autres requêtes, continuer la chaîne de filtres
        chain.doFilter(exchange);
    }
}