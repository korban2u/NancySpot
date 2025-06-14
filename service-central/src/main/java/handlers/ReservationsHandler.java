package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import rmi.Serveur;
import utils.HttpUtils;

import java.io.IOException;

/**
 * Handler pour la gestion des réservations avec créneaux
 * Gère les endpoints /reservations/*
 */
public class ReservationsHandler implements HttpHandler {
    private final Serveur serviceCentral;

    public ReservationsHandler(Serveur serviceCentral) {
        this.serviceCentral = serviceCentral;
    }

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
     * GET /reservations/date/{restaurantId}/{date}
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
     * POST /reservations/annuler/{reservationId}
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

    private String[] extractPathParts(String path, String prefix) {
        String remaining = path.substring(prefix.length());
        return remaining.split("/");
    }
}
