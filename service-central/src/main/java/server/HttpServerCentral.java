package server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpContext;
import rmi.Serveur;
import handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Serveur HTTP central utilisant la topologie en étoile
 * Utilise directement le ServiceCentralImpl pour accéder aux services inscrits
 */
public class HttpServerCentral {

    private static final Logger LOGGER = Logger.getLogger(HttpServerCentral.class.getName());

    private final int port;
    private HttpServer server;
    private final Serveur serviceCentral;

    public HttpServerCentral(int port, Serveur serviceCentral) {
        this.port = port;
        this.serviceCentral = serviceCentral;
    }

    /**
     * Démarrer le serveur HTTP
     */
    public void start() throws IOException {
        LOGGER.info("Démarrage du serveur HTTP sur le port " + port);

        // Créer le serveur HTTP
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Configurer le pool de threads
        server.setExecutor(Executors.newFixedThreadPool(10));

        // Créer les contextes (routes)
        createContexts();

        // Démarrer le serveur
        server.start();

        LOGGER.info("Serveur HTTP démarré et en écoute sur le port " + port);
        LOGGER.info("Routes disponibles:");
        LOGGER.info("  GET  /restaurants");
        LOGGER.info("  GET  /tables/{restaurantId}");
        LOGGER.info("  POST /reserver");
        LOGGER.info("  GET  /velib");
        LOGGER.info("  GET  /incidents");
        LOGGER.info("  GET  /services/etat");
    }

    /**
     * Créer les contextes (routes) HTTP
     */
    private void createContexts() {
        // Route pour récupérer les restaurants
        HttpContext restaurantsContext = server.createContext("/restaurants",
                new RestaurantsHandler(serviceCentral));

        // Route pour récupérer les tables libres
        HttpContext tablesContext = server.createContext("/tables",
                new TablesHandler(serviceCentral));

        // Route pour réserver une table
        HttpContext reserverContext = server.createContext("/reserver",
                new ReserverHandler(serviceCentral));

        // Route pour récupérer les données Vélib
        HttpContext velibContext = server.createContext("/velib",
                new VelibHandler(serviceCentral));

        // Route pour récupérer les incidents
        HttpContext incidentsContext = server.createContext("/incidents",
                new IncidentsHandler(serviceCentral));

        // Route pour l'état des services
        HttpContext etatContext = server.createContext("/services/etat",
                new EtatServicesHandler(serviceCentral));

        // Ajouter un filtre CORS à tous les contextes
        CorsFilter corsFilter = new CorsFilter();
        restaurantsContext.getFilters().add(corsFilter);
        tablesContext.getFilters().add(corsFilter);
        reserverContext.getFilters().add(corsFilter);
        velibContext.getFilters().add(corsFilter);
        incidentsContext.getFilters().add(corsFilter);
        etatContext.getFilters().add(corsFilter);

        LOGGER.info("Contextes HTTP créés avec filtres CORS");
    }

    /**
     * Arrêter le serveur
     */
    public void stop() {
        if (server != null) {
            LOGGER.info("Arrêt du serveur HTTP...");
            server.stop(0);
            LOGGER.info("Serveur HTTP arrêté");
        }
    }
}