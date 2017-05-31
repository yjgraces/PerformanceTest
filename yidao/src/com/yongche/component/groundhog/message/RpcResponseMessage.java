package com.yongche.component.groundhog.message;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class RpcResponseMessage extends ResponseBaseMessage {
    public int ttl;
    public String receiverType;
    public long receiverId;
    public long receiverDid;
    public String result;
    
    public RpcResponseMessage() {
        this.functionId = FUNCTION_ID_RPC_RESPONSE;
    }
    
    public int decode(byte[] payload, byte status) throws GroundhogMessageException {
        if (status != GroundhogMessage.STATUS_SUCCESS) {
            this.result = "{\"ret_code\":\"" + Integer.toString(GroundhogMessage.RPC_RET_CODE_SERVER_ERROR) + 
                    "\",\"ret_msg\":\"" + new String(payload, DEFAULT_CHARSET) + "\"}";
            return this.result.length();
        }
        
        int offset = super.decode(payload, status);
        
        ByteArrayInputStream byteStream = new ByteArrayInputStream(payload, offset, payload.length - offset);
        DataInputStream in = new DataInputStream(byteStream);
        
        int bodyLength = offset;        
        try {
            this.ttl = in.readInt();
            bodyLength += Integer.SIZE/Byte.SIZE;
            
            int messageLength = in.readInt();
            bodyLength += Integer.SIZE/Byte.SIZE;
            
            @SuppressWarnings("unused")
            short receiverCount = in.readShort();
            bodyLength += Short.SIZE/Byte.SIZE;
            
            byte[] receiverType = new byte[USERTYPE_LENGTH];
            in.readFully(receiverType);
            this.receiverType = new String(receiverType, DEFAULT_CHARSET);
            bodyLength += USERTYPE_LENGTH;
            
            this.receiverId = in.readLong();
            bodyLength += Long.SIZE/Byte.SIZE;
            
            this.receiverDid = in.readLong();
            bodyLength += Long.SIZE/Byte.SIZE;
            
            byte[] result = new byte[messageLength];
            in.readFully(result);
            this.result = new String(result, DEFAULT_CHARSET);
            bodyLength += messageLength;
        } catch (IOException e) {
            throw new GroundhogMessageException(e);
        }
        
        return bodyLength;
    }
}
