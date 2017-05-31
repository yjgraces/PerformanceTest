package com.yongche.component.groundhog.message;

public class GetPublicKeyRequestMessage extends RequestMessage {
    public GetPublicKeyRequestMessage() {
        this.functionId = FUNCTION_ID_GET_PUBLIC_KEY;
    }
}