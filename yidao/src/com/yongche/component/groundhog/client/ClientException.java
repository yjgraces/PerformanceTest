package com.yongche.component.groundhog.client;

import com.yongche.component.groundhog.MessageException;


public class ClientException extends MessageException {
    
    private static final long serialVersionUID = 403891462849748004L;

    public ClientException() {
    }

    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientException(String message) {
        super(message);
    }

    public ClientException(Throwable cause) {
        super(cause);
    }

}
