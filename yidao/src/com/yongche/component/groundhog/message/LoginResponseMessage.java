package com.yongche.component.groundhog.message;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class LoginResponseMessage extends ResponseMessage {

    public int currentTime;
    
    public LoginResponseMessage() {
        this.functionId = GroundhogMessage.FUNCTION_ID_LOGIN_RESPONSE;
    }
    
    public int decode(byte[] payload, byte status) throws GroundhogMessageException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(payload);
        DataInputStream in = new DataInputStream(byteStream);
        int bodyLength = 0;
        
        try {
            this.currentTime = in.readInt();
            bodyLength += Integer.SIZE/Byte.SIZE;
        } catch (IOException e) {
            throw new GroundhogMessageException(e);
        }
        
        return bodyLength;
    }
}
