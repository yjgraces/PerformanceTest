package com.yongche.component.groundhog.push;

import com.yongche.component.groundhog.push.Logger.Level;

public class CommonConfig {
    public final static short VERSION = 14;
    public final static String OS_NAME = "android";
    
    public final static boolean COMPRESSION_ENABLE = true;
    public final static int BODY_SIZE_THRESHOLD = 1024;
    
    public final static boolean ENCRYPTION_ENABLE = true;
    public final static String KEY_GENERATE_ALGORITHM = "DESede";
    public final static String KEY_FACTORY_ALGORITHM = "RSA";
    public final static String KEY_ENCRYPT_ALGORITHM = "RSA/ECB/NOPadding";
    public final static String DES_ENCRYPT_ALGORITHM = "DESede/CBC/PKCS5Padding";
    
    public final static Level LOG_LEVEL = Level.ALL;//鍏ㄥ眬鏃ュ織绾у埆閰嶇疆
    
    public final static int CONN_TIMEOUT = 10000;//socket杩炴帴瓒呮椂鏃堕暱
    public final static int RESPONSE_TIMEOUT = 30000;//鍚屾璇锋眰瓒呮椂鏃堕暱
    
    public static String[] MANAGER_HOST = {"m.mc.yongche.com", "s.mc.yongche.com"};//manager host琛�    public static String[] MANAGER_HOST = {"10.0.11.243", "10.0.11.244"};
    //public static String[] MANAGER_HOST = {"10.0.11.71", "10.0.11.72"};
    public final static int MANAGER_PORT = 5108;//manager port
    
    public static final int PING_FREQUENCY = 60 * 1000;//瀹氭椂ping闂撮殧
    
    public static final long PING_TIMEOUT = 60 * 1000;//ping绛夊緟鏈�ぇ闂撮殧
    public static final long LOOP_MAX_WAIT_TIME = 60 * 1000;

    public static final int MAX_RETRY_MANAGER_TIMES = 3;//杩炴帴閲嶈瘯娆℃暟
    public static final long MANAGER_RETRY_INTERVAL = 5 * 1000;//閲嶈瘯闂撮殧

    public static final int MAX_RETRY_WORKER_TIMES = 3;//杩炴帴閲嶈瘯娆℃暟
    public static final long WORKER_RETRY_INTERVAL = 3 * 1000;//閲嶈瘯闂撮殧
    
    public static final int DEFAULT_MESSAGE_TTL = 30;
    
    public static final int MAX_SEND_QUEUE_SIZE = 100;
    
    public static final long TIME_CORRECTION_THRESHOLD = 300000; // 5 minutes

    public static final long SUBSCRIBE_TIMEOUT = 60 * 1000;
}
