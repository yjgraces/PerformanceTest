package com.yongche.component.groundhog.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


abstract public class RequestBaseMessage extends RequestMessage {
    public long deviceId;
    
    public byte[] encode() throws GroundhogMessageException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteStream);
        
        try {
            out.write(this.userType.getBytes(DEFAULT_CHARSET));
            out.writeLong(this.userId);
            out.writeLong(this.deviceId);
            out.flush();
        } catch (IOException e) {
            throw new GroundhogMessageException(e);
        }
        return byteStream.toByteArray();
    }
    
    public int decode(byte[] payload, byte status) throws GroundhogMessageException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(payload);
        DataInputStream in = new DataInputStream(byteStream);
        int bodyLength = 0;
        
        try {
            byte[] userType = new byte[USERTYPE_LENGTH];
            in.readFully(userType);
            this.userType = new String(userType, DEFAULT_CHARSET);
            bodyLength += USERTYPE_LENGTH;
            
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
