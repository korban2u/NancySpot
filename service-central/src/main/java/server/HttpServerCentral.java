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
 * Serveur HTTP Central avec support HTTPS et gestion des créneaux horaires.
 *
 * Ce serveur expose les APIs qui font le pont entre les clients web
 * et les services RMI backend. Il supporte à la fois HTTP et HTTPS
 *
 * Endpoints disponibles :
 * - GET /restaurants - Liste des restaurants
 * - GET /creneaux - Liste des créneaux horaires
 * - GET /tables/libres/{restaurantId}/{date}/{creneauId} - Tables libres
 * - POST /reserver - Effectuer une réservation
 * - GET /incidents - Incidents de circulation
 * - GET /services/etat - État des services backend
 */
public class HttpServerCentral {

    private static final Logger LOGGER = Logger.getLogger(HttpServerCentral.class.getName());

    private final int port;
    private final boolean httpsEnabled;
    private final String keystorePath;
    private final String keystorePassword;
    private HttpServer server;
    private final Serveur serviceCentral;

    /**
     * Constructeur pour serveur HTTP simple.
     *
     * @param port le port d'écoute du serveur
     * @param serviceCentral l'instance du service central RMI
     */
    public HttpServerCentral(int port, Serveur serviceCentral) {
        this(port, serviceCentral, false, null, null);
    }

    /**
     * Constructeur complet avec support HTTPS.
     *
     * @param port le port d'écoute du serveur
     * @param serviceCentral l'instance du service central RMI
     * @param httpsEnabled true pour activer HTTPS, false pour HTTP simple
     * @param keystorePath le chemin vers le keystore SSL (requis si HTTPS activé)
     * @param keystorePassword le mot de passe du keystore SSL (requis si HTTPS activé)
     */
    public HttpServerCentral(int port, Serveur serviceCentral, boolean httpsEnabled,
                             String keystorePath, String keystorePassword) {
        this.port = port;
        this.serviceCentral = serviceCentral;
        this.httpsEnabled = httpsEnabled;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
    }

    /**
     * Démarre le serveur HTTP ou HTTPS selon la configuration.
     *
     * @throws Exception en cas d'erreur lors du démarrage du serveur
     */
    public void start() throws Exception {
        if (httpsEnabled) {
            startHttpsServer();
        } else {
            startHttpServer();
        }
    }

    /**
     * Démarre le serveur HTTPS avec certificat SSL.
     * Configure le contexte SSL et le keystore pour les connexions sécurisées.
     *
     * @throws Exception en cas d'erreur lors de la configuration SSL ou du démarrage
     */
    private void startHttpsServer() throws Exception {
        LOGGER.info("Démarrage du serveur HTTPS sur le port " + port);

        // Chargement du keystore SSL
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            keyStore.load(fis, keystorePassword.toCharArray());
        }

        // Configuration des gestionnaires de clés et de confiance
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, keystorePassword.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(keyStore);

        // Création du contexte SSL
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        // Création et configuration du serveur HTTPS
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

    /**
     * Démarre le serveur HTTP simple (non sécurisé).
     *
     * @throws IOException en cas d'erreur lors du démarrage du serveur
     */
    private void startHttpServer() throws IOException {
        LOGGER.info("Démarrage du serveur HTTP sur le port " + port);

        server = HttpServer.create(new InetSocketAddress(port), 0);
        configureServer();

        LOGGER.info("Serveur HTTP démarré sur http://localhost:" + port);
    }

    /**
     * Configure le serveur avec les contextes et les handlers.
     * Initialise le pool de threads et démarre effectivement le serveur.
     */
    private void configureServer() {
        server.setExecutor(Executors.newFixedThreadPool(10));
        createContexts();
        server.start();
        logAvailableRoutes();
    }

    /**
     * Crée tous les contextes HTTP avec leurs handlers et filtres CORS.
     * Configure l'ensemble des endpoints de l'API REST avec gestion des créneaux.
     */
    private void createContexts() {
        CorsFilter corsFilter = new CorsFilter();

        // Endpoints restaurants
        HttpContext restaurantsContext = server.createContext("/restaurants",
                new RestaurantsHandler(serviceCentral));
        restaurantsContext.getFilters().add(corsFilter);

        // Endpoints créneaux horaires
        HttpContext creneauxContext = server.createContext("/creneaux",
                new CreneauxHandler(serviceCentral));
        creneauxContext.getFilters().add(corsFilter);

        // Endpoints tables avec gestion des créneaux
        HttpContext tablesCreneauxContext = server.createContext("/tables/",
                new TablesCreneauxHandler(serviceCentral));
        tablesCreneauxContext.getFilters().add(corsFilter);

        // Endpoint principal de réservation
        HttpContext reserverContext = server.createContext("/reserver",
                new ReserverHandler(serviceCentral));
        reserverContext.getFilters().add(corsFilter);

        // Endpoints de gestion des réservations
        HttpContext reservationsContext = server.createContext("/reservations/",
                new ReservationsHandler(serviceCentral));
        reservationsContext.getFilters().add(corsFilter);

        // Endpoints externes (incidents de circulation)
        HttpContext incidentsContext = server.createContext("/incidents",
                new IncidentsHandler(serviceCentral));
        incidentsContext.getFilters().add(corsFilter);

        // Endpoints système
        HttpContext etatContext = server.createContext("/services/etat",
                new EtatServicesHandler(serviceCentral));
        etatContext.getFilters().add(corsFilter);


        LOGGER.info("Contextes HTTP créés avec filtres CORS");
    }

    /**
     * Affiche toutes les routes disponibles dans les logs.
     * Utile pour le debugging et la documentation des APIs.
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

        LOGGER.info("=== SERVEUR OPÉRATIONNEL ===");
    }

    /**
     * Arrête proprement le serveur HTTP.
     * Termine le traitement des requêtes en cours et libère les ressources.
     */
    public void stop() {
        if (server != null) {
            LOGGER.info("Arrêt du serveur...");
            server.stop(0);
            LOGGER.info("Serveur arrêté");
        }
    }
}