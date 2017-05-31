package com.yongche.component.groundhog.message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class RpcRequestMessage extends RequestBaseMessage {
    private short receiverCount = 1;
    
    public int ttl;
    public String receiverType;
    public long receiverId;
    public long receiverDid;
    public String serviceType;
    public String serviceUri;
    public String data;
    
    public RpcRequestMessage() {
        this.functionId = FUNCTION_ID_RPC;
    }
    
    private String formatNumber(int n, boolean prefix_space, int fixed_len)
    {
    	String padding;
    	String s = String.valueOf(n);
    	int len = s.length();
    	if (len < fixed_len) {
    		char[] bs;
    		bs = new char[fixed_len - len];
    		Arrays.fill(bs, ' ');
    		padding = new String(bs);
    	} else {
    		padding = "";
    	}
    	
    	if (prefix_space) {
    		return " " + s + padding;
    	} else {
    		return s + padding;
    	}
    }
    
    public byte[] encode() throws GroundhogMessageException {
        byte[] front = super.encode();
        
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteStream);
        
        int serviceTypeLength = this.serviceType.getBytes(DEFAULT_CHARSET).length;
        int serviceUriLength = this.serviceUri.getBytes(DEFAULT_CHARSET).length;
        int dataLength = this.data.getBytes(DEFAULT_CHARSET).length;
        
        try {
            out.write(front, 0, front.length);
            out.writeInt(this.ttl);
            int messageLength = 4 + serviceTypeLength + 7 + serviceUriLength + 9 + dataLength + 6;
            if (messageLength > Integer.MAX_VALUE) {
                messageLength = Integer.MAX_VALUE;
            }
            out.writeInt(messageLength);
            out.writeShort(this.receiverCount);
            out.write(this.receiverType.getBytes(DEFAULT_CHARSET), 0, USERTYPE_LENGTH);
            out.writeLong(this.receiverId);
            out.writeLong(this.receiverDid);

            out.write(this.formatNumber(serviceTypeLength, false, 4).getBytes(DEFAULT_CHARSET));
            out.write(this.serviceType.getBytes(DEFAULT_CHARSET), 0, serviceTypeLength);
            out.write(this.formatNumber(serviceUriLength, true, 6).getBytes(DEFAULT_CHARSET));
            out.write(this.serviceUri.getBytes(DEFAULT_CHARSET), 0, serviceUriLength);
            out.write(this.formatNumber(dataLength, true, 8).getBytes(DEFAULT_CHARSET));
            out.write(this.data.getBytes(DEFAULT_CHARSET), 0, dataLength);
            out.write(this.formatNumber(0, true, 5).getBytes(DEFAULT_CHARSET));
            out.flush();
        } catch (IOException e) {
            throw new GroundhogMessageException(e);
        }
        
        return byteStream.toByteArray();
    }
}
