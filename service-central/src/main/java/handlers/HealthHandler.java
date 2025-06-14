package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import rmi.Serveur;
import utils.HttpUtils;

import java.io.IOException;

public class HealthHandler implements HttpHandler {
    private final Serveur serviceCentral;

    public HealthHandler(Serveur serviceCentral) {
        this.serviceCentral = serviceCentral;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpUtils.handleGetRequest(exchange, "/health", () -> {
            try {
                // Vérification de l'état des services
                String etatServices = serviceCentral.getEtatServices();

                JSONObject health = new JSONObject();
                health.put("status", "UP");
                health.put("timestamp", System.currentTimeMillis());
                health.put("version", "2.0-CRENEAUX");
                health.put("services", new JSONObject(etatServices));

                // Ajout d'informations sur les fonctionnalités
                JSONObject features = new JSONObject();
                features.put("creneaux_horaires", true);
                features.put("reservations_avancees", true);
                features.put("gestion_statut_tables", true);
                features.put("validation_disponibilite", true);
                health.put("features", features);

                return health.toString();

            } catch (Exception e) {
                JSONObject health = new JSONObject();
                health.put("status", "DOWN");
                health.put("timestamp", System.currentTimeMillis());
                health.put("error", e.getMessage());
                return health.toString();
            }
        });
    }
}
