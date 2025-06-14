import interfaces.ServiceCentral;
import interfaces.ServiceProxy;
import rmi.Proxy;
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
            ProxyConfig config = new ProxyConfig(configManager);

            LOGGER.info("=== Démarrage du Service Proxy ===");
            logConfig(config);

            Proxy proxy = new Proxy(config.useIutProxy, config.proxyHost, config.proxyPort);

            ServiceProxy serviceProxyStub = (ServiceProxy) UnicastRemoteObject.exportObject(proxy, 0);

            ServiceCentral serviceCentral = connectToServiceCentral(config);

            boolean inscrit = serviceCentral.enregistrerServiceProxy(serviceProxyStub);

            if (inscrit) {
                LOGGER.info("=== Service Proxy opérationnel ===");
                LOGGER.info("Inscrit auprès du Service Central");
                if (config.useIutProxy) {
                    LOGGER.info("Proxy réseau : " + config.proxyHost + ":" + config.proxyPort);
                }
            } else {
                LOGGER.severe("Échec de l'inscription auprès du Service Central");
                System.exit(1);
            }

            addShutdownHook(serviceCentral);

            Thread.currentThread().join();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur fatale du Service Proxy", e);
            System.exit(1);
        }
    }

    private static void logConfig(ProxyConfig config) {
        LOGGER.info("Utilisation proxy IUT: " + config.useIutProxy);
        LOGGER.info("Service Central: " + config.centralHost + ":" + config.centralPort);
    }

    private static ServiceCentral connectToServiceCentral(ProxyConfig config) throws Exception {
        Registry registry = LocateRegistry.getRegistry(config.centralHost, config.centralPort);
        return (ServiceCentral) registry.lookup("ServiceCentral");
    }

    private static void addShutdownHook(ServiceCentral serviceCentral) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                serviceCentral.supprimerService("PROXY");
                LOGGER.info("Service Proxy désinscrit du Service Central");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la désinscription", e);
            }
        }));
    }
}