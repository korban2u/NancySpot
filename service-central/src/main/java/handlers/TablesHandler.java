package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import rmi.Serveur;
import utils.HttpUtils;

import java.io.IOException;

public class TablesHandler implements HttpHandler {
    private final Serveur serviceCentral;

    public TablesHandler(Serveur serviceCentral) {
        this.serviceCentral = serviceCentral;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpUtils.handleGetRequest(exchange, "/tables", () -> {
            int restaurantId = extractRestaurantId(exchange);
            if (restaurantId <= 0) {
                throw new IllegalArgumentException("ID restaurant manquant ou invalide. Utilisez /tables/{id} ou /tables?restaurantId={id}");
            }
            return serviceCentral.getTablesLibres(restaurantId);
        });
    }


    private int extractRestaurantId(HttpExchange exchange) {
        try {
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();

            if (path.startsWith("/tables/")) {
                String idStr = path.substring("/tables/".length());
                if (!idStr.isEmpty()) {
                    return Integer.parseInt(idStr);
                }
            }

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
}