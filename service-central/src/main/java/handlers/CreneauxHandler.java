package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import rmi.Serveur;
import utils.HttpUtils;

import java.io.IOException;

/**
 * Handler pour la gestion des créneaux horaires
 * Gère les endpoints /creneaux et /creneaux/{id}
 */
public class CreneauxHandler implements HttpHandler {
    private final Serveur serviceCentral;

    public CreneauxHandler(Serveur serviceCentral) {
        this.serviceCentral = serviceCentral;
    }

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