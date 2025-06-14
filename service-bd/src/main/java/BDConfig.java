import utils.Configurateur;

/**
 * Configuration du service BD.
 * Contient les paramètres de connexion à la base Oracle et au service central.
 */
public class BDConfig {

    public final String jdbcUrl;

    public final String jdbcUser;

    public final String jdbcPassword;

    public final String centralHost;

    public final int centralPort;

    /**
     * Constructeur qui charge la configuration depuis le ConfigManager.
     *
     * @param config le gestionnaire de configuration
     */
    public BDConfig(Configurateur config) {
        this.jdbcUrl = config.getString("bd.jdbc.url");
        this.jdbcUser = config.getString("bd.jdbc.user");
        this.jdbcPassword = config.getString("bd.jdbc.password");
        this.centralHost = config.getString("central.host");
        this.centralPort = config.getInt("central.rmi.port");
    }
}