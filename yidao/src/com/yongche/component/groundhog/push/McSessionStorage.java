package com.yongche.component.groundhog.push;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

final public class McSessionStorage {

    private final static String SESSION_PREFS_NAME = "MC_PERSISTENT_SESSION";
    private SharedPreferences prefs;
    private Service bindService;
    
    public McSessionStorage(Service bindService) {
        this.bindService = bindService;
    }

    public synchronized boolean updatezSessionInfo(McSessionInfo sessionInfo) {
        prefs = this.bindService.getSharedPreferences(
                McSessionStorage.SESSION_PREFS_NAME,
                Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        if (sessionInfo != null) {
            editor.putString(McSessionInfo.MC_MANAGER_HOSTS, 
                    sessionInfo.managerHosts);

            editor.putLong(McSessionInfo.MC_WORKER_EXPIRE_TIME, 
                    sessionInfo.workerExpireTime);
            editor.putString(McSessionInfo.MC_WORKER_HOST, 
                    sessionInfo.workerHost);
            editor.putInt(McSessionInfo.MC_WORKER_PORT, 
                    sessionInfo.workerPort);
            
            editor.putString(McSessionInfo.MC_USER_TYPE, 
                    sessionInfo.userType);
            editor.putLong(McSessionInfo.MC_USER_ID, 
                    sessionInfo.userId);
            editor.putLong(McSessionInfo.MC_DEVICE_ID, 
                    sessionInfo.deviceId);
            editor.putString(McSessionInfo.MC_SESSION_ID, 
                    sessionInfo.sessionId);
        } else {
            editor.clear();
        }
        
        return editor.commit();
    }
    
    public synchronized McSessionInfo getSessionInfo() {
        McSessionInfo sessionInfo = new McSessionInfo();

        sessionInfo.managerHosts = prefs.getString(McSessionInfo.MC_MANAGER_HOSTS, "");

        sessionInfo.workerExpireTime = prefs.getLong(McSessionInfo.MC_WORKER_EXPIRE_TIME, 0);
        sessionInfo.workerHost = prefs.getString(McSessionInfo.MC_WORKER_HOST, "");
        sessionInfo.workerPort = prefs.getInt(McSessionInfo.MC_WORKER_PORT, 0);
        
        sessionInfo.userType = prefs.getString(McSessionInfo.MC_USER_TYPE, "");
        sessionInfo.userId = prefs.getLong(McSessionInfo.MC_USER_ID, 0);
        sessionInfo.deviceId = prefs.getLong(McSessionInfo.MC_DEVICE_ID, 0);
        sessionInfo.sessionId = prefs.getString(McSessionInfo.MC_SESSION_ID, "");
        
        return sessionInfo;
    }
}
