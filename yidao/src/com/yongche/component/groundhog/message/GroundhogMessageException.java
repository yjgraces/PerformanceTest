package com.yongche.component.groundhog.message;

import com.yongche.component.groundhog.MessageException;


public class GroundhogMessageException extends MessageException {
    
    private static final long serialVersionUID = -6454679809009521557L;

    public GroundhogMessageException() {
        super();
    }

    public GroundhogMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public GroundhogMessageException(String message) {
        super(message);
    }

    public GroundhogMessageException(Throwable cause) {
        super(cause);
    }
    
}
