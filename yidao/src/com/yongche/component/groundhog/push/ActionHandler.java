package com.yongche.component.groundhog.push;

import java.util.concurrent.LinkedBlockingDeque;

import android.content.Intent;


public abstract class ActionHandler extends Thread {

    public volatile boolean requestStopFlag;
    protected LinkedBlockingDeque<Intent> actionQueue;
    
    public ActionHandler() {
        this.actionQueue = new LinkedBlockingDeque<Intent>(CommonConfig.MAX_SEND_QUEUE_SIZE);
        this.setDaemon(true);
    }
    
    public void run() {
        while(!requestStopFlag) {
            Intent intent;
            try {
                intent = actionQueue.take();
                handleIntent(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    abstract protected void handleIntent(Intent intent) throws Exception;
    
    abstract public void addAction(Intent intent);
    
    public void requestStop() {
        this.requestStopFlag = true;
        this.interrupt();
        return;
    }
}
