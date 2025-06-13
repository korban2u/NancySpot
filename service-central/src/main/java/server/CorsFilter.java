package server;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Filtre pour ajouter les headers CORS nécessaires
 */
public class CorsFilter extends Filter {

    private static final Logger LOGGER = Logger.getLogger(CorsFilter.class.getName());

    @Override
    public String description() {
        return "Filtre CORS pour permettre les requêtes depuis webetu";
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        // Ajouter les headers CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Accept");
        exchange.getResponseHeaders().add("Access-Control-Max-Age", "3600");

        // Gérer les requêtes OPTIONS (preflight)
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }

        // Continuer avec la chaîne de filtres
        chain.doFilter(exchange);
    }
}