package com.opentaxi.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import com.opentaxi.android.utils.Network;
import com.opentaxi.rest.RestClient;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 11/8/13
 * Time: 12:16 PM
 * developer STANIMIR MARINOV
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {

       /* if (intent.getExtras() != null) {
            NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                // Log.i("app","Network "+ni.getTypeName()+" connected");
                onConnected();
            } else if (intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                // Log.d("app", "There's no network connectivity");
                onDisconnected();
            }
        } else {*/

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetInfo != null) {
            //Toast.makeText( context, "Active Network Type : " + activeNetInfo.getTypeName(), Toast.LENGTH_SHORT ).show();
            if (activeNetInfo.isConnected()) {
                StringBuilder network = new StringBuilder();
                if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    RestClient.getInstance().setBandwidth(wm.getConnectionInfo().getLinkSpeed());
                    network.append(activeNetInfo.getTypeName()).append(" ").append(wm.getConnectionInfo().getLinkSpeed()).append("Mbps");
                } else if (activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    RestClient.getInstance().setBandwidth(tm.getNetworkType());
                    network.append(activeNetInfo.getTypeName()).append(" ").append(Network.getNetworkTypeName(tm.getNetworkType()));
                } else RestClient.getInstance().setBandwidth(0);

                onConnected(network.toString());

            } else onDisconnected();

        } else onDisconnected();
        /* else {
            NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mobNetInfo != null) {
                //Toast.makeText( context, "Mobile Network Type : " + mobNetInfo.getTypeName(), Toast.LENGTH_SHORT ).show();
                if(mobNetInfo.isConnectedOrConnecting()) onConnected(mobNetInfo.getTypeName());
                else onDisconnected();
            }
        }*/

    }

    private void onConnected(String typeName) {
        RestClient.getInstance().setHaveConnection(true);
        RestClient.getInstance().setConnectionTypeName(typeName);
        /*String gcmRegId = RestClient.getInstance().getGCMRegistrationId(); //DB is the same and hold gcm regId
        if (gcmRegId != null && gcmRegId.length() > 0) RestClient.getInstance().gcmRegister(gcmRegId);*/
    }

    private void onDisconnected() {
        RestClient.getInstance().setHaveConnection(false);
        RestClient.getInstance().setConnectionTypeName("");
    }

    /*if (network) {
        if (bandwidth > 16) {
            // Code for large items
        } else if (bandwidth <= 16 && bandwidth > 8) {
            // Code for medium items
        } else {
            //Code for small items
        }
    } else {
        //Code for disconnected
    }*/

    /*private boolean isDataConnected() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }*/

   /* private int isHighBandwidth(NetworkInfo info) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            return wm.getConnectionInfo().getLinkSpeed();
        } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getNetworkType();
        }
        return 0;
    }*/
}