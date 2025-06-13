package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import rmi.Serveur;
import utils.HttpUtils;

import java.io.IOException;
public class ReserverHandler implements HttpHandler {
    private final Serveur serviceCentral;

    public ReserverHandler(Serveur serviceCentral) {
        this.serviceCentral = serviceCentral;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpUtils.handlePostRequest(exchange, "/reserver",
                serviceCentral::reserverTable,
                new HttpUtils.ReservationValidator());
    }
}