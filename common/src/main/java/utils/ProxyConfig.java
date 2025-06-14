package utils;

import utils.ConfigManager;

public class ProxyConfig {

    /**
     * Indique si le proxy IUT doit être utilisé pour les accès externes.
     * Si true, toutes les requêtes vers les APIs externes passent par le proxy IUT.
     * Si false, les requêtes sont effectuées en mode direct.
     */
    public final boolean useIutProxy;

    /**
     * Nom d'hôte ou adresse IP du proxy IUT.
     * Utilisé uniquement si useIutProxy est true.
     */
    public final String proxyHost;

    /**
     * Port du proxy IUT.
     * Utilisé uniquement si useIutProxy est true.
     */
    public final String proxyPort;

    /**
     * Nom d'hôte ou adresse IP du service central.
     * Utilisé pour s'enregistrer auprès du service central via RMI.
     */
    public final String centralHost;

    /**
     * Port RMI du service central.
     * Utilisé pour la connexion au registre RMI du service central.
     */
    public final int centralPort;

    /**
     * Constructeur qui charge la configuration depuis le ConfigManager.
     *
     * Extrait tous les paramètres nécessaires au service proxy :
     * - proxy.use.iut.proxy : Activation du proxy IUT
     * - proxy.iut.host : Adresse du proxy IUT
     * - proxy.iut.port : Port du proxy IUT
     * - central.host : Adresse du service central
     * - central.rmi.port : Port RMI du service central
     *
     * @param config le gestionnaire de configuration initialisé
     */
    public ProxyConfig(ConfigManager config) {
        this.useIutProxy = config.getBoolean("proxy.use.iut.proxy");
        this.proxyHost = config.getString("proxy.iut.host");
        this.proxyPort = config.getString("proxy.iut.port");
        this.centralHost = config.getString("central.host");
        this.centralPort = config.getInt("central.rmi.port");
    }
}