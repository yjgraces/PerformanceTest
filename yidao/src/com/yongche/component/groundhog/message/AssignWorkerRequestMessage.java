package com.yongche.component.groundhog.message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class AssignWorkerRequestMessage extends RequestBaseMessage {
    
    private short userCount = 1;
    
    public AssignWorkerRequestMessage() {
        this.functionId = FUNCTION_ID_REQUEST_WORKER;
    }
    
    public byte[] encode() throws GroundhogMessageException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteStream);
        
        try {
            out.writeShort(this.userCount);
            out.write(this.userType.getBytes(DEFAULT_CHARSET), 0, USERTYPE_LENGTH);
            out.writeLong(this.userId);
            out.writeLong(this.deviceId);
            out.flush();
        } catch (IOException e) {
            throw new GroundhogMessageException(e);
        }
        
        return byteStream.toByteArray();
    }
    
}
