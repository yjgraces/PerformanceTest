package letv.jmeter.yidao;

import java.io.IOException;

/**
 * Created by agile6v on 9/26/16.
 */
public abstract class PSFConnection {

    public abstract PSFConnectionInfo get(String serviceType) throws InterruptedException;

    public abstract void release(String serviceType, PSFConnectionInfo conn);

    public abstract void close() throws IOException;
}
