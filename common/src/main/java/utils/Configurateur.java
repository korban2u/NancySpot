package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Gestionnaire de configuration pour l'application Nancy Spot.
 *
 * Cette classe centralise la gestion des paramètres de configuration
 * de tous les services de l'application. Elle charge les configurations
 * depuis des fichiers properties et fournit des valeurs par défaut
 * pour assurer le bon fonctionnement même sans fichier de configuration.
 *
 * Paramètres gérés :
 * - Configuration du service central (ports RMI, HTTP, HTTPS)
 * - Configuration de la base de données (JDBC)
 * - Configuration du proxy réseau
 */
public class Configurateur {

    private static final Logger LOGGER = Logger.getLogger(Configurateur.class.getName());
    private static final String DEFAULT_CONFIG_FILE = "config.properties";

    private final Properties props;

    /**
     * Constructeur avec fichier de configuration spécifique.
     *
     * @param configFile le chemin vers le fichier de configuration
     *                   ou null pour utiliser le fichier par défaut
     */
    public Configurateur(String configFile) {
        this.props = new Properties();
        loadDefaultValues();
        loadFromFile(configFile != null ? configFile : DEFAULT_CONFIG_FILE);
    }

    /**
     * Constructeur par défaut.
     * Utilise le fichier de configuration par défaut (config.properties).
     */
    public Configurateur() {
        this(DEFAULT_CONFIG_FILE);
    }

    /**
     * Charge les valeurs par défaut pour tous les paramètres.
     * Ces valeurs assurent le fonctionnement de l'application
     * même sans fichier de configuration externe.
     */
    private void loadDefaultValues() {
        // Service Central
        props.setProperty("central.rmi.port", "1098");
        props.setProperty("central.http.port", "8080");
        props.setProperty("central.https.enabled", "false");
        props.setProperty("central.https.port", "8443");
        props.setProperty("central.keystore.path", "nancy-keystore.jks");
        props.setProperty("central.keystore.password", "password123");
        props.setProperty("central.host", "localhost");

        // Service BD
        props.setProperty("bd.jdbc.url", "jdbc:oracle:thin:@charlemagne:1521:XE");
        props.setProperty("bd.jdbc.user", "user");
        props.setProperty("bd.jdbc.password", "password");
        props.setProperty("bd.rmi.port", "1099");

        // Service Proxy
        props.setProperty("proxy.use.iut.proxy", "false");
        props.setProperty("proxy.iut.host", "proxy.infra.univ-lorraine.fr");
        props.setProperty("proxy.iut.port", "3128");
    }

    /**
     * Charge la configuration depuis un fichier properties.
     * En cas d'échec de lecture, les valeurs par défaut sont conservées.
     *
     * @param configFile le chemin vers le fichier de configuration
     */
    private void loadFromFile(String configFile) {
        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
            LOGGER.info("Configuration chargée depuis " + configFile);
        } catch (IOException e) {
            LOGGER.warning("Impossible de charger " + configFile + ", utilisation des valeurs par défaut");
        }
    }

    /**
     * Récupère une valeur de configuration sous forme de chaîne.
     *
     * @param key la clé de configuration
     * @return la valeur associée à la clé ou null si non trouvée
     */
    public String getString(String key) {
        return props.getProperty(key);
    }

    /**
     * Récupère une valeur de configuration sous forme d'entier.
     *
     * @param key la clé de configuration
     * @return la valeur entière associée à la clé
     * @throws NumberFormatException si la valeur n'est pas un entier valide
     */
    public int getInt(String key) {
        return Integer.parseInt(props.getProperty(key));
    }

    /**
     * Récupère une valeur de configuration sous forme de booléen.
     *
     * @param key la clé de configuration
     * @return la valeur booléenne associée à la clé
     */
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(props.getProperty(key));
    }
}