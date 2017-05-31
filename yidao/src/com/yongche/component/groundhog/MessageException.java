package com.yongche.component.groundhog;

public class MessageException extends Exception {
    
    private static final long serialVersionUID = -4917812952385356951L;
    
    public MessageException() {
        super();
    }

    public MessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageException(String message) {
        super(message);
    }

    public MessageException(Throwable cause) {
        super(cause);
    }
    
}
