package letv.jmeter.yidao;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by agile6v on 9/26/16.
 */
public class PSFConnectionPool extends PSFConnection {

    private int maxConnNum;
    private ConcurrentHashMap<String, Object> connectionHash = null;
    private int waitTimeout;

    public PSFConnectionPool(int maxConnNum, int timeout) {
        this.maxConnNum = maxConnNum;
        this.connectionHash = new ConcurrentHashMap();
        this.waitTimeout = timeout;
    }

    @Override
    public PSFConnectionInfo get(String serviceType) throws InterruptedException {

        PSFConnectionInfo connInfo = null;
        ArrayBlockingQueue<PSFConnectionInfo> idleQueue = null;

        idleQueue = (ArrayBlockingQueue<PSFConnectionInfo>) connectionHash.get(serviceType);
        if (idleQueue == null) {
            synchronized (this) {
                idleQueue = (ArrayBlockingQueue<PSFConnectionInfo>) connectionHash.get(serviceType);
                if (idleQueue == null) {
                    idleQueue = new ArrayBlockingQueue<PSFConnectionInfo>(this.maxConnNum, true);
                    connInfo = new PSFConnectionInfo();
                    connectionHash.put(serviceType, idleQueue);
                    return connInfo;
                }
            }
        }

        if (idleQueue.size() > 0) {
            connInfo = idleQueue.poll(this.waitTimeout, TimeUnit.MILLISECONDS);
        }

        if (connInfo == null) {
            connInfo = new PSFConnectionInfo();
        }

        return connInfo;
    }

    @Override
    public void release(String serviceType, PSFConnectionInfo conn) {
        ArrayBlockingQueue<PSFConnectionInfo> idleQueue =
                (ArrayBlockingQueue<PSFConnectionInfo>) connectionHash.get(serviceType);
        boolean ret = idleQueue.offer(conn);
        if (ret == false) {
            conn.close();
        }
    }

    @Override
    public void close() throws IOException {
        Set setOfKeys = connectionHash.keySet();
        Iterator iterator = setOfKeys.iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            ArrayBlockingQueue<PSFConnectionInfo> idleQueue =
                    (ArrayBlockingQueue<PSFConnectionInfo>) connectionHash.get(key);

            Iterator<PSFConnectionInfo> iter = idleQueue.iterator();
            while (iter.hasNext()) {
                PSFConnectionInfo conn = (PSFConnectionInfo) iter.next();
                if (conn.sock != null) {
                    conn.sock.close();
                    conn.sock = null;
                }
            }
        }
        this.connectionHash.clear();
        this.connectionHash = null;
    }
}
