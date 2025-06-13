package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import rmi.Serveur;
import utils.HttpUtils;

import java.io.IOException;

public class IncidentsHandler implements HttpHandler {
    private final Serveur serviceCentral;

    public IncidentsHandler(Serveur serviceCentral) {
        this.serviceCentral = serviceCentral;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpUtils.handleGetRequest(exchange, "/incidents", serviceCentral::getIncidents);
    }
}
