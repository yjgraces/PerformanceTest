package com.yongche.component.groundhog.message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.yongche.component.groundhog.push.CommonConfig;

public class LoginRequestMessage extends RequestBaseMessage {
    public String sessionId;
    public int workerAge;
    public String currentNetType;
    
    public LoginRequestMessage() {
        this.functionId = FUNCTION_ID_LOGIN;
    }
    
    @Override
    public byte[] encode() throws GroundhogMessageException {
        byte[] front = super.encode();
        
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteStream);
        
        byte sessionIdLength = (byte) this.sessionId.length();
        if (sessionIdLength > Byte.MAX_VALUE) {
            sessionIdLength = Byte.MAX_VALUE;
        }
        
        try {
            out.write(front, 0, front.length);
            out.writeByte(sessionIdLength);
            out.write(this.sessionId.getBytes(DEFAULT_CHARSET), 0, sessionIdLength);
            out.writeInt(this.workerAge);
            out.writeShort(CommonConfig.VERSION);
            
            String extraData = "os=" + CommonConfig.OS_NAME + "&net=" + currentNetType
            		+ "&abi=" + "&uid=";
            short extraDataLength = (short) extraData.length();
            if (extraDataLength > Short.MAX_VALUE) {
                extraDataLength = Short.MAX_VALUE;
            }
            out.writeShort(extraDataLength);
            out.write(extraData.getBytes(DEFAULT_CHARSET), 0, extraDataLength);
            out.flush();
        } catch (IOException e) {
            throw new GroundhogMessageException(e);
        }
        
        return byteStream.toByteArray();
    }
}
