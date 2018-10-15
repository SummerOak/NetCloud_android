package com.summer.netcloud;

import com.summer.netcore.VpnConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by summer on 12/06/2018.
 */

public class Constants {

    public static final String TAG = "NetCloud";




    public static final byte DEBUG_LEV_RELEASE  = 0;
    public static final byte DEBUG_LEV_DEBUG    = 1;


    public static final byte DEBUG_LEV = DEBUG_LEV_DEBUG;

    public static VpnConfig.AVAIL_CTRLS DEFAULT_SYSTEM_CTRL = VpnConfig.AVAIL_CTRLS.PROXY;
    public static VpnConfig.AVAIL_CTRLS DEFAULT_UNKNOWN_CTRL = VpnConfig.AVAIL_CTRLS.PROXY;


    public static Map<String,VpnConfig.AVAIL_CTRLS> DEFAULT_APP_CTRLS = new HashMap<>();
    static {
        DEFAULT_APP_CTRLS.put("com.android.browser", VpnConfig.AVAIL_CTRLS.PROXY);
        DEFAULT_APP_CTRLS.put("com.google.android.gms", VpnConfig.AVAIL_CTRLS.PROXY);
        DEFAULT_APP_CTRLS.put("com.google.android.youtube", VpnConfig.AVAIL_CTRLS.PROXY);
        DEFAULT_APP_CTRLS.put("com.google.android.gsf", VpnConfig.AVAIL_CTRLS.PROXY);
        DEFAULT_APP_CTRLS.put("bbc.mobile.news.uk", VpnConfig.AVAIL_CTRLS.PROXY);
        DEFAULT_APP_CTRLS.put("com.android.providers.downloads", VpnConfig.AVAIL_CTRLS.PROXY);
        DEFAULT_APP_CTRLS.put("com.android.vending", VpnConfig.AVAIL_CTRLS.PROXY);
        DEFAULT_APP_CTRLS.put("com.google.android.gm", VpnConfig.AVAIL_CTRLS.PROXY);
        DEFAULT_APP_CTRLS.put("com.android.chrome", VpnConfig.AVAIL_CTRLS.PROXY);
        DEFAULT_APP_CTRLS.put("com.twitter.android", VpnConfig.AVAIL_CTRLS.PROXY);
        DEFAULT_APP_CTRLS.put("com.google.android.syncadapters.calendar", VpnConfig.AVAIL_CTRLS.PROXY);
        DEFAULT_APP_CTRLS.put("com.google.android.syncadapters.contacts", VpnConfig.AVAIL_CTRLS.PROXY);
        DEFAULT_APP_CTRLS.put("com.google.android.partnersetup", VpnConfig.AVAIL_CTRLS.PROXY);
        DEFAULT_APP_CTRLS.put("com.facebook.katana", VpnConfig.AVAIL_CTRLS.PROXY);
    }

    public static Map<String,VpnConfig.AVAIL_CTRLS> DEFAULT_IP_CTRLS = new HashMap<>();

    static {
        DEFAULT_IP_CTRLS.put("8.8.8.8", VpnConfig.AVAIL_CTRLS.PROXY);
    }

    public static boolean USE_DEFAULT_PROXY = true;
    public static int DEFAULT_PROXY_IPVER = 4;
    public static String DEFAULT_PROXY_ADDR = "47.90.206.185";
    public static String DEFAULT_PROXY_PORT = "9111";

    public static boolean USE_DEFAULT_DNS = true;
    public static String DEFAULT_DNS = "8.8.8.8";

}
