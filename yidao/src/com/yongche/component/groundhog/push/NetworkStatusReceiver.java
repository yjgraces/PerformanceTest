package com.yongche.component.groundhog.push;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;

public class NetworkStatusReceiver extends BroadcastReceiver {
    private static final String ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null 
                && action.equals(ACTION)) {
            if (NetworkTool.isNetworkUsable(context)) {
                Logger.info(this.getClass().getName(), "Network becomes usable, Thread : " + Thread.currentThread().getId());
                PushService.actionPing(context);
                //debugIntent(intent);
            }
        }
    }
    
    /*
    private void debugIntent(Intent intent) {
        Logger.verbose(this.getClass().getName(), "action: " + intent.getAction());
        Logger.verbose(this.getClass().getName(), "component: " + intent.getComponent());
        
        android.os.Bundle extras = intent.getExtras();
        if (extras != null) {
           for (String key: extras.keySet()) {
               Logger.verbose(this.getClass().getName(), 
                       "key [" + key + "]: " + extras.get(key));
           }
        } else {
           Logger.verbose(this.getClass().getName(), "no extras");
        }
     }
     */
}