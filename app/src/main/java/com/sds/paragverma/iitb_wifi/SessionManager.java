package com.sds.paragverma.iitb_wifi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;



public class SessionManager {

    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Sharedpref file name
    private static final String PREF_WIFI_SCAN_FREQUENCY = "WifiScanFrequency";

    // All Shared Preferences Keys
    private static final String PREF_URL = "Url";

    // User name (make variable public to access from outside)
    private static final String PREF_PORT = "Port";

    private static final String PREF_PACKET_LENGTH = "PacketLength";

    private static final String PREF_UDP_SEND_TIMES= "Udprequesttimes";

    private static final String PREF_DEVICE_PORT= "Bindevice";

    private static final String PREF_SOCKET_TIMEOUT= "Timeout";


    int PRIVATE_MODE = 0;

    // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_WIFI_SCAN_FREQUENCY, PRIVATE_MODE);
        editor = pref.edit();
    }


    public void setFreq(int freq){
        editor.putInt(PREF_WIFI_SCAN_FREQUENCY, freq);
        editor.commit();
    }

    public int getFreq(){
        return pref.getInt(PREF_WIFI_SCAN_FREQUENCY, 20);
    }

    public void setUrl(String url){
        editor.putString(PREF_URL, url);
        editor.commit();
    }

    public String getUrl(){
        return pref.getString(PREF_URL, "192.168.4.1");
    }

    public void setPort(int port){
        editor.putInt(PREF_PORT, port);
        editor.commit();
    }

    public int getPort(){
        return pref.getInt(PREF_PORT, 5338);
    }

    public void setPacketLength(int pl){
        editor.putInt(PREF_PACKET_LENGTH, pl);
        editor.commit();
    }

    public int getPacketLength(){
        return pref.getInt(PREF_PACKET_LENGTH, 60);
    }

    public void setUdpSendTimes(int pl){
        editor.putInt(PREF_UDP_SEND_TIMES, pl);
        editor.commit();
    }

    public int getUdpGetTimes(){
        return pref.getInt(PREF_UDP_SEND_TIMES, 20);
    }

    public void setDevicePort(int pl){
        editor.putInt(PREF_DEVICE_PORT, pl);
        editor.commit();
    }

    public int getDevicePort(){
        return pref.getInt(PREF_DEVICE_PORT, 8888);
    }

    public void setSocketTimeOut(int pl){
        editor.putInt(PREF_SOCKET_TIMEOUT, pl);
        editor.commit();
    }

    public int getSocketTimeOut(){
        return pref.getInt(PREF_SOCKET_TIMEOUT, 2);
    }

}
