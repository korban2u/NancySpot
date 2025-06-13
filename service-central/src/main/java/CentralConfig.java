import utils.ConfigManager;


public class CentralConfig {
    public final int rmiPort;
    public final int httpPort;
    public final boolean httpsEnabled;
    public final int httpsPort;
    public final String keystorePath;
    public final String keystorePassword;

    public CentralConfig(ConfigManager config) {
        this.rmiPort = config.getInt("central.rmi.port");
        this.httpPort = config.getInt("central.http.port");
        this.httpsEnabled = config.getBoolean("central.https.enabled");
        this.httpsPort = config.getInt("central.https.port");
        this.keystorePath = config.getString("central.keystore.path");
        this.keystorePassword = config.getString("central.keystore.password");
    }
}