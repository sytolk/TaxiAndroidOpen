package com.opentaxi.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import com.opentaxi.android.utils.MessageEvent;
import com.opentaxi.rest.RestClient;
import de.greenrobot.event.EventBus;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 11/8/13
 * Time: 12:16 PM
 * developer STANIMIR MARINOV
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    /**
     * if (bandwidth > 16) Code for large items
     * if (bandwidth <= 16 && bandwidth > 8) Code for medium items
     * else Code for small items
     */
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetInfo != null) {
            if (activeNetInfo.isConnected()) {
                StringBuilder network = new StringBuilder();
                if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    network.append(activeNetInfo.getTypeName()).append(" ").append(wm.getConnectionInfo().getLinkSpeed()).append("Mbps");
                } else if (activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    network.append(activeNetInfo.getTypeName()).append(" ").append(getNetworkTypeName(tm.getNetworkType()));
                } //else RestClient.getInstance().setBandwidth(0);

                onConnected(network.toString());

            } else onDisconnected();

        } else onDisconnected();
    }

    private void onConnected(String typeName) {
        RestClient.getInstance().setHaveConnection(true);
        EventBus.getDefault().postSticky(new MessageEvent(typeName));
    }

    private void onDisconnected() {
        RestClient.getInstance().setHaveConnection(false);
        EventBus.getDefault().postSticky(new MessageEvent("disconnected"));
    }

    /**
     * Current network is GPRS
     */
    public static final int NETWORK_TYPE_GPRS = 1;
    /**
     * Current network is EDGE
     */
    public static final int NETWORK_TYPE_EDGE = 2;
    /**
     * Current network is UMTS
     */
    public static final int NETWORK_TYPE_UMTS = 3;
    /**
     * Current network is CDMA: Either IS95A or IS95B
     */
    public static final int NETWORK_TYPE_CDMA = 4;
    /**
     * Current network is EVDO revision 0
     */
    public static final int NETWORK_TYPE_EVDO_0 = 5;
    /**
     * Current network is EVDO revision A
     */
    public static final int NETWORK_TYPE_EVDO_A = 6;
    /**
     * Current network is 1xRTT
     */
    public static final int NETWORK_TYPE_1xRTT = 7;
    /**
     * Current network is HSDPA
     */
    public static final int NETWORK_TYPE_HSDPA = 8;
    /**
     * Current network is HSUPA
     */
    public static final int NETWORK_TYPE_HSUPA = 9;
    /**
     * Current network is HSPA
     */
    public static final int NETWORK_TYPE_HSPA = 10;
    /**
     * Current network is iDen
     */
    public static final int NETWORK_TYPE_IDEN = 11;
    /**
     * Current network is EVDO revision B
     */
    public static final int NETWORK_TYPE_EVDO_B = 12;
    /**
     * Current network is LTE
     */
    public static final int NETWORK_TYPE_LTE = 13;
    /**
     * Current network is eHRPD
     */
    public static final int NETWORK_TYPE_EHRPD = 14;
    /**
     * Current network is HSPA+
     */
    public static final int NETWORK_TYPE_HSPAP = 15;

    public String getNetworkTypeName(int type) {
        switch (type) {
            case NETWORK_TYPE_GPRS:
                return "GPRS";
            case NETWORK_TYPE_EDGE:
                return "EDGE";
            case NETWORK_TYPE_UMTS:
                return "UMTS";
            case NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case NETWORK_TYPE_HSPA:
                return "HSPA";
            case NETWORK_TYPE_CDMA:
                return "CDMA";
            case NETWORK_TYPE_EVDO_0:
                return "CDMA - EvDo rev. 0";
            case NETWORK_TYPE_EVDO_A:
                return "CDMA - EvDo rev. A";
            case NETWORK_TYPE_EVDO_B:
                return "CDMA - EvDo rev. B";
            case NETWORK_TYPE_1xRTT:
                return "CDMA - 1xRTT";
            case NETWORK_TYPE_LTE:
                return "LTE";
            case NETWORK_TYPE_EHRPD:
                return "CDMA - eHRPD";
            case NETWORK_TYPE_IDEN:
                return "iDEN";
            case NETWORK_TYPE_HSPAP:
                return "HSPA+";
            default:
                return "UNKNOWN";
        }
    }
}