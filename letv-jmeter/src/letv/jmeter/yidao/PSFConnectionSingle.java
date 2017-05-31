package letv.jmeter.yidao;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by agile6v on 9/26/16.
 */
public class PSFConnectionSingle extends PSFConnection {

    public HashMap<String, PSFConnectionInfo> server_manager_hash;

    public PSFConnectionSingle() {
        this.server_manager_hash = new HashMap<>();
    }

    @Override
    public PSFConnectionInfo get(String serviceType) {
        PSFConnectionInfo connInfo = server_manager_hash.get(serviceType);
        if (connInfo == null) {
            connInfo = new PSFConnectionInfo();
            server_manager_hash.put(serviceType, connInfo);
        }

        return connInfo;
    }

    @Override
    public void release(String serviceType, PSFConnectionInfo conn) {

    }

    @Override
    public void close() throws IOException {
        if (this.server_manager_hash == null) {
            return;
        }
        Set<Map.Entry<String, PSFConnectionInfo>> entrySet;
        Iterator<Map.Entry<String, PSFConnectionInfo>> iterator;
        entrySet = this.server_manager_hash.entrySet();
        iterator = entrySet.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, PSFConnectionInfo> entry = iterator.next();
            PSFConnectionInfo managerConn = entry.getValue();
            managerConn.close();
        }
        server_manager_hash.clear();
        this.server_manager_hash = null;
    }
}
