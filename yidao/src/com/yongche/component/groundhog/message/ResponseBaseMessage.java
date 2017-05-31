package com.yongche.component.groundhog.message;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

abstract public class ResponseBaseMessage extends ResponseMessage {
    public long deviceId;
    
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
