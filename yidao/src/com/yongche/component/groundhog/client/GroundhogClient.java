package com.yongche.component.groundhog.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;

//import android.annotation.SuppressLint;

import com.yongche.component.groundhog.MessageException;
import com.yongche.component.groundhog.message.AcknowledgeMessage;
import com.yongche.component.groundhog.message.GetPublicKeyRequestMessage;
import com.yongche.component.groundhog.message.GetPublicKeyResponseMessage;
import com.yongche.component.groundhog.message.GroundhogMessage;
import com.yongche.component.groundhog.message.GroundhogParser;
import com.yongche.component.groundhog.message.LoginRequestMessage;
import com.yongche.component.groundhog.message.LoginResponseMessage;
import com.yongche.component.groundhog.message.SetSecretKeyRequestMessage;
import com.yongche.component.groundhog.push.CommonConfig;


/*
 * Not thread safe
 */
abstract public class GroundhogClient {
    
    private static long globalSequence;
    
    public int connectTimeout;
    public int responseTimeout;
    
    public String host;
    public int port;
    public GroundhogParser parser;
    
    public long deviceId;
    public String userType;
    public long userId;
    
    public String publicKey;
    public byte[] secretKey;
    
    protected Socket socket;
    protected DataInputStream in;
    protected DataOutputStream out;
    
    static public synchronized long getNextSequence() {
        if (globalSequence == Long.MAX_VALUE) {
            globalSequence = 0;
        }
        return ++globalSequence;
    }
    
    public void connect() throws IOException {
        this.socket = new Socket();
        this.socket.connect(new InetSocketAddress(this.host, this.port), connectTimeout);
        this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        try {
            this.socket.setTcpNoDelay(true);
        }
        catch (SocketException ex) {
            //ex.printStackTrace();
        }
    }
    
    public long login(String sessionId, String currentNetType) throws IOException, MessageException {
        this.publicKey = this.getPublicKey();
        this.secretKey = this.genSecretKey();
        this.setSecretKey();
        this.parser.setSecretKey(this.secretKey);
        
        LoginRequestMessage login = new LoginRequestMessage();
       
        long sequence = GroundhogClient.getNextSequence();
        login.sequenceId = sequence;
        login.deviceId = this.deviceId;
        login.userType = this.userType;
        login.userId = this.userId;
        login.sessionId = sessionId;
        login.currentNetType = currentNetType;
        
        parser.sendMsg(login, this.out);
        
        //set read timeout
        this.socket.setSoTimeout(this.responseTimeout);
        GroundhogMessage nextMsg = parser.getMsg(in);
        //reset read timeout
        this.socket.setSoTimeout(0);

        if (sequence != nextMsg.sequenceId) {
            throw new ClientException("The sequence of response message from server is wrong when login");
        }
        
        if (GroundhogMessage.STATUS_SUCCESS == nextMsg.status) {
            if (!(nextMsg instanceof LoginResponseMessage)) {
                throw new ClientException("The response message from server is not login response when login, message: " + nextMsg);
            }
        } else {
            if (!(nextMsg instanceof AcknowledgeMessage)) {
                throw new ClientException("The response message from server is not ACK response when login, message: " + nextMsg);
            }

            if (GroundhogMessage.STATUS_EPERM == nextMsg.status) {
                //PushService.onLoginFailed();
            }
            throw new ClientException("fail to log in, status: " + nextMsg.status + ", message: " + ((AcknowledgeMessage)nextMsg).message);
        }
        
        return ((long)((LoginResponseMessage)nextMsg).currentTime) * 1000;
    }
    
    public void close() {
        try {
            if (this.in != null) {
                this.in.close();
                this.in = null;
            }
            
            if (this.out != null) {
                this.out.close();
                this.out = null;
            }
            
            if (this.socket != null) {
                this.socket.close();
                this.socket = null;
            }
        } catch (Exception e) {
            this.socket = null;
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        if (this.socket == null
                || !this.socket.isConnected()
                || this.socket.isClosed()) {
            return false;
        }
        
        return true;
    }
    
    public static ManagerClient buildManagerClient() {
        ManagerClient managerConn = new ManagerClient();
        managerConn.parser = new GroundhogParser();
        
        return managerConn;
    }
    
    public static WorkerClient buildWorkerClient() {
        WorkerClient workerConn = new WorkerClient();
        workerConn.parser = new GroundhogParser();
        
        return workerConn;
    }
    
    private String getPublicKey() throws IOException, MessageException {
        GetPublicKeyRequestMessage message = new GetPublicKeyRequestMessage();
        
        long sequence = GroundhogClient.getNextSequence();
        message.sequenceId = sequence;

        parser.sendMsg(message, this.out);
        
        //set read timeout
        this.socket.setSoTimeout(this.responseTimeout);
        GroundhogMessage nextMsg = parser.getMsg(in);
        //reset read timeout
        this.socket.setSoTimeout(0);
        
        if (!(nextMsg instanceof GetPublicKeyResponseMessage)) {
            throw new ClientException("The response message from server is not GetPublicKeyResponseMessage, message: " + nextMsg);
        }
        
        if (sequence != nextMsg.sequenceId) {
            throw new ClientException("The sequence of response message is wrong when getting public key");
        }
        
        if (GroundhogMessage.STATUS_SUCCESS != nextMsg.status) {
            throw new ClientException("fail to get public key, status :" + nextMsg.status);
        }
        
        return ((GetPublicKeyResponseMessage)nextMsg).publicKey;
    }

    private void setSecretKey() throws IOException, MessageException {
        SetSecretKeyRequestMessage message = new SetSecretKeyRequestMessage();
        
        long sequence = GroundhogClient.getNextSequence();
        message.sequenceId = sequence;
        message.publicKey = this.publicKey;
        message.secretKey = this.secretKey;

        parser.sendMsg(message, this.out);
        
        //set read timeout
        this.socket.setSoTimeout(this.responseTimeout);
        GroundhogMessage nextMsg = parser.getMsg(in);
        //reset read timeout
        this.socket.setSoTimeout(0);
        
        if (!(nextMsg instanceof AcknowledgeMessage)) {
            throw new ClientException("The response message from server is not AcknowledgeMessage, message: " + nextMsg);
        }
        
        if (sequence != nextMsg.sequenceId) {
            throw new ClientException("The sequence of response message is wrong when setting secret key");
        }
        
        if (GroundhogMessage.STATUS_SUCCESS != nextMsg.status) {
            throw new ClientException("fail to set secret key, status :" + nextMsg.status);
        }
        
        return;
    }

    //@SuppressLint("TrulyRandom")
    private byte[] genSecretKey() {
        Key secretKey = null;
        byte[] keys;
        try {
            KeyGenerator generator = KeyGenerator.getInstance(CommonConfig.KEY_GENERATE_ALGORITHM);
            generator.init(new SecureRandom());
            secretKey = generator.generateKey();
            generator = null;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        
        keys = secretKey.getEncoded();
        keys[0] &= 0x7F;
        //Logger.info(this.getClass().getName(), "secret key is " + GroundhogParser.bytesToHex(keys));
        return keys;
    }
}
