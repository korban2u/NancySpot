import server.HttpServerCentral;
import server.HttpsProxyServer;
import rmi.Serveur;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Classe principale pour démarrer le service central HTTP/RMI + Proxy HTTPS
 * Centre de la topologie en étoile avec support HTTPS pour webetu
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final String DEFAULT_CONFIG_FILE = "config.properties";

    public static void main(String[] args) {
        try {
            // Charger la configuration
            Properties config = loadConfiguration(args.length > 0 ? args[0] : DEFAULT_CONFIG_FILE);

            // Récupérer les paramètres
            int rmiPort = Integer.parseInt(config.getProperty("central.rmi.port", "1098"));
            int httpPort = Integer.parseInt(config.getProperty("central.http.port", "8080"));
            int httpsPort = Integer.parseInt(config.getProperty("central.https.port", "8443"));
            boolean enableHttpsProxy = Boolean.parseBoolean(config.getProperty("central.enable.https.proxy", "true"));

            LOGGER.info("=== Démarrage du Service Central ===");
            LOGGER.info("RMI Port: " + rmiPort);
            LOGGER.info("HTTP Port: " + httpPort);
            if (enableHttpsProxy) {
                LOGGER.info("HTTPS Proxy Port: " + httpsPort);
            }

            // 1. Créer et démarrer le registry RMI (SEULE registry du système)
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(rmiPort);
                LOGGER.info("Registry RMI créé sur le port " + rmiPort);
            } catch (Exception e) {
                registry = LocateRegistry.getRegistry(rmiPort);
                LOGGER.info("Utilisation du registry existant sur le port " + rmiPort);
            }

            // 2. Créer le service central RMI
            Serveur serveur = new Serveur();

            // 3. Enregistrer le service central dans le registry
            registry.rebind("ServiceCentral", serveur);
            LOGGER.info("Service Central RMI enregistré");

            // 4. Créer et démarrer le serveur HTTP
            HttpServerCentral httpServer = new HttpServerCentral(httpPort, serveur);
            httpServer.start();

            // 5. Créer et démarrer le proxy HTTPS si activé
            HttpsProxyServer httpsProxy = null;
            if (enableHttpsProxy) {
                httpsProxy = new HttpsProxyServer(httpsPort, "localhost", httpPort);
                httpsProxy.start();
            }

            LOGGER.info("=== Service Central opérationnel ===");
            LOGGER.info("Les services BD et Proxy peuvent maintenant s'inscrire");
            LOGGER.info("Frontend HTTP accessible sur http://localhost:" + httpPort);
            if (enableHttpsProxy) {
                LOGGER.info("Frontend HTTPS accessible sur https://localhost:" + httpsPort);
                LOGGER.info("Utilisez l'URL HTTPS depuis webetu pour éviter les erreurs Mixed Content");
            }

            // Ajouter un shutdown hook
            Registry finalRegistry = registry;
            HttpsProxyServer finalHttpsProxy = httpsProxy;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("Arrêt du Service Central...");
                httpServer.stop();
                if (finalHttpsProxy != null) {
                    finalHttpsProxy.stop();
                }
                try {
                    finalRegistry.unbind("ServiceCentral");
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Erreur lors de la désinscription", e);
                }
            }));

            // Garder le service actif
            Thread.currentThread().join();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur fatale du Service Central", e);
            System.exit(1);
        }
    }

    /**
     * Charger la configuration depuis un fichier properties
     */
    private static Properties loadConfiguration(String configFile) {
        Properties props = new Properties();

        // Valeurs par défaut
        props.setProperty("central.rmi.port", "1098");
        props.setProperty("central.http.port", "8080");
        props.setProperty("central.https.port", "8443");
        props.setProperty("central.enable.https.proxy", "true");

        // Charger depuis le fichier si disponible
        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
            LOGGER.info("Configuration chargée depuis " + configFile);
        } catch (IOException e) {
            LOGGER.warning("Impossible de charger " + configFile + ", utilisation des valeurs par défaut");
        }

        return props;
    }
}