package com.yongche.component.groundhog.message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UnsubscribeRequestMessage extends RequestBaseMessage {
    public short unsubscribeMessageType;
    public long driverId;
    public long orderId;
    
    public UnsubscribeRequestMessage() {
        this.functionId = FUNCTION_ID_UNSUBSCRIBE;
    }
    
    @Override
    public byte[] encode() throws GroundhogMessageException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteStream);
        
        try {
            out.writeShort(this.unsubscribeMessageType);
            out.writeLong(this.driverId);
            out.writeLong(this.orderId);
            out.flush();
        } catch (IOException e) {
            throw new GroundhogMessageException(e);
        }
        
        return byteStream.toByteArray();
    }
}
