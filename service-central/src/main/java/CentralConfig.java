import utils.Configurateur;

/**
 * Configuration spécialisée pour le service central.
 *
 * Cette classe utilise le ConfigManager pour charger les valeurs depuis les fichiers
 * de configuration et fournit un accès typé et structuré aux paramètres.
 *
 * Paramètres gérés :
 * - Port RMI pour l'enregistrement des services
 * - Port HTTP pour les APIs REST
 * - Configuration HTTPS (activation, port, certificats)
 * - Chemins et mots de passe des keystores SSL
 */
public class CentralConfig {

    /**
     * Port d'écoute du registre RMI.
     * Utilisé pour l'enregistrement et la découverte des services backend.
     */
    public final int rmiPort;

    /**
     * Port d'écoute du serveur HTTP.
     * Utilisé quand HTTPS est désactivé.
     */
    public final int httpPort;

    /**
     * Indique si le mode HTTPS est activé.
     * Si true, le serveur utilise SSL/TLS pour les connexions sécurisées.
     */
    public final boolean httpsEnabled;

    /**
     * Port d'écoute du serveur HTTPS.
     * Utilisé quand HTTPS est activé.
     */
    public final int httpsPort;

    /**
     * Chemin vers le fichier keystore SSL.
     * Contient les certificats nécessaires pour les connexions HTTPS.
     */
    public final String keystorePath;

    /**
     * Mot de passe du keystore SSL.
     * Requis pour accéder aux certificats stockés dans le keystore.
     */
    public final String keystorePassword;

    /**
     * Constructeur qui charge la configuration depuis le ConfigManager.
     *
     * Extrait tous les paramètres nécessaires au service central :
     * - central.rmi.port : Port du registre RMI
     * - central.http.port : Port HTTP
     * - central.https.enabled : Activation HTTPS
     * - central.https.port : Port HTTPS
     * - central.keystore.path : Chemin du keystore
     * - central.keystore.password : Mot de passe du keystore
     *
     * @param config le gestionnaire de configuration initialisé
     */
    public CentralConfig(Configurateur config) {
        this.rmiPort = config.getInt("central.rmi.port");
        this.httpPort = config.getInt("central.http.port");
        this.httpsEnabled = config.getBoolean("central.https.enabled");
        this.httpsPort = config.getInt("central.https.port");
        this.keystorePath = config.getString("central.keystore.path");
        this.keystorePassword = config.getString("central.keystore.password");
    }
}