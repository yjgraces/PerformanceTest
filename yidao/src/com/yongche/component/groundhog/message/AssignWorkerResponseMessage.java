 package com.yongche.component.groundhog.message;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class AssignWorkerResponseMessage extends ResponseMessage {
    
    @SuppressWarnings("unused")
    private short serverCount = 1;
    @SuppressWarnings("unused")
    private short userCount = 1;
    
    public int maxAge;
    public String workerIp;
    public int port;
    @SuppressWarnings("unused")
    private long deviceId; 

    public AssignWorkerResponseMessage() {
        this.functionId = FUNCTION_ID_ASSGIN_WORKER;
    }
    
    public int decode(byte[] payload, byte status) throws GroundhogMessageException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(payload);
        DataInputStream in = new DataInputStream(byteStream);
        int bodyLength = 0;
        try {
        	this.maxAge = in.readInt();
        	bodyLength += Integer.SIZE/Byte.SIZE;
        	
            this.serverCount = in.readShort();
            bodyLength += Short.SIZE/Byte.SIZE;
            
            byte ipLength = in.readByte();
            bodyLength += Byte.SIZE/Byte.SIZE;
            
            byte[] ip = new byte[ipLength];
            in.readFully(ip);
            this.workerIp = new String(ip, DEFAULT_CHARSET);
            bodyLength += ipLength;
            
            this.port = in.readShort();
            bodyLength += Short.SIZE/Byte.SIZE;
            
            this.userCount = in.readShort();
            bodyLength += Short.SIZE/Byte.SIZE;
            
            this.userId = in.readLong();
            bodyLength += Long.SIZE/Byte.SIZE;
            
            this.deviceId = in.readLong();
            bodyLength += Long.SIZE/Byte.SIZE;
        } catch (IOException e) {
            throw new GroundhogMessageException(e);
        }
        
        return bodyLength;
    }
}
