package com.yongche.component.groundhog.push;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;

public class PingAlarmReceiver extends BroadcastReceiver {	
    public static final String ACTION_PING = "com.yongche.component.groundhog.push.intent.PING";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null 
                && action.equals(ACTION_PING)
                && NetworkTool.isNetworkUsable(context)) {
            Logger.info(this.getClass().getName(), "fixed timing ping, Thread : " + Thread.currentThread().getId());
            PushService.actionPing(context);
        }
	}
}
