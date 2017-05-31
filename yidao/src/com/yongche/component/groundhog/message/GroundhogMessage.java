package com.yongche.component.groundhog.message;

import com.yongche.component.groundhog.IMessage;


abstract public class GroundhogMessage implements IMessage {
    
    static public final int USERTYPE_LENGTH = 2;
    
    static public final int MAX_PACKAGE_SIZE = 16384;
    static public final int COMPRESS_BUFFER_SIZE = 1024;

    static public final String USER_TYPE_PASSENGER = "PA";
    static public final String USER_TYPE_DRIVER = "DR";
    static public final String USER_TYPE_VIRTUAL = "VI";
    static public final String USER_TYPE_GREENCAR_DRIVER = "GD";
    static public final String USER_TYPE_GREENCAR_PASSENGER = "GP";
    
    static public final short MESSAGE_TYPE_SYSTEM = 1000;
    static public final short MESSAGE_TYPE_DISPATCH = 10001;
    static public final short MESSAGE_TYPE_LATLNG = 20001;
    static public final short MESSAGE_TYPE_CHAT = 20003;
    static public final short MESSAGE_TYPE_DRIVER_COMMUNICATION = 21001;
    
    static public final byte FUNCTION_ID_ACK = 64;
    static public final byte FUNCTION_ID_LOGIN = 65;
    static public final byte FUNCTION_ID_LOGIN_RESPONSE = 66;
    static public final byte FUNCTION_ID_REQUEST_WORKER = 67;
    static public final byte FUNCTION_ID_ASSGIN_WORKER = 68;
    static public final byte FUNCTION_ID_MESSAGE = 69;
    static public final byte FUNCTION_ID_PING = 71;
    static public final byte FUNCTION_ID_SUBSCRIBE = 86;
    static public final byte FUNCTION_ID_UNSUBSCRIBE = 87;
    static public final byte FUNCTION_ID_RPC = 91;
    static public final byte FUNCTION_ID_RPC_RESPONSE = 92;

    static public final byte FUNCTION_ID_GET_PUBLIC_KEY = 101;
    static public final byte FUNCTION_ID_GET_PUBLIC_KEY_RESPONSE = 102;
    static public final byte FUNCTION_ID_SET_SECRET_KEY = 103;
    
    static public final short FLAGS_NONE = 0;
    static public final short FLAGS_ONLINE = 1;
    static public final short FLAGS_ENCRYPT = 2;
    static public final short FLAGS_COMPRESS = 4;

    static public final byte STATUS_SUCCESS = 0;
    static public final byte STATUS_EPERM = 1;
    
    static public final int RPC_RET_CODE_SERVER_ERROR = 500;
    static public final int RPC_RET_CODE_TIMEOUT = 408;
    static public final int RPC_RET_CODE_DEQUE_FULL = 409;
    
    public short messageType = MESSAGE_TYPE_SYSTEM;
    public byte functionId;
    public byte status = STATUS_SUCCESS;
    public long sequenceId;
    public short flags = FLAGS_NONE;
    
    public long userId;
    public String userType;
    
    public long expire;  //expired time

    public GroundhogMessage() {
    }
    
    @Override
    public byte[] encode() throws GroundhogMessageException {
        return null;
    }

    @Override
    public int decode(byte[] payload, byte status) throws GroundhogMessageException {
        return 0;
    }
    
}
