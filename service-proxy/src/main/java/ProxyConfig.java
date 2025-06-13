import utils.ConfigManager;

public class ProxyConfig {
    public final boolean useIutProxy;
    public final String proxyHost;
    public final String proxyPort;
    public final String centralHost;
    public final int centralPort;

    public ProxyConfig(ConfigManager config) {
        this.useIutProxy = config.getBoolean("proxy.use.iut.proxy");
        this.proxyHost = config.getString("proxy.iut.host");
        this.proxyPort = config.getString("proxy.iut.port");
        this.centralHost = config.getString("central.host");
        this.centralPort = config.getInt("central.rmi.port");
    }
}