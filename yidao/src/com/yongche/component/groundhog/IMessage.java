package com.yongche.component.groundhog;

import java.nio.charset.Charset;


public interface IMessage {
    
    static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    
    abstract byte[] encode() throws MessageException;
    abstract int decode(byte[] payload, byte status) throws MessageException;
    
}
