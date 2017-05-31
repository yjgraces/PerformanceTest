package com.yongche.component.groundhog.push;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;

import com.yongche.component.groundhog.ILoginStatusListener;
import com.yongche.component.groundhog.message.GroundhogMessage;
import com.yongche.component.groundhog.message.PingMessage;
import com.yongche.component.groundhog.message.RpcRequestMessage;
import com.yongche.component.groundhog.message.SubscribeRequestMessage;
import com.yongche.component.groundhog.message.UnsubscribeRequestMessage;

public class PushService extends Service {

    public static final String ACTION_RPC_RESPONSE = "com.yongche.component.groundhog.RPC_RESPONSE";
    public static final String ACTION_RECEIVED_MESSAGE = "com.yongche.component.groundhog.RECEIVED_MESSAGE";
    public static final String ACTION_CONNECTION_STATUS = "com.yongche.component.groundhog.CONNECTION_STATUS";
    public static final String ACTION_TIME_CORRECTION = "com.yongche.component.groundhog.TIME_CORRECTION";
    
    public static final String ACTION_INIT = "com.yongche.component.groundhog.push.INIT";
    public static final String ACTION_PING = "com.yongche.component.groundhog.push.PING";
    public static final String ACTION_RPC_REQUEST = "com.yongche.component.groundhog.push.RPC_REQUEST";
    public static final String ACTION_SEND_MESSAGE = "com.yongche.component.groundhog.push.SEND_MESSAGE";
    public static final String ACTION_SUBSCRIBE = "com.yongche.component.groundhog.push.SUBSCRIBE";
    public static final String ACTION_UNSUBSCRIBE = "com.yongche.component.groundhog.push.UNSUBSCRIBE";

    public static volatile boolean requestPingFlag = false;
    public static volatile boolean retrySubscribeFlag = false;

    public static boolean subscribeEnabled = false;
    public static SubscribeInfo subscribeInfo;

    private BroadcastReceiver networkStatusReceiver;
    private BroadcastReceiver pingAlarmReceiver;
    private BroadcastReceiver connectionStatusReceiver;
    
    private PendingIntent pingPendingIntent;
    private int pingFrequency = CommonConfig.PING_FREQUENCY;
    
    private McSessionStorage sessoinStorage;
    private McPersistentConnection persistentConnection;
    private PushServiceActionHandler pushActionHandler;
    
    private static McPersistentConnection currentConnection;
    private static Lock waitingResponseQueueLock;
    private static ConcurrentHashMap<Long, GroundhogMessage> waitingResponseQueue;
    private static LinkedList<Long> msgTimeoutList;
    private static ILoginStatusListener loginStatusListener;
    
    @Override
    public void onCreate() {
        sessoinStorage = new McSessionStorage(this);
        waitingResponseQueue = new ConcurrentHashMap<Long, GroundhogMessage>();
        waitingResponseQueueLock = new ReentrantLock();
        msgTimeoutList = new LinkedList<Long>();
        persistentConnection = new McPersistentConnection(this, sessoinStorage);
        persistentConnection.start();
        pushActionHandler = new PushServiceActionHandler(persistentConnection);
        pushActionHandler.start();
        PushService.currentConnection = persistentConnection;
        subscribeInfo = new SubscribeInfo();
        
        registerConnectionReceiver();
        registerConnectivityReceiver();
        registerPingAlarmReceiver();
        registerPingAlarm();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterPingAlarm();
        unregisterPingAlarmReceiver();
        unregisterConnectivityReceiver();
        unregisterConnectionReceiver();
        
        PushService.currentConnection = null;
        pushActionHandler.requestStop();
        persistentConnection.requestStop();
        PushService.clearWaitingResponse();
        msgTimeoutList.clear();
        retrySubscribeFlag = false;
        subscribeEnabled = false;
    }
    
    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void onStart(Intent intent, int startId) {
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        //START_STICKY, recreate service and intent is null
        if (intent != null) {
        	String action = intent.getAction();
            Logger.info(this.getClass().getName(), "action : " + action);
            if (action == null || action.equals(ACTION_PING)) {
            	intent = null;
            }
        }
        else {
            Logger.info(this.getClass().getName(), "intent  is null!");
        }

        if (intent == null) {
            PushService.requestPingFlag = true;
            this.pushActionHandler.notifyMe();
        }
        else {
            this.pushActionHandler.addAction(intent);
        }

        return 0;//super.onStartCommand(intent, flags, startId);
    }
    
    public void notifyPushServiceActionHandler() {
        this.pushActionHandler.notifyMe();
    }
    
    public static boolean isConnected() {
        return PushService.currentConnection != null &&
                PushService.currentConnection.isConnected();
    }
    
    public static void actionInit(Context context, String userType,
            long userId, long deviceId, String password) {
        PushService.actionInit(context, userType, userId, deviceId, password, null, null);
    }

    public static void actionInit(Context context, String userType,
            long userId, long deviceId, String password, ILoginStatusListener listener) {
        PushService.actionInit(context, userType, userId, deviceId, password, listener, null);
    }

    public static void actionInit(Context context, String userType,
            long userId, long deviceId, String password,
            ILoginStatusListener listener, String[] hosts) {
        ArrayList<String> managerHosts = new ArrayList<String>();

        if (hosts != null) {
            for(String host : hosts) {
                if (!managerHosts.contains(host)) {
                    managerHosts.add(host);
                }
            }
        }

        for (String host : CommonConfig.MANAGER_HOST) {
            managerHosts.add(host + ":" + CommonConfig.MANAGER_PORT);
        }

        PushService.loginStatusListener = listener;

        try {
            Intent i = new Intent(context, PushService.class);
            i.setAction(ACTION_INIT);
            Bundle bdl = new Bundle();
            bdl.putStringArrayList("mc_manager_hosts", managerHosts);
            bdl.putString("mc_user_type", userType);
            bdl.putLong("mc_user_id", userId);
            bdl.putLong("mc_device_id", deviceId);
            bdl.putString("mc_session_id", password);
            i.putExtras(bdl);
            context.startService(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void actionStop(Context context) {
        try {
            Intent i = new Intent(context, PushService.class);
            context.stopService(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void actionPing(Context context) {
        try {
            Intent i = new Intent(context, PushService.class);
            i.setAction(ACTION_PING);
            context.startService(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
       
    public static void sendMessage(Context context, String message,
            short messageType, int ttl, long seq) {
        PushService.sendMessage(context, message, messageType, ttl, seq, false, false);
    }
    
    public static void sendMessage(Context context, String message,
            short messageType, int ttl, long seq, boolean enableEncryption) {
        PushService.sendMessage(context, message, messageType, ttl, seq, enableEncryption, false);
    }
    
    public static void sendMessage(Context context, String message,
            short messageType, int ttl, long seq, boolean enableEncryption, boolean disableCompression) {
        try {
            Intent i = new Intent(context, PushService.class);
            i.setAction(ACTION_SEND_MESSAGE);
            Bundle bdl = new Bundle();
            bdl.putString("message", message);
            bdl.putShort("msg_type", messageType);
            bdl.putLong("seq", seq);
            bdl.putBoolean("enable_encryption", enableEncryption);
            bdl.putBoolean("disable_compression", disableCompression);
            bdl.putLong("expire", System.currentTimeMillis() + ttl * 1000);
            i.putExtras(bdl);
            context.startService(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void rpcRequest(Context context, String serviceType, String serviceUri,
            String data, int ttl, long seq) {
        PushService.rpcRequest(context, serviceType, serviceUri, data, ttl, seq, false, false);
    }
    
    public static void rpcRequest(Context context, String serviceType, String serviceUri,
            String data, int ttl, long seq, boolean enableEncryption) {
        PushService.rpcRequest(context, serviceType, serviceUri, data, ttl, seq, enableEncryption, false);
    }
    
    public static void rpcRequest(Context context, String serviceType, String serviceUri,
            String data, int ttl, long seq, boolean enableEncryption, boolean disableCompression) {
        try {
            Intent i = new Intent(context, PushService.class);
            i.setAction(ACTION_RPC_REQUEST);
            Bundle bdl = new Bundle();
            bdl.putString("service_type", serviceType);
            bdl.putString("service_uri", serviceUri);
            bdl.putString("data", data);
            bdl.putLong("seq", seq);
            bdl.putBoolean("enable_encryption", enableEncryption);
            bdl.putBoolean("disable_compression", disableCompression);
            bdl.putLong("expire", System.currentTimeMillis() + ttl * 1000);
            i.putExtras(bdl);
            context.startService(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void onLoginFailed() {
        if (PushService.loginStatusListener != null) {
            PushService.loginStatusListener.onLoginFailed();
        }
    }

    public static void subscribe(Context context,
            long driverId, long orderId) {
        PushService.subscribe(context, driverId, orderId,
                GroundhogMessage.MESSAGE_TYPE_LATLNG, CommonConfig.SUBSCRIBE_TIMEOUT / 1000);
    }

    public static void subscribe(Context context, long driverId,
            long orderId, short messageType, long retryTimeout) {
        try {
            Intent i = new Intent(context, PushService.class);
            i.setAction(ACTION_SUBSCRIBE);
            Bundle bdl = new Bundle();
            bdl.putLong("driver_id", driverId);
            bdl.putLong("order_id", orderId);
            bdl.putShort("msg_type", messageType);
            bdl.putLong("retry_timeout", retryTimeout);
            i.putExtras(bdl);
            context.startService(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unsubscribe(Context context,
            long driverId, long orderId) {
        PushService.unsubscribe(context, driverId, orderId, GroundhogMessage.MESSAGE_TYPE_LATLNG);
    }

    public static void unsubscribe(Context context,
            long driverId, long orderId, short messageType) {
        try {
            Intent i = new Intent(context, PushService.class);
            i.setAction(ACTION_UNSUBSCRIBE);
            Bundle bdl = new Bundle();
            bdl.putLong("driver_id", driverId);
            bdl.putLong("order_id", orderId);
            bdl.putShort("msg_type", messageType);
            i.putExtras(bdl);
            context.startService(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void putWaitingResponse(Long sequenceId, GroundhogMessage msg) {
        waitingResponseQueueLock.lock();
        try {
            boolean done = false;
            waitingResponseQueue.put(sequenceId, msg);
            ListIterator<Long> it = msgTimeoutList.listIterator(msgTimeoutList.size());
            while(it.hasPrevious()) {
                long seq = it.previous();
                GroundhogMessage waitingMsg = waitingResponseQueue.get(seq);
                if (waitingMsg != null && msg.expire >= waitingMsg.expire) {
                    it.add(sequenceId);
                    done = true;
                    break;
                }
            }
            if (!done) {
                msgTimeoutList.addFirst(sequenceId);
            }
        } finally {
            waitingResponseQueueLock.unlock();
        }
    }
    
    public static GroundhogMessage getWaitingResponse(Long sequenceId) {
        return waitingResponseQueue.get(sequenceId);
    }

    public static void removeWaitingResponse(Long sequenceId) {
        waitingResponseQueueLock.lock();
        try {
            waitingResponseQueue.remove(sequenceId);
        } finally {
            waitingResponseQueueLock.unlock();
        }
    }

    public static void clearWaitingResponse() {
        waitingResponseQueueLock.lock();

        try {
            for (Entry<Long, GroundhogMessage> entry : waitingResponseQueue.entrySet()) {
                GroundhogMessage msg = entry.getValue();
                if (msg instanceof RpcRequestMessage) {
                    PushService.sendRpcResponse(((RpcRequestMessage)msg).sequenceId,
                            GroundhogMessage.RPC_RET_CODE_TIMEOUT);
                }
            }
            waitingResponseQueue.clear();
        } finally {
            waitingResponseQueueLock.unlock();
        }
    }
    
    public static long checkTimeoutMsgList() {
        if (msgTimeoutList.size() == 0) {
            return CommonConfig.LOOP_MAX_WAIT_TIME;
        }
        
        ListIterator<Long> it = msgTimeoutList.listIterator();
        
        waitingResponseQueueLock.lock();
        try {
            long currentTime = System.currentTimeMillis();
            while(it.hasNext()) {
                long seq = it.next();
                GroundhogMessage msg = waitingResponseQueue.get(seq);
                if (msg == null) {
                    it.remove();
                } else {
                    if (msg.expire > currentTime) {
                        long remainTime = msg.expire - currentTime;
                        return (remainTime < CommonConfig.LOOP_MAX_WAIT_TIME) ? remainTime : CommonConfig.LOOP_MAX_WAIT_TIME;
                    } else {
                        if (msg instanceof PingMessage) {
                        	Logger.error(PushService.class.getName(), "ping timeout, close connection!");
                            currentConnection.close();
                        } else if (msg instanceof RpcRequestMessage) {
                            PushService.sendRpcResponse(((RpcRequestMessage)msg).sequenceId,
                                    GroundhogMessage.RPC_RET_CODE_TIMEOUT);
                        } else if (msg instanceof SubscribeRequestMessage) {
                            retrySubscribeFlag = true;
                        } else if (msg instanceof UnsubscribeRequestMessage) {
                            Logger.error(PushService.class.getName(), "UnsubscribeRequestMessage timeout, close connection!");
                            currentConnection.close();
                        }
                        it.remove();
                        waitingResponseQueue.remove(seq);
                    }
                }
            }
        } finally {
            waitingResponseQueueLock.unlock();
        }
        
        return CommonConfig.LOOP_MAX_WAIT_TIME;
    }
    
    public static void sendRpcResponse(long seq, int ret_code) {
        String ret_msg = "";

        switch (ret_code) {
        case GroundhogMessage.RPC_RET_CODE_TIMEOUT:
            ret_msg = "RPC call timeout";
            break;
        case GroundhogMessage.RPC_RET_CODE_DEQUE_FULL:
            ret_msg = "System deque full";
            break;
        default:
            break;
        }

        currentConnection.broadcastRpcResponse(seq,
                "{\"ret_code\":\"" + Integer.toString(ret_code) +
                "\",\"ret_msg\":\"" + ret_msg + "\"}");
    }

    private void registerPingAlarmReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PingAlarmReceiver.ACTION_PING);
        pingAlarmReceiver = new PingAlarmReceiver();
        registerReceiver(pingAlarmReceiver, filter);
    }
    
    private void unregisterPingAlarmReceiver() {
        unregisterReceiver(pingAlarmReceiver);
    }
    
    private void registerPingAlarm() {
        Intent intent = new Intent();
        intent.setAction(PingAlarmReceiver.ACTION_PING);
        pingPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        long current = System.currentTimeMillis() + pingFrequency;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, current, pingFrequency, pingPendingIntent);
    }
    
    private void unregisterPingAlarm() {
        if (pingPendingIntent == null) {
            return;
        }
        
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pingPendingIntent);
        pingPendingIntent = null;
    }
    
    private void registerConnectivityReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        networkStatusReceiver = new NetworkStatusReceiver();
        registerReceiver(networkStatusReceiver, filter);
    }
    
    private void unregisterConnectivityReceiver() {
        unregisterReceiver(networkStatusReceiver);
    }
    
    private void registerConnectionReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PushService.ACTION_CONNECTION_STATUS);
        connectionStatusReceiver = new ConnectionStatusReceiver();
        registerReceiver(connectionStatusReceiver, filter);
    }
    
    private void unregisterConnectionReceiver() {
        unregisterReceiver(connectionStatusReceiver);
    }
}
