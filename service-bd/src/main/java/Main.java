import interfaces.ServiceCentral;
import interfaces.ServiceBD;
import rmi.BaseDonnee;
import utils.Configurateur;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Point d'entrée du service BD.
 * Lance le service de base de données et l'enregistre auprès du service central.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /**
     * Point d'entrée principal du service BD.
     * Charge la configuration, démarre le service et l'enregistre au service central.
     *
     * @param args arguments de ligne de commande (optionnel : fichier de config)
     */
    public static void main(String[] args) {
        try {
            Configurateur configurateur = new Configurateur(args.length > 0 ? args[0] : null);
            BDConfig config = new BDConfig(configurateur);

            LOGGER.info("=== Démarrage du Service BD ===");
            logConfig(config);

            BaseDonnee baseDonnee = new BaseDonnee(config.jdbcUrl, config.jdbcUser, config.jdbcPassword);

            ServiceBD serviceBDStub = (ServiceBD) UnicastRemoteObject.exportObject(baseDonnee, 0);

            Registry registry = LocateRegistry.getRegistry(config.centralHost, config.centralPort);
            ServiceCentral serviceCentral =  (ServiceCentral) registry.lookup("ServiceCentral");

            boolean inscrit = serviceCentral.enregistrerServiceBD(serviceBDStub);

            if (inscrit) {
                LOGGER.info("=== Service BD opérationnel ===");
                LOGGER.info("Inscrit auprès du Service Central");
            } else {
                LOGGER.severe("Échec de l'inscription auprès du Service Central");
                System.exit(1);
            }

            addShutdownHook(serviceCentral);

            Thread.currentThread().join();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur fatale du Service BD", e);
            System.exit(1);
        }
    }

    /**
     * Affiche la configuration dans les logs.
     *
     * @param config la configuration à afficher
     */
    private static void logConfig(BDConfig config) {
        LOGGER.info("URL BD: " + config.jdbcUrl);
        LOGGER.info("Service Central: " + config.centralHost + ":" + config.centralPort);
    }

    /**
     * Configure un hook d'arrêt pour se désenregistrer proprement.
     *
     * @param serviceCentral le service central pour la désinscription
     */
    private static void addShutdownHook(ServiceCentral serviceCentral) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                serviceCentral.supprimerService("BD");
                LOGGER.info("Service BD désinscrit du Service Central");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la désinscription", e);
            }
        }));
    }
}