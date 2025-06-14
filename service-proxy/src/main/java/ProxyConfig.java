import utils.Configurateur;

/**
 * Configuration du service Proxy.
 * Contient les paramètres pour l'accès aux APIs externes et la connexion au service central.
 */
public class ProxyConfig {

    public final boolean useIutProxy;

    public final String proxyHost;

    public final String proxyPort;

    public final String centralHost;

    public final int centralPort;

    /**
     * Constructeur qui charge la configuration depuis le ConfigManager.
     *
     * @param config le gestionnaire de configuration
     */
    public ProxyConfig(Configurateur config) {
        this.useIutProxy = config.getBoolean("proxy.use.iut.proxy");
        this.proxyHost = config.getString("proxy.iut.host");
        this.proxyPort = config.getString("proxy.iut.port");
        this.centralHost = config.getString("central.host");
        this.centralPort = config.getInt("central.rmi.port");
    }
}