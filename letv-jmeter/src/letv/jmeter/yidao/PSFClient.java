package letv.jmeter.yidao;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;

/**
 * Created by agile6v on 8/17/16.
 */
public class PSFClient {

    public static final String PSF_DEFAULT_CHARSET              = "UTF-8";

    public static final int PSF_MAX_SERVICE_TYPE_SIZE           = 16;
    public static final int SERVICE_CENTER_HOST_NUMBER_MAX      = 8;
    public static final int PSF_PROTO_MAGIC_NUMBER              = 0x23232323;

    public static final int PSF_PROTO_FID_ALLOC_SERVER          = 69;
    public static final int PSF_PROTO_FID_CLIENT_JOIN           = 71;
    public static final int PSF_PROTO_FID_RPC_REQ_NO_HEADER     = 75;
    public static final int PSF_PROTO_FID_RPC_REQ_WITH_HEADER   = 81;

    public static final int PSF_PROTO_HEADER_LEN = 10;

    private PSFConnection psfConnection;
    private PSFClientContext m_context;

    public static class PSFRPCRequestData
    {
        public String service_uri;
        public String data;
        public Hashtable headers;
    }

    public class PSFProtoHeader
    {
        public int magic_number;
        public byte func_id;
        public byte status;
        public int body_len;
    }

    /**
     * issues request of psf-rpc
     * @param serviceType service type(e.g. device¡¢dispatch)
     * @param request rpc request
     * @return String response of rpc request
     */
    public String call(String serviceType, PSFRPCRequestData request) throws Exception
    {
        return this.call(serviceType, request, 0);
    }

    /**
     * issues request of psf-rpc
     * @param serviceType service type(e.g. device¡¢dispatch)
     * @param request rpc request
     * @param timeout rpc network timeout (for read or write socket)
     * @return String response of rpc request
     */
    public String call(String serviceType, PSFRPCRequestData request,
                          int timeout) throws Exception
    {
        boolean restore = false;
        int default_network_timeout;
        PSFConnectionInfo clientInfo;
        String response = new String();

        if (m_context == null) {
            throw new Exception("context may not have been initialized.");
        }

        if (serviceType.length() > PSF_MAX_SERVICE_TYPE_SIZE) {
            throw new Exception("serivce_type size is greater than 16.");
        }

        if ((PSF_PROTO_HEADER_LEN + 2/* service_uri_len */ + request.service_uri.length()
                + request.data.length()) > m_context.max_pkg_size)
        {
            throw new Exception("the packet size exceeds max_pkg_size." + m_context.max_pkg_size);
        }

        default_network_timeout = m_context.network_timeout;
        if (default_network_timeout != timeout && timeout > 0) {
            m_context.network_timeout = timeout;
            restore = true;
        }

        try {
            clientInfo = getServerManagerConn(m_context, serviceType);
            response = executeRpcCall(clientInfo, request, serviceType);
        } catch (Exception e) {
            throw e;
        }

        if (restore) {
            m_context.network_timeout = default_network_timeout;
        }

        psfConnection.release(serviceType, clientInfo);

        return response;
    }

    /**
     * close all sockets
     */
    public void close() throws IOException {
        psfConnection.close();
    }

    /**
     * creates a psf client with the host address of the service_center server
     * @param serviceCenter host address of the service center.
     */
    public PSFClient(String[] serviceCenter) throws Exception
    {
        final int connect_timeout = 2 * 1000;
        final int network_timeout = 30 * 1000;
        final int max_pkg_size = 64 * 1024;
        final int reconnect_times = 3;
        final int waitTimeOut = 10;
        this.init(serviceCenter, connect_timeout, network_timeout,
                max_pkg_size, reconnect_times, 0, waitTimeOut);
    }

    /**
     * creates a psf client with the specified params
     * @param serviceCenter     host address of the service center.
     * @param connect_timeout   timeout for connecting to the server which is service_center or server_manager, in milliseconds.
     * @param network_timeout   timeout for socket read or write, in milliseconds.
     * @param max_pkg_size      The maximum package size that can be send to server. (header + body)
     * @param reconnect_times   times to reconnect to the server_manager.
     * @param maxConnNum        using single connection mode if the maxConnNum is zero, otherwise using connection pool mode.
     * @param waitTimeout       timeout for waiting the idle connection queue if connection pool mode is enabled, in milliseconds.
     */
    public PSFClient(String[] serviceCenter, int connect_timeout, int network_timeout,
                       int max_pkg_size, int reconnect_times, int maxConnNum, int waitTimeout) throws Exception
    {
        this.init(serviceCenter, connect_timeout, network_timeout,
                max_pkg_size, reconnect_times, maxConnNum, waitTimeout);
    }

    private void init(String[] serviceCenter, int connect_timeout, int network_timeout,
            int max_pkg_size, int reconnect_times, int maxConnNum, int waitTimeout) throws Exception
    {
        if (serviceCenter.length < 1 || serviceCenter.length > SERVICE_CENTER_HOST_NUMBER_MAX) {
            throw new Exception("the \"server_center\" is invalid, "
                    + "must be greater than 1 and less than " + SERVICE_CENTER_HOST_NUMBER_MAX);
        }

        m_context = new PSFClientContext(serviceCenter, connect_timeout,
                network_timeout, max_pkg_size, reconnect_times, maxConnNum);

        if (maxConnNum <= 0) {
            psfConnection = new PSFConnectionSingle();
        } else {
            psfConnection = new PSFConnectionPool(maxConnNum, waitTimeout);
        }
    }

    public void reconnectServerManager(PSFConnectionInfo info,
                                           String service_type) throws IOException
    {
        boolean ret = false;
        PSFProtoHeader header;

        for (int i = 0; i < m_context.reconnect_times; i++) {
            try {
                info.connect(m_context.connect_timeout, m_context.network_timeout);
                ret = true;
                break;
            } catch (IOException ex) {
                ex.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException err) {}
            }
        }

        if (ret == false) {
            info.reallocate = !info.forbidden_alloc_server;
            throw new IOException("failed to connect server manager");
        }

        try {
            int len = buildClientJoinPackage(info.send_recv_buf, service_type);

            OutputStream out = info.sock.getOutputStream();
            out.write(info.send_recv_buf, 0, len);

            InputStream in = info.sock.getInputStream();
            header = recvHeader(in);
            if (header.status != 0) {
                throw new IOException("recv header status != 0 from server manager.");
            }
        } catch (IOException e) {
            info.reallocate = !info.forbidden_alloc_server;
            info.close();
            throw e;
        }
    }

    public boolean connectServiceCenter(PSFClientContext context,
                                            PSFConnectionInfo conn)
    {
        int host_id;
        boolean ret = false;

        host_id = context.last_connect_host_id + 1;
        for (int i = 0; i < context.service_center_host.length; i++) {
            host_id = (host_id + i) % context.service_center_host.length;
            conn.port = context.service_center_host[host_id].port;
            conn.ip_addr = context.service_center_host[host_id].ip_addr;

            try {
                conn.connect(m_context.connect_timeout, m_context.network_timeout);
                context.last_connect_host_id = host_id;
                ret = true;
                break;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return ret;
    }

    public byte[] readFully(InputStream in, int expectLength) throws IOException
    {
        int remain;
        int bytes;
        byte[] data  = new byte[expectLength];
        remain = expectLength;
        while (remain > 0 && (bytes=in.read(data, expectLength - remain, remain)) > 0) {
            remain -= bytes;
        }

        if (remain != 0) {
            throw new IOException("connection closed");
        }

        return data;
    }

    public PSFProtoHeader recvHeader(InputStream in) throws IOException
    {
        int bytes;
        byte[] recv_buff = this.readFully(in, 10);

        PSFProtoHeader header = new PSFProtoHeader();
        header.magic_number = buff2int(recv_buff, 0);
        header.func_id = recv_buff[4];
        header.status = recv_buff[5];
        header.body_len = buff2int(recv_buff, 6);

        if (header.magic_number != PSF_PROTO_MAGIC_NUMBER) {
            throw new IOException("recv package magic_number " + header.magic_number + " != " + PSF_PROTO_MAGIC_NUMBER);
        }

        if (header.body_len < 0) {
            throw new IOException("recv package body_len " + header.body_len + " < 0");
        }

        return header;
    }


    public void allocateFromServiceCenter(PSFConnectionInfo clientInfo,
                                              String serivce_type) throws IOException
    {
        boolean ret;
        byte[] recvBuff = null;
        PSFConnectionInfo serviceCenterConn = new PSFConnectionInfo();

        ret = connectServiceCenter(m_context, serviceCenterConn);
        if (!ret) {
            throw new IOException("failed to connect to service center");
        }

        try {
            int len = buildAllocatePackage(clientInfo.send_recv_buf, serivce_type);

            OutputStream out = serviceCenterConn.sock.getOutputStream();
            out.write(clientInfo.send_recv_buf, 0, len);

            InputStream in = serviceCenterConn.sock.getInputStream();

            PSFProtoHeader header = recvHeader(in);
            if (header.body_len > 0) {
                recvBuff = this.readFully(in, header.body_len);
            }

            if (header.status != 0) {
                String error;
                error = "recv package status " + header.status + " != 0";
                if (recvBuff != null) {
                    error += ", error info: " + new String(recvBuff, PSF_DEFAULT_CHARSET);
                }
                throw new IOException(error);
            }

            if (header.body_len <= 3) {
                throw new IOException("recv package body_len " + header.body_len + " <= 3");
            }

            /* server manager ip & port */
            clientInfo.port = buff2short(recvBuff, 0);
            clientInfo.ip_addr = new String(recvBuff, 3, recvBuff[2]);
        } finally {
            serviceCenterConn.close();
        }
    }

    public void reconnectServerManagerEx(PSFConnectionInfo info,
                                            String service_type) throws IOException
    {
        try {
            reconnectServerManager(info, service_type);
        } catch (IOException e) {
            checkAllocAndConnectServerManager(info, service_type);
        }
    }

    public int checkAllocAndConnectServerManager(PSFConnectionInfo info,
                                                        String service_type) throws IOException
    {
        if (info.reallocate) {
            info.close();
            allocateFromServiceCenter(info, service_type);
            info.reallocate = false;
            info.forbidden_alloc_server = false;
        }

        reconnectServerManager(info, service_type);
        return 0;
    }

    public PSFConnectionInfo getServerManagerConn(PSFClientContext context,
                                                  String service_type) throws IOException, InterruptedException {
        PSFConnectionInfo connInfo;

        connInfo = psfConnection.get(service_type);
        if (connInfo == null) {
            throw new IOException("connection is insufficient.");
        }

        if (connInfo.sock == null) {
            if (connInfo.send_recv_buf == null) {
                connInfo.send_recv_buf = new byte[context.max_pkg_size];
            }
            checkAllocAndConnectServerManager(connInfo, service_type);
        }

        return connInfo;
    }

    public String executeRpcCall(PSFConnectionInfo clientInfo,
                                  PSFRPCRequestData request, String service_type) throws IOException
    {
        PSFProtoHeader header;
        byte[] data;
        String response = null;
        boolean retry = true;
        InputStream in;

        do {
            try {
                int len = buildRpcRequestPackage(clientInfo.send_recv_buf, request);

                try {
                    OutputStream out = clientInfo.sock.getOutputStream();
                    out.write(clientInfo.send_recv_buf, 0, len);
                } catch (IOException e) {
                    if (retry) {
                        retry = false;
                        reconnectServerManagerEx(clientInfo, service_type);
                        continue;
                    }
                    throw e;
                }

                try {
                    in = clientInfo.sock.getInputStream();
                    header = recvHeader(in);
                } catch (IOException e) {
                    if (e instanceof SocketTimeoutException) {
                        clientInfo.close();
                    } else if (retry) {
                        retry = false;
                        clientInfo.reallocate = !clientInfo.forbidden_alloc_server;
                        checkAllocAndConnectServerManager(clientInfo, service_type);
                        continue;
                    }
                    throw e;
                }

                if (header.status != 0) {
                    throw new IOException("failed to connect to service manager, status: " + header.status);
                }

                if (header.body_len > 0) {
                    data = this.readFully(in, header.body_len);
                    response = new String(data, PSF_DEFAULT_CHARSET);
                }
            } catch (IOException e) {
                clientInfo.close();
                throw e;
            }

            break;
        } while (true);

        return response;
    }

    public int buildRpcRequestPackage(byte[] recvSendBuff,
                                         PSFRPCRequestData request) throws UnsupportedEncodingException
    {
        int header_count;
        int header_len;
        int func_id;
        int body_len;
        byte[] buff;
        byte[] header;
        byte[] service_uri;
        byte[] data;
        StringBuilder str = new StringBuilder();

        if (request.headers != null && (header_count = request.headers.size()) > 0) {
            Enumeration e = request.headers.keys();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                str.append(key + "=" + request.headers.get(key));
                if (--header_count > 0) {
                    str.append("&");
                }
            }
            header = str.toString().getBytes(PSF_DEFAULT_CHARSET);
            header_len = header.length;
        } else {
            header = null;
            header_len = 0;
        }

        data = request.data.getBytes(PSF_DEFAULT_CHARSET);
        service_uri = request.service_uri.getBytes(PSF_DEFAULT_CHARSET);

        body_len = 2 + service_uri.length + data.length;
        if (header_len == 0) {
            func_id = PSF_PROTO_FID_RPC_REQ_NO_HEADER;
        } else {
            func_id = PSF_PROTO_FID_RPC_REQ_WITH_HEADER;
            body_len += 2 + header_len;
        }

        int offset = buildPsfProtoHeader(recvSendBuff,
                func_id, 0, body_len);

        if (header_len > 0) {
            buff = short2buff((short) header_len);
            System.arraycopy(buff, 0, recvSendBuff, offset, buff.length);
            offset += buff.length;
        }

        buff = short2buff((short)service_uri.length);
        System.arraycopy(buff, 0, recvSendBuff, offset, buff.length);
        offset += buff.length;

        System.arraycopy(service_uri, 0, recvSendBuff, offset, service_uri.length);
        offset += service_uri.length;

        System.arraycopy(data, 0, recvSendBuff, offset, data.length);
        offset += data.length;

        if (header_len > 0) {
            System.arraycopy(header, 0, recvSendBuff, offset, header.length);
            offset += header.length;
        }

        return offset;
    }

    public int buildPsfProtoHeader(byte[] header, int func_id, int status, int body_len)
    {
        byte[] hex_len;
        int offset;

        offset = 0;
        hex_len = int2buff(PSF_PROTO_MAGIC_NUMBER);
        System.arraycopy(hex_len, 0, header, offset, hex_len.length);
        offset += hex_len.length;
        header[offset++] = (byte)func_id;
        header[offset++] = (byte)status;
        hex_len = int2buff(body_len);
        System.arraycopy(hex_len, 0, header, offset, hex_len.length);
        offset += hex_len.length;

        return offset;
    }

    public int buildAllocatePackage(byte[] recvSendBuff, String service_type)
            throws UnsupportedEncodingException
    {
        byte[] data;
        int body_len = 1/* service_type_len(1byte) */ + service_type.length();

        int offset = buildPsfProtoHeader(recvSendBuff,
                PSF_PROTO_FID_ALLOC_SERVER, 0, body_len);

        recvSendBuff[offset++] = (byte) service_type.length();
        data = service_type.getBytes(PSF_DEFAULT_CHARSET);
        System.arraycopy(data, 0, recvSendBuff, offset, data.length);
        offset += data.length;

        return offset;
    }

    public int buildClientJoinPackage(byte[] recvSendBuff, String service_type)
            throws UnsupportedEncodingException
    {
        byte[] data;

        int body_len = 1 /* service_type_len(1byte) */ + service_type.length();

        int offset = buildPsfProtoHeader(recvSendBuff,
                PSF_PROTO_FID_CLIENT_JOIN, 0, body_len);

        recvSendBuff[offset++] = (byte) service_type.length();

        data = service_type.getBytes(PSF_DEFAULT_CHARSET);
        System.arraycopy(data, 0, recvSendBuff, offset, data.length);
        offset += data.length;

        return offset;
    }

    /**
     * buff convert to int
     * @param bs the buffer (big-endian)
     * @param offset the start position based 0
     * @return int number
     */
    public static int buff2int(byte[] bs, int offset)
    {
        return  (((int)(bs[offset] >= 0 ? bs[offset] : 256+bs[offset])) << 24) |
                (((int)(bs[offset+1] >= 0 ? bs[offset+1] : 256+bs[offset+1])) << 16) |
                (((int)(bs[offset+2] >= 0 ? bs[offset+2] : 256+bs[offset+2])) <<  8) |
                ((int)(bs[offset+3] >= 0 ? bs[offset+3] : 256+bs[offset+3]));
    }

    /**
     * int convert to buff (big-endian)
     * @param n number
     * @return 4 bytes buff
     */
    public static byte[] int2buff(int n)
    {
        byte[] bs;

        bs = new byte[4];
        bs[0] = (byte)((n >> 24) & 0xFF);
        bs[1] = (byte)((n >> 16) & 0xFF);
        bs[2] = (byte)((n >> 8) & 0xFF);
        bs[3] = (byte)(n & 0xFF);

        return bs;
    }

    /**
     * buff convert to int
     * @param bs the buffer (big-endian)
     * @param offset the start position based 0
     * @return int number
     */
    public static int buff2short(byte[] bs, int offset)
    {
        return (((int)(bs[offset] >= 0 ? bs[offset] : 256+bs[offset])) << 8) |
               ((int)(bs[offset+1] >= 0 ? bs[offset+1] : 256+bs[offset+1]));
    }

    public static byte[] short2buff(short n)
    {
        byte[] b = new byte[2];
        b[1] = (byte) (n & 0xff);
        b[0] = (byte) ((n >> 8) & 0xff);
        return b;
    }
}
