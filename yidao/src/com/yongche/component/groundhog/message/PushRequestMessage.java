package com.yongche.component.groundhog.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class PushRequestMessage extends RequestBaseMessage {
    private short receiverCount = 1;
    
    public int ttl;  //unit: second
    public String pushMessage;
    public String receiverType;
    public long receiverId;
    public long receiverDid;
    
    public PushRequestMessage() {
        this.functionId = FUNCTION_ID_MESSAGE;
    }
    
    public byte[] encode() throws GroundhogMessageException {
        byte[] front = super.encode();
        
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteStream);
        
        try {
            out.write(front, 0, front.length);
            out.writeInt(this.ttl);
            int messageLength = this.pushMessage.getBytes(DEFAULT_CHARSET).length;
            if (messageLength > Integer.MAX_VALUE) {
                messageLength = Integer.MAX_VALUE;
            }
            out.writeInt(messageLength);
            out.writeShort(this.receiverCount);
            out.write(this.receiverType.getBytes(DEFAULT_CHARSET), 0, USERTYPE_LENGTH);
            out.writeLong(this.receiverId);
            out.writeLong(this.receiverDid);
            out.write(this.pushMessage.getBytes(DEFAULT_CHARSET), 0, messageLength);
            out.flush();
        } catch (IOException e) {
            throw new GroundhogMessageException(e);
        }
        
        return byteStream.toByteArray();
    }
    
    public int decode(byte[] payload, byte status) throws GroundhogMessageException {
        int offset = super.decode(payload, status);
        
        ByteArrayInputStream byteStream = new ByteArrayInputStream(payload, offset, payload.length - offset);
        DataInputStream in = new DataInputStream(byteStream);
        
        int bodyLength = offset;        
        try {
            this.ttl = in.readInt();
            bodyLength += Integer.SIZE/Byte.SIZE;
            
            int messageLength = in.readInt();
            bodyLength += Integer.SIZE/Byte.SIZE;
            
            this.receiverCount = in.readShort();
            bodyLength += Short.SIZE/Byte.SIZE;
            
            byte[] receiverType = new byte[USERTYPE_LENGTH];
            in.readFully(receiverType);
            this.receiverType = new String(receiverType, DEFAULT_CHARSET);
            bodyLength += USERTYPE_LENGTH;
            
            this.receiverId = in.readLong();
            bodyLength += Long.SIZE/Byte.SIZE;
            
            this.receiverDid = in.readLong();
            bodyLength += Long.SIZE/Byte.SIZE;
            
            byte[] pushMessage = new byte[messageLength];
            in.readFully(pushMessage);
            this.pushMessage = new String(pushMessage, DEFAULT_CHARSET);
            bodyLength += messageLength;
        } catch (IOException e) {
            throw new GroundhogMessageException(e);
        }
        
        return bodyLength;
    }
}
