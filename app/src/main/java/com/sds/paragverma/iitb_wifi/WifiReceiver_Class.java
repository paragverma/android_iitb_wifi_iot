package com.sds.paragverma.iitb_wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by paragverma on 6/25/2017.
 */

public class WifiReceiver_Class extends BroadcastReceiver {

    WifiManager wifiManager;
    @Override
    public void onReceive(Context context, Intent intent) {
        wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);


        if(wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING ||
                wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED);
           // Log.d("WIFI_Y", "CL: Disable hua");


    }
}
