import server.HttpServerCentral;
import interfaces.ServiceCentral;
import rmi.Serveur;
import utils.ConfigManager;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;
import java.util.logging.Level;


public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            ConfigManager configManager = new ConfigManager(args.length > 0 ? args[0] : null);
            CentralConfig config = new CentralConfig(configManager);

            LOGGER.info("=== Démarrage du Service Central ===");
            logConfig(config);

            Registry registry = createOrGetRegistry(config.rmiPort);

            Serveur serveur = new Serveur();

            // EXPORTER l'objet RMI dans le main
            ServiceCentral serviceCentralStub = (ServiceCentral) UnicastRemoteObject.exportObject(serveur, 0);

            // Enregistrer dans le registry
            registry.rebind("ServiceCentral", serviceCentralStub);
            LOGGER.info("Service Central RMI enregistré");

            // Démarrer le serveur HTTP/HTTPS
            HttpServerCentral httpServer = createHttpServer(config, serveur);
            httpServer.start();

            LOGGER.info("=== Service Central opérationnel ===");
            logAccessInfo(config);

            // Ajouter shutdown hook
            addShutdownHook(registry, httpServer);

            // Garder actif
            Thread.currentThread().join();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur fatale du Service Central", e);
            System.exit(1);
        }
    }

    private static void logConfig(CentralConfig config) {
        LOGGER.info("RMI Port: " + config.rmiPort);
        if (config.httpsEnabled) {
            LOGGER.info("HTTPS Port: " + config.httpsPort);
        } else {
            LOGGER.info("HTTP Port: " + config.httpPort);
        }
    }

    private static Registry createOrGetRegistry(int rmiPort) throws Exception {
        try {
            Registry registry = LocateRegistry.createRegistry(rmiPort);
            LOGGER.info("Registry RMI créé sur le port " + rmiPort);
            return registry;
        } catch (Exception e) {
            Registry registry = LocateRegistry.getRegistry(rmiPort);
            LOGGER.info("Utilisation du registry existant sur le port " + rmiPort);
            return registry;
        }
    }

    private static HttpServerCentral createHttpServer(CentralConfig config, Serveur serveur) {
        if (config.httpsEnabled) {
            LOGGER.info("Mode HTTPS activé");
            return new HttpServerCentral(config.httpsPort, serveur, true,
                    config.keystorePath, config.keystorePassword);
        } else {
            LOGGER.info("Mode HTTP activé");
            return new HttpServerCentral(config.httpPort, serveur);
        }
    }

    private static void logAccessInfo(CentralConfig config) {
        String host = "172.22.152.208";
        if (config.httpsEnabled) {
            LOGGER.info("Frontend accessible sur https://" + host + ":" + config.httpsPort);
            LOGGER.info("ATTENTION: Accepter le certificat auto-signé dans le navigateur");
        } else {
            LOGGER.info("Frontend accessible sur http://" + host + ":" + config.httpPort);
        }
    }

    private static void addShutdownHook(Registry registry, HttpServerCentral httpServer) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Arrêt du Service Central...");
            httpServer.stop();
            try {
                registry.unbind("ServiceCentral");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la désinscription", e);
            }
        }));
    }
}