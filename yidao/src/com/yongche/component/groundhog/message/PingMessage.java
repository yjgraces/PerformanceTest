package com.yongche.component.groundhog.message;

public class PingMessage extends RequestMessage {
    public PingMessage() {
        this.functionId = FUNCTION_ID_PING;
    }
}