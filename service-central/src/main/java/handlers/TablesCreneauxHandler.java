package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import rmi.Serveur;
import utils.HttpUtils;

import java.io.IOException;
/**
 * Handler amélioré pour les tables avec support des créneaux
 * Gère plusieurs endpoints pour la disponibilité des tables
 */
public class TablesCreneauxHandler implements HttpHandler {
    private final Serveur serviceCentral;

    public TablesCreneauxHandler(Serveur serviceCentral) {
        this.serviceCentral = serviceCentral;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (path.startsWith("/tables/libres/")) {
            handleTablesLibresCreneau(exchange);
        } else if (path.startsWith("/tables/statut/")) {
            handleTablesStatut(exchange);
        } else if (path.startsWith("/tables/disponibilite/")) {
            handleVerifierDisponibilite(exchange);
        } else if (path.startsWith("/tables/")) {
            // Ancien endpoint pour compatibilité
            handleTablesLegacy(exchange);
        } else {
            HttpUtils.sendError(exchange, 404, "Endpoint non trouvé");
        }
    }

    /**
     * GET /tables/libres/{restaurantId}/{date}/{creneauId}
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
     * GET /tables/statut/{restaurantId}/{date}/{creneauId}
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
     * GET /tables/disponibilite/{tableId}/{date}/{creneauId}
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
     * Ancien endpoint pour compatibilité descendante
     * GET /tables/{restaurantId} ou /tables?restaurantId={id}
     */
    private void handleTablesLegacy(HttpExchange exchange) throws IOException {
        int restaurantId = extractRestaurantId(exchange);
        if (restaurantId <= 0) {
            HttpUtils.sendError(exchange, 400, "ID restaurant manquant ou invalide");
            return;
        }

        HttpUtils.handleGetRequest(exchange, "/tables", () -> serviceCentral.getTablesLibres(restaurantId));
    }

    private int extractRestaurantId(HttpExchange exchange) {
        try {
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();

            // Format /tables/{id}
            if (path.startsWith("/tables/")) {
                String idStr = path.substring("/tables/".length());
                if (!idStr.isEmpty() && !idStr.contains("/")) {
                    return Integer.parseInt(idStr);
                }
            }

            // Format /tables?restaurantId={id}
            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=", 2);
                    if (keyValue.length == 2 && "restaurantId".equals(keyValue[0])) {
                        return Integer.parseInt(keyValue[1]);
                    }
                }
            }

            return -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String[] extractPathParts(String path, String prefix) {
        String remaining = path.substring(prefix.length());
        return remaining.split("/");
    }
}

