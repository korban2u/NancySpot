import utils.ConfigManager;


public class BDConfig {
    public final String jdbcUrl;
    public final String jdbcUser;
    public final String jdbcPassword;
    public final String centralHost;
    public final int centralPort;

    public BDConfig(ConfigManager config) {
        this.jdbcUrl = config.getString("bd.jdbc.url");
        this.jdbcUser = config.getString("bd.jdbc.user");
        this.jdbcPassword = config.getString("bd.jdbc.password");
        this.centralHost = config.getString("central.host");
        this.centralPort = config.getInt("central.rmi.port");
    }
}