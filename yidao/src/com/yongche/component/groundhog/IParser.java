package com.yongche.component.groundhog;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;


public interface IParser {
    
    void sendMsg(IMessage msg, DataOutputStream out) throws IOException, MessageException;
    IMessage getMsg(DataInputStream in) throws IOException, MessageException;
    
}
