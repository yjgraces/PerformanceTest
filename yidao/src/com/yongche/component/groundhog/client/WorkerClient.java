package com.yongche.component.groundhog.client;

import java.io.IOException;

import com.yongche.component.groundhog.MessageException;
import com.yongche.component.groundhog.message.AcknowledgeMessage;
import com.yongche.component.groundhog.message.GroundhogMessage;
import com.yongche.component.groundhog.message.GroundhogMessageException;
import com.yongche.component.groundhog.message.PingMessage;
import com.yongche.component.groundhog.message.PushRequestMessage;
import com.yongche.component.groundhog.message.RpcRequestMessage;
import com.yongche.component.groundhog.message.SubscribeRequestMessage;
import com.yongche.component.groundhog.message.UnsubscribeRequestMessage;

public class WorkerClient extends GroundhogClient {
    public GroundhogMessage getMsg() throws GroundhogMessageException, IOException {
        GroundhogMessage nextMsg = null;
        try {
            nextMsg = parser.getMsg(this.in);
        } catch (IOException ex) {
            throw ex;
        }
        return nextMsg;
    }
    
    public void ping(long timeout) throws ClientException {
        PingMessage pingMsg = new PingMessage();
        long sequenceId = GroundhogClient.getNextSequence();

	    try {
	        pingMsg.sequenceId = sequenceId;
	        pingMsg.userType = this.userType;
	        pingMsg.userId = Long.valueOf(this.userId);
	        pingMsg.expire = System.currentTimeMillis() + timeout;
	        
	        this.parser.sendMsg(pingMsg, this.out);
//	        PushService.putWaitingResponse(sequenceId, pingMsg);
        } catch (Exception e) {
        	System.out.println("");
            this.close();
            e.printStackTrace();
            throw new ClientException(e);
        }
    }
    
    public void sendMsg(String message, short messageType, long expire,
            long seq, boolean enableEncryption, boolean disableCompression) throws ClientException {
        PushRequestMessage pushMsg = new PushRequestMessage();
        
        try {
            pushMsg.messageType = messageType;
            pushMsg.sequenceId = (seq == 0) ? GroundhogClient.getNextSequence() : seq;
            if (enableEncryption) {
                pushMsg.flags |= GroundhogMessage.FLAGS_ENCRYPT;
            }
            if (!disableCompression) {
                pushMsg.flags |= GroundhogMessage.FLAGS_COMPRESS;
            }
            pushMsg.userType = this.userType;
            pushMsg.userId = this.userId;
            pushMsg.deviceId = this.deviceId;
            pushMsg.ttl = (int)((expire - System.currentTimeMillis()) / 1000);
            pushMsg.receiverType = GroundhogMessage.USER_TYPE_VIRTUAL;
            pushMsg.receiverId = 0;
            pushMsg.receiverDid = messageType;
            pushMsg.pushMessage = message;
            pushMsg.expire = expire;
            
            this.parser.sendMsg(pushMsg, this.out);
//            PushService.putWaitingResponse(pushMsg.sequenceId, pushMsg);
        } catch (Exception e) {
            this.close();
            e.printStackTrace();
            throw new ClientException(e);
        }
    }
    
    public void rpcRequest(String serviceType, String serviceUri, String data,
            long expire, long seq, boolean enableEncryption, boolean disableCompression) throws ClientException {
        RpcRequestMessage rpcMsg = new RpcRequestMessage();
        
        try {
            rpcMsg.messageType = GroundhogMessage.MESSAGE_TYPE_SYSTEM;
            rpcMsg.sequenceId = (seq == 0) ? GroundhogClient.getNextSequence() : seq;
            if (enableEncryption) {
                rpcMsg.flags |= GroundhogMessage.FLAGS_ENCRYPT;
            }
            if (!disableCompression) {
                rpcMsg.flags |= GroundhogMessage.FLAGS_COMPRESS;
            }
            rpcMsg.userType = this.userType;
            rpcMsg.userId = this.userId;
            rpcMsg.deviceId = this.deviceId;
            rpcMsg.ttl = (int)((expire - System.currentTimeMillis()) / 1000);
            rpcMsg.receiverType = GroundhogMessage.USER_TYPE_VIRTUAL;
            rpcMsg.receiverId = 0;
            rpcMsg.receiverDid = GroundhogMessage.MESSAGE_TYPE_SYSTEM;
            rpcMsg.serviceType = serviceType;
            rpcMsg.serviceUri = serviceUri;
            rpcMsg.data = data;
            rpcMsg.expire = expire;
            
            this.parser.sendMsg(rpcMsg, this.out);
//            PushService.putWaitingResponse(rpcMsg.sequenceId, rpcMsg);
        } catch (Exception e) {
            this.close();
            e.printStackTrace();
            throw new ClientException(e);
        }
    }
    
    public void subscribe(long driverId, long orderId, short messageType, long retryTimeout) throws ClientException {
        SubscribeRequestMessage subMsg = new SubscribeRequestMessage();

        try {
            subMsg.sequenceId = GroundhogClient.getNextSequence();
            subMsg.flags |= GroundhogMessage.FLAGS_ENCRYPT;

            subMsg.driverId = driverId;
            subMsg.orderId = orderId;
            subMsg.subscribeMessageType = messageType;
            subMsg.expire = System.currentTimeMillis() + retryTimeout * 1000;

            this.parser.sendMsg(subMsg, this.out);
//            PushService.putWaitingResponse(subMsg.sequenceId, subMsg);
//            PushService.subscribeInfo.lastRecvPkgTime = System.currentTimeMillis();
        } catch (Exception e) {
            this.close();
            e.printStackTrace();
            throw new ClientException(e);
        }
    }

    public void unsubscribe(long driverId, long orderId, short messageType, long retryTimeout) throws ClientException {
        UnsubscribeRequestMessage unsubMsg = new UnsubscribeRequestMessage();

        try {
            unsubMsg.sequenceId = GroundhogClient.getNextSequence();
            unsubMsg.flags |= GroundhogMessage.FLAGS_ENCRYPT;

            unsubMsg.driverId = driverId;
            unsubMsg.orderId = orderId;
            unsubMsg.unsubscribeMessageType = messageType;
            unsubMsg.expire = System.currentTimeMillis() + retryTimeout * 1000;

            this.parser.sendMsg(unsubMsg, this.out);
//            PushService.putWaitingResponse(unsubMsg.sequenceId, unsubMsg);
        } catch (Exception e) {
            this.close();
            e.printStackTrace();
            throw new ClientException(e);
        }
    }

    public void sendAckForMessage(GroundhogMessage msg) throws MessageException, IOException {
        AcknowledgeMessage ack = new AcknowledgeMessage();
        ack.sequenceId = msg.sequenceId;
        this.parser.sendMsg(ack, this.out);
    }
}