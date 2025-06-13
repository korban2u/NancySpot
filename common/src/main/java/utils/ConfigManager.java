package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;


public class ConfigManager {

    private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());
    private static final String DEFAULT_CONFIG_FILE = "config.properties";

    private final Properties props;

    public ConfigManager(String configFile) {
        this.props = new Properties();
        loadDefaultValues();
        loadFromFile(configFile != null ? configFile : DEFAULT_CONFIG_FILE);
    }

    public ConfigManager() {
        this(DEFAULT_CONFIG_FILE);
    }


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


    private void loadFromFile(String configFile) {
        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
            LOGGER.info("Configuration chargée depuis " + configFile);
        } catch (IOException e) {
            LOGGER.warning("Impossible de charger " + configFile + ", utilisation des valeurs par défaut");
        }
    }


    public String getString(String key) {
        return props.getProperty(key);
    }

    public int getInt(String key) {
        return Integer.parseInt(props.getProperty(key));
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(props.getProperty(key));
    }







}