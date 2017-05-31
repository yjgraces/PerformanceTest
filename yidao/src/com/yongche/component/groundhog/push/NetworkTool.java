package com.yongche.component.groundhog.push;

import android.content.Context;
import android.content.net.ConnectivityManager;
import android.content.net.NetworkInfo;

public class NetworkTool {
    public static boolean isNetworkUsable(Context context) {
        NetworkInfo info = getActiveNetworkInfo(context);
        if (null != info && info.isConnected()) {
            return true;
        }
        return false;
    }
    
    public static String getCurrentNetType(Context context) {
        NetworkInfo info = getActiveNetworkInfo(context);
        if (info == null) {
            return "";
        } else if (info.getType() != ConnectivityManager.TYPE_MOBILE) {
            return info.getTypeName();
        } else {
            //return info.getSubtypeName();
            return info.getTypeName();
        }
    }
    
    private static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager cmManager = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cmManager.getActiveNetworkInfo();
    }
}
