package letv.jmeter.yidao;

/**
 * Created by agile6v on 9/26/16.
 */
public class PSFClientContext
{
    public int connect_timeout;
    public int network_timeout;
    public int reconnect_times;
    public int last_connect_host_id;
    public int max_pkg_size;
    public PSFConnectionInfo[] service_center_host;
    public int maxConnNum;

    public PSFClientContext(String[] serviceCenter, int connect_timeout,
                            int network_timeout, int max_pkg_size, int reconnect_times, int maxConnNum) throws Exception
    {
        this.maxConnNum = maxConnNum;
        this.connect_timeout = connect_timeout;
        this.network_timeout = network_timeout;
        this.reconnect_times = reconnect_times;
        this.max_pkg_size = max_pkg_size;
        this.last_connect_host_id = 0;
        this.service_center_host = new PSFConnectionInfo[serviceCenter.length];
        for (int i = 0; i < serviceCenter.length; i++) {
            String[] server = serviceCenter[i].split("\\:", 2);
            if (server.length != 2) {
                throw new Exception("\"serviceCenter\" is invalid, the correct format is host:port");
            }

            this.service_center_host[i] = new PSFConnectionInfo(Integer.parseInt(server[1].trim()), server[0].trim());
        }
    }
}
