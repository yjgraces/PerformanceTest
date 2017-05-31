package com.yongche.component.groundhog.push;

final public class McSessionInfo {
    public static String MC_MANAGER_HOSTS = "mc_manager_hosts";

    public static String MC_WORKER_EXPIRE_TIME = "mc_worker_expire_time";
    public static String MC_WORKER_HOST = "mc_worker_host";
    public static String MC_WORKER_PORT = "mc_worker_port";
    
    public static String MC_USER_TYPE = "mc_user_type";
    public static String MC_USER_ID = "mc_user_id";
    public static String MC_DEVICE_ID = "mc_device_id";
    public static String MC_SESSION_ID = "mc_session_id";

    public String managerHosts;

    public long workerExpireTime;
    public String workerHost;
    public int workerPort;
    
    public String userType;
    public long userId;
    public long deviceId;
    public String sessionId;
}
