package server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpContext;
import rmi.Serveur;
import handlers.*;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Serveur HTTP Central amélioré avec support des créneaux
 * Version 2.0 avec nouveaux endpoints pour la gestion des créneaux horaires
 *
 * @author Nancy Spot Team
 * @version 2.0 - Avec gestion des créneaux
 */
public class HttpServerCentral {

    private static final Logger LOGGER = Logger.getLogger(HttpServerCentral.class.getName());

    private final int port;
    private final boolean httpsEnabled;
    private final String keystorePath;
    private final String keystorePassword;
    private HttpServer server;
    private final Serveur serviceCentral;

    public HttpServerCentral(int port, Serveur serviceCentral) {
        this(port, serviceCentral, false, null, null);
    }

    public HttpServerCentral(int port, Serveur serviceCentral, boolean httpsEnabled,
                             String keystorePath, String keystorePassword) {
        this.port = port;
        this.serviceCentral = serviceCentral;
        this.httpsEnabled = httpsEnabled;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
    }

    public void start() throws Exception {
        if (httpsEnabled) {
            startHttpsServer();
        } else {
            startHttpServer();
        }
    }

    private void startHttpsServer() throws Exception {
        LOGGER.info("Démarrage du serveur HTTPS sur le port " + port);

        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            keyStore.load(fis, keystorePassword.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, keystorePassword.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(port), 0);
        httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            @Override
            public void configure(HttpsParameters params) {
                params.setSSLParameters(sslContext.getDefaultSSLParameters());
            }
        });

        this.server = httpsServer;
        configureServer();

        LOGGER.info("Serveur HTTPS démarré sur https://localhost:" + port);
    }

    private void startHttpServer() throws IOException {
        LOGGER.info("Démarrage du serveur HTTP sur le port " + port);

        server = HttpServer.create(new InetSocketAddress(port), 0);
        configureServer();

        LOGGER.info("Serveur HTTP démarré sur http://localhost:" + port);
    }

    private void configureServer() {
        server.setExecutor(Executors.newFixedThreadPool(10));
        createContexts();
        server.start();
        logAvailableRoutes();
    }

    /**
     * Configuration de tous les contextes HTTP avec support des créneaux
     */
    private void createContexts() {
        CorsFilter corsFilter = new CorsFilter();

        // ==================== ENDPOINTS RESTAURANTS ====================

        HttpContext restaurantsContext = server.createContext("/restaurants",
                new RestaurantsHandler(serviceCentral));
        restaurantsContext.getFilters().add(corsFilter);

        // ==================== ENDPOINTS CRÉNEAUX ====================

        HttpContext creneauxContext = server.createContext("/creneaux",
                new CreneauxHandler(serviceCentral));
        creneauxContext.getFilters().add(corsFilter);

        // ==================== ENDPOINTS TABLES ====================

        HttpContext tablesCreneauxContext = server.createContext("/tables/",
                new TablesCreneauxHandler(serviceCentral));
        tablesCreneauxContext.getFilters().add(corsFilter);

        // ==================== ENDPOINTS RÉSERVATIONS ====================

        // Endpoint principal de réservation
        HttpContext reserverContext = server.createContext("/reserver",
                new ReserverHandler(serviceCentral));
        reserverContext.getFilters().add(corsFilter);

        // Endpoints de gestion des réservations
        HttpContext reservationsContext = server.createContext("/reservations/",
                new ReservationsHandler(serviceCentral));
        reservationsContext.getFilters().add(corsFilter);

        // ==================== ENDPOINTS EXTERNES ====================

        // Incidents de circulation
        HttpContext incidentsContext = server.createContext("/incidents",
                new IncidentsHandler(serviceCentral));
        incidentsContext.getFilters().add(corsFilter);

        // ==================== ENDPOINTS SYSTÈME ====================

        // État des services
        HttpContext etatContext = server.createContext("/services/etat",
                new EtatServicesHandler(serviceCentral));
        etatContext.getFilters().add(corsFilter);

        // Endpoint de santé général
        HttpContext healthContext = server.createContext("/health",
                new HealthHandler(serviceCentral));
        healthContext.getFilters().add(corsFilter);

        LOGGER.info("Contextes HTTP créés avec filtres CORS");
    }

    /**
     * Affiche toutes les routes disponibles dans les logs
     */
    private void logAvailableRoutes() {
        LOGGER.info("=== ROUTES DISPONIBLES ===");

        LOGGER.info("RESTAURANTS:");
        LOGGER.info("  GET  /restaurants                     - Liste des restaurants");

        LOGGER.info("CRÉNEAUX:");
        LOGGER.info("  GET  /creneaux                        - Liste des créneaux disponibles");
        LOGGER.info("  GET  /creneaux/{id}                   - Détails d'un créneau");

        LOGGER.info("TABLES:");
        LOGGER.info("  GET  /tables/libres/{restaurantId}/{date}/{creneauId}    - Tables libres pour un créneau");
        LOGGER.info("  GET  /tables/statut/{restaurantId}/{date}/{creneauId}    - Statut de toutes les tables");
        LOGGER.info("  GET  /tables/disponibilite/{tableId}/{date}/{creneauId} - Vérifier disponibilité d'une table");

        LOGGER.info("RÉSERVATIONS:");
        LOGGER.info("  POST /reserver                        - Effectuer une réservation");
        LOGGER.info("  GET  /reservations/date/{restaurantId}/{date}           - Réservations d'une date");
        LOGGER.info("  POST /reservations/annuler/{reservationId}              - Annuler une réservation");

        LOGGER.info("EXTERNES:");
        LOGGER.info("  GET  /incidents                       - Incidents de circulation");

        LOGGER.info("SYSTÈME:");
        LOGGER.info("  GET  /services/etat                   - État des services RMI");
        LOGGER.info("  GET  /health                          - Santé générale du système");

        LOGGER.info("=== SERVEUR OPÉRATIONNEL ===");
    }

    public void stop() {
        if (server != null) {
            LOGGER.info("Arrêt du serveur...");
            server.stop(0);
            LOGGER.info("Serveur arrêté");
        }
    }
}