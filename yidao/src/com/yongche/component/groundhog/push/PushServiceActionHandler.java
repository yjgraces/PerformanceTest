package com.yongche.component.groundhog.push;

import java.io.IOException;
import java.util.Iterator;

import org.json.JSONException;

import android.content.Intent;
import android.os.Bundle;

import com.yongche.component.groundhog.MessageException;
import com.yongche.component.groundhog.client.ClientException;
import com.yongche.component.groundhog.message.GroundhogMessage;

public class PushServiceActionHandler extends ActionHandler {
    
    private McPersistentConnection persistentConnection;
    private long lastPingTime;
    
    public PushServiceActionHandler(McPersistentConnection persistentConnection) {
        super();
        this.persistentConnection = persistentConnection;
    }
    
    public void run() {
        Iterator<Intent> it;

        while(!requestStopFlag) {
            Intent intent;

            try {
                long waitTime = PushService.checkTimeoutMsgList();
                long now = System.currentTimeMillis();
                if (PushService.requestPingFlag) {
                    this.requestPing();
                }

                if (PushService.subscribeEnabled) {
                    if (PushService.retrySubscribeFlag ||
                            (PushService.subscribeInfo.lastRecvPkgTime +
                            PushService.subscribeInfo.retryTimeout * 1000 < now))
                    {
                        PushService.retrySubscribeFlag = false;
                        this.retrySubscribe();
                    }
                }
                
                if (this.persistentConnection.isConnected()) {
                    intent = actionQueue.poll();
                    if (intent != null) {
                        this.handleIntent(intent);
                    } else {
                        synchronized (this) {
                            this.wait(waitTime);
                        }
                    }
                } else {
                    it = actionQueue.iterator();
                    while (it.hasNext()) {
                        Intent head = it.next();
                        Bundle bdl = head.getExtras();
                        if (bdl.getLong("expire") <= System.currentTimeMillis()) {
                            it.remove();
                            if (head.getAction().equals(PushService.ACTION_RPC_REQUEST)) {
                                PushService.sendRpcResponse(bdl.getLong("seq"),
                                        GroundhogMessage.RPC_RET_CODE_TIMEOUT);
                            }
                        } else {
                            break;
                        }
                    }
                    synchronized (this) {
                        this.wait(waitTime);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void addAction(Intent i) {
        if (i.getAction().equals(PushService.ACTION_INIT)) {
            this.lastPingTime = System.currentTimeMillis();
            this.persistentConnection.requestReconnect(i);
        } else {
            if (!this.actionQueue.offer(i)) {
                if (i.getAction().equals(PushService.ACTION_RPC_REQUEST)) {
                    PushService.sendRpcResponse(i.getExtras().getLong("seq"),
                            GroundhogMessage.RPC_RET_CODE_DEQUE_FULL);
                }
            }
            synchronized(this) {
                this.notify();
            }
        }
    }
    
    public synchronized void notifyMe() {
        this.notify();
    }
    
    protected void handleIntent(Intent i) {
        String action = i.getAction();
        Bundle bdl = i.getExtras();
        
        if (action.equals(PushService.ACTION_SEND_MESSAGE)) {
            if ((bdl.getLong("expire") - System.currentTimeMillis()) / 1000 > 1) {
                try {
                    this.persistentConnection.sendMsg(
                            bdl.getString("message"),
                            bdl.getShort("msg_type"),
                            bdl.getLong("expire"),
                            bdl.getLong("seq"),
                            bdl.getBoolean("enable_encryption"),
                            bdl.getBoolean("disable_compression"));
                } catch (Exception e) {
                    this.persistentConnection.requestReconnect(i);
                }
            }
        } else if (action.equals(PushService.ACTION_RPC_REQUEST)) {
            if ((bdl.getLong("expire") - System.currentTimeMillis()) / 1000 > 1) {
                try {
                    this.persistentConnection.rpcRequest(
                            bdl.getString("service_type"),
                            bdl.getString("service_uri"),
                            bdl.getString("data"),
                            bdl.getLong("expire"),
                            bdl.getLong("seq"),
                            bdl.getBoolean("enable_encryption"),
                            bdl.getBoolean("disable_compression"));
                } catch (Exception e) {
                    this.persistentConnection.requestReconnect(i);
                }
            } else {
                PushService.sendRpcResponse(bdl.getLong("seq"),
                        GroundhogMessage.RPC_RET_CODE_TIMEOUT);
            }
        } else if (action.equals(PushService.ACTION_SUBSCRIBE)) {
            try {
                this.persistentConnection.subscribe(
                        bdl.getLong("driver_id"),
                        bdl.getLong("order_id"),
                        bdl.getShort("msg_type"),
                        bdl.getLong("retry_timeout"));
            } catch (Exception e) {
                this.persistentConnection.requestReconnect(i);
            }
        } else if (action.equals(PushService.ACTION_UNSUBSCRIBE)) {
            try {
                this.persistentConnection.unsubscribe(
                        bdl.getLong("driver_id"),
                        bdl.getLong("order_id"),
                        bdl.getShort("msg_type"));
            } catch (Exception e) {
                this.persistentConnection.requestReconnect(i);
            }
        }
    }
    
    private void requestPing() {
        Intent intent;

        long now = System.currentTimeMillis();
        PushService.requestPingFlag = false;
        
        if (!this.persistentConnection.isConnected()) {
            Logger.info(this.getClass().getName(), "trigger reconnect");
            intent = new Intent();
            intent.setAction(PushService.ACTION_PING);
            this.lastPingTime = now;
            this.persistentConnection.requestReconnect(intent);
            return;
        }
    
        if ((this.lastPingTime + CommonConfig.PING_FREQUENCY / 2 >= now ||
                this.persistentConnection.lastRecvPkgTime + CommonConfig.PING_FREQUENCY / 2 >= now)) {
            return;
        }
        
        this.lastPingTime = now;
        try {
            Logger.info(this.getClass().getName(), "start ping");
            this.persistentConnection.ping();
            Logger.info(this.getClass().getName(), "end ping");
        } catch (Exception e) {
            Logger.info(this.getClass().getName(), "restart the connection after ping");
            intent = new Intent();
            intent.setAction(PushService.ACTION_PING);
            this.persistentConnection.requestReconnect(intent);
        }
    }

    private void retrySubscribe()
            throws ClientException, PushConnectionException, MessageException, IOException, InterruptedException, JSONException {
        Logger.info(this.getClass().getName(), "retrySubscribe");
        this.persistentConnection.subscribe(PushService.subscribeInfo.driverId,
                PushService.subscribeInfo.orderId, PushService.subscribeInfo.messageType,
                PushService.subscribeInfo.retryTimeout);
    }
}
