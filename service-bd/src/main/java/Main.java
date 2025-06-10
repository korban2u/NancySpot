import interfaces.IServiceBD;
import rmi.ServiceBDImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Classe principale pour démarrer le service RMI Base de Données
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final String DEFAULT_CONFIG_FILE = "config.properties";

    public static void main(String[] args) {
        try {
            // Charger la configuration
            Properties config = loadConfiguration(args.length > 0 ? args[0] : DEFAULT_CONFIG_FILE);

            // Récupérer les paramètres
            String dbUrl = config.getProperty("bd.jdbc.url", "jdbc:oracle:thin:@charlemagne:1521:XE");
            String dbUser = config.getProperty("bd.jdbc.user", "user");
            String dbPassword = config.getProperty("bd.jdbc.password", "password");
            String rmiName = config.getProperty("bd.rmi.name", "ServiceBD");
            int rmiPort = Integer.parseInt(config.getProperty("bd.rmi.port", "1099"));

            LOGGER.info("Démarrage du Service BD...");
            LOGGER.info("URL BD: " + dbUrl);
            LOGGER.info("RMI Port: " + rmiPort);
            LOGGER.info("RMI Name: " + rmiName);

            // Créer et démarrer le registry RMI
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(rmiPort);
                LOGGER.info("Registry RMI créé sur le port " + rmiPort);
            } catch (Exception e) {
                // Le registry existe déjà
                registry = LocateRegistry.getRegistry(rmiPort);
                LOGGER.info("Utilisation du registry existant sur le port " + rmiPort);
            }

            // Créer et enregistrer le service
            IServiceBD serviceBD = new ServiceBDImpl(dbUrl, dbUser, dbPassword);
            registry.rebind(rmiName, serviceBD);

            LOGGER.info("Service BD démarré et enregistré sous le nom '" + rmiName + "'");
            LOGGER.info("En attente des requêtes RMI...");

            // Arreter tout proprement si on arrete le programme (avec un control +c par exemple)
            Registry finalRegistry = registry;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    finalRegistry.unbind(rmiName);
                    LOGGER.info("Service BD arrêté proprement");
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Erreur lors de l'arrêt", e);
                }
            }));

            // Garder le service actif
            Thread.currentThread().join();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur fatale du Service BD", e);
            System.exit(1);
        }
    }

    /**
     * Charger la configuration depuis un fichier properties
     */
    private static Properties loadConfiguration(String configFile) {
        Properties props = new Properties();


        // On récupère les infos dans le fichier de config
        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
            LOGGER.info("Configuration chargée depuis " + configFile);
        } catch (IOException e) {
            LOGGER.warning("Impossible de charger " + configFile + ", utilisation des valeurs par défaut");
        }

        return props;
    }
}