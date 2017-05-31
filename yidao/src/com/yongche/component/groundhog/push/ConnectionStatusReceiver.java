package com.yongche.component.groundhog.push;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;

public class ConnectionStatusReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null
                && action.equals(PushService.ACTION_CONNECTION_STATUS)) {
            boolean status = intent.getBooleanExtra("status", false);
            if (NetworkTool.isNetworkUsable(context)
                    && status == false) {
                Logger.info(this.getClass().getName(), "Connection failed, retry, Thread : " + Thread.currentThread().getId());
                PushService.actionPing(context);
            }
        }
    }
}
