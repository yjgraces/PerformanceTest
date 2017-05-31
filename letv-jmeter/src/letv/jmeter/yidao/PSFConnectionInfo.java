package letv.jmeter.yidao;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by agile6v on 9/26/16.
 */
public class PSFConnectionInfo
{
    Socket sock;
    int port;
    String ip_addr;
    boolean reallocate;
    boolean forbidden_alloc_server;
    byte[] send_recv_buf;

    public PSFConnectionInfo(int port, String ip_addr) {
        this.port = port;
        this.ip_addr = ip_addr;
        this.reallocate = true;
    }

    public PSFConnectionInfo()
    {
        this.reallocate = true;
    }

    public void connect(int connectTimeout, int networkTimeout) throws IOException
    {
        if (this.sock == null) {
            this.sock = new Socket();
            
            try {
                this.sock.setSoTimeout(networkTimeout);
                this.sock.connect(new InetSocketAddress(this.ip_addr, this.port), connectTimeout);
            } catch (IOException ex) {
                this.close();
                throw ex;
            }
        }
    }

    public void close()
    {
        try {
            if (this.sock != null) {
                this.sock.close();
                this.sock = null;
            }
        } catch (IOException ex) {
        }
    }

    protected void finalize()
    {
        this.close();
    }
}