package com.sds.paragverma.iitb_wifi;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static android.R.id.message;
import static android.R.id.selectAll;

public class Home extends AppCompatActivity {

    private static final int ACCESS_COARSE_LOCATION_CODE = 6;
    private final int WIFI_STATE_CODE = 1;
    private final int CHANGE_WIFI_STATE_CODE = 2;
    private final int INTERNET_CODE = 3;
    private final int ACCESS_NETWORK_STATE_CODE = 4;
    private final int ACCESS_FINE_LOCATION_CODE = 5;

    private final int TYPE_WPA = 1;
    private final int TYPE_WEP = 2;
    private final int TYPE_OPEN = 3;

    Button enable_wifi;
    private boolean wifion;

    WifiManager wifiManager;
    BroadcastReceiver wifiReceiver;
    BroadcastReceiver wifiStateChange;
    BroadcastReceiver wifiSSIDSChanged;
    View progressCircle;
    CoordinatorLayout coordinatorLayout;
    ListView listView;
    ArrayAdapter adapter;
    TextView wifi_disabled;

    WifiReceiver mywifiReceiver;
    WifiSSIDSChanged mywifiSSIDSChanged;

    boolean mIsWifiReceiverRegistered = false;
    boolean mIsWifiSSIDRegistered = false;

    private boolean activityInFront;

    private ArrayList<String> ssidlist;

    ScanResult selectedWifi;

    private List<ScanResult> catchScanRes;
    private List<ScanResult> wifiScanList;

    SessionManager sessionManager;

    Timer timer;

    NetworkInfo networkInfo;

    boolean authenticatingWifi;

    private Snackbar snackbar;

    DatagramSocket ds;
    DatagramPacket dp;
    private int scantime;

    byte[] ba;
    String msg;

    private boolean connectedWifi;
    private ConnectivityManager connectivityManager;

    private boolean appClosedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

       /* String[] values = new String[] { "Wi-Fi List" };*/

        sessionManager = new SessionManager(getApplicationContext());

        enable_wifi = (Button) findViewById(R.id.enable_wifi);
        listView = (ListView) findViewById(R.id.listview);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        progressCircle = findViewById(R.id.wifi_progress);
        wifi_disabled = (TextView) findViewById(R.id.wifi_disabled);
        timer = null;


        connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);


        if(wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED){
            enable_wifi.setVisibility(View.INVISIBLE);
            wifi_disabled.setVisibility(View.INVISIBLE);
        }


        enable_wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!wifiManager.isWifiEnabled()) {
                    listView.setVisibility(View.INVISIBLE);
                    wifion = true;
                    snackbar = Snackbar.make(coordinatorLayout, "Starting Wi-Fi", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("Action", null).show();

                    wifiManager.setWifiEnabled(true);

                }
            }
        });


        wifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {


                //wifiScanList = wifiManager.getScanResults();

                //Log.d("WIFI_Y", "Scan results method");
                //Log.d("WIFI_Y", wifiScanList.toString());

                /*if(!wifiScanList.isEmpty()) {
                    //String data = wifiScanList.get(0).toString();
                    Log.d("WIFI_Y", wifiScanList.toString());
                }*/

                ssidlist = new ArrayList<String>();
                HashMap<String, Integer> kvp = new HashMap<>();
                HashMap<Integer, ScanResult> scankvp = new HashMap<>();
                ScanResult rcv;
                int i = 0;
                int holdindex = 0;
                String delog = "";
                /*for (ScanResult s:
                        wifiScanList) {

                    //delog += " |" + s.SSID;

                    if(selectedWifi != null){

                        // Yeh BSSID nay dikha
                        if(kvp.get(s.BSSID) == null) {

                            //if(wifiManager.getWifiState() == )
                            if (TextUtils.equals(s.BSSID, wifiManager.getConnectionInfo().getBSSID())) {
                                ssidlist.add("Connected<- " + s.SSID + " Strength: " + WifiManager.calculateSignalLevel(s.level, 100) +
                                        " Type: " + wifiSec(wifitype(s.capabilities)));
                            } else
                                ssidlist.add(s.SSID + " Strength: " + WifiManager.calculateSignalLevel(s.level, 100) +
                                        " Type: " + wifiSec(wifitype(s.capabilities)));
                            kvp.put(s.BSSID, i);
                            scankvp.put(i, s);
                            i++;
                        }

                        //Yeh hashmap mein tha
                        else{
                            //hasmap se uska position nikal

                            holdindex = kvp.get(s.BSSID);

                            //Woh scanresult dusri
                            rcv = scankvp.get(holdindex);

                            if(WifiManager.calculateSignalLevel(s.level, 100) >
                                    WifiManager.calculateSignalLevel(rcv.level, 100)){
                                if (TextUtils.equals(s.BSSID, wifiManager.getConnectionInfo().getBSSID())) {
                                    ssidlist.add("Connected<- " + s.SSID + " Strength: " + WifiManager.calculateSignalLevel(s.level, 100) +
                                            " Type: " + wifiSec(wifitype(s.capabilities)));
                                } else
                                    ssidlist.add(s.SSID + " Strength: " + WifiManager.calculateSignalLevel(s.level, 100) +
                                            " Type: " + wifiSec(wifitype(s.capabilities)));

                                kvp.put(s.BSSID, holdindex);
                                scankvp.put(holdindex, s);
                            }
                        }


                    }
                    else ssidlist.add(s.SSID + " Strength: " + WifiManager.calculateSignalLevel(s.level, 100) +
                    " Type: " + wifiSec(wifitype(s.capabilities)));
                }

                */




                ArrayList<ScanResult> mItems = new ArrayList<>();
                ArrayList<String> StringmItems = new ArrayList<>();
                List<ScanResult> results = wifiManager.getScanResults();
                Log.d("WIFI_Y", results.toString());
                adapter = new ArrayAdapter(Home.this,
                        android.R.layout.simple_list_item_1, StringmItems);

                listView.setAdapter(adapter);

                int size = results.size();

                int alindex = 0;

                HashMap<String, Integer> signalStrength = new HashMap<String, Integer>();
                boolean dbg = false;
                Log.d("WIFI_Y", "size: " + size);
                try {
                   i = 0;
                    while(i < size) {
                        Log.d("WIFI_Y", "Loop: " + i);
                        ScanResult result = results.get(i);

                        if(TextUtils.equals(result.SSID, "hello")) dbg = true;

                        Log.d("WIFI_Y", i + ": " + result.SSID);

                        //If result is not empty
                        if (!result.SSID.isEmpty()) {

                            //Making unique key
                            String key = result.SSID + " "
                                    + result.capabilities;




                            //If not stored previously in strength map
                            if (!signalStrength.containsKey(key)) {


                                Log.d("WIFI_Y", "New");
                                //Put it in strength map, at exact index
                                signalStrength.put(key, alindex++);

                                //Add the result in final result list
                                mItems.add(result);

                                if(TextUtils.equals(wifiManager.getConnectionInfo().getBSSID(), result.BSSID)){
                                    StringmItems.add("Connected<- " + result.SSID + " |Strength: " + WifiManager.calculateSignalLevel(result.level, 100) + " Type: " + wifiSec(wifitype(result.capabilities)));
                                }
                                else StringmItems.add(result.SSID + " |Strength: " + WifiManager.calculateSignalLevel(result.level, 100) + " Type: " + wifiSec(wifitype(result.capabilities)));

                                adapter.notifyDataSetChanged();

                                //Already have some strength
                            } else {

                                Log.d("WIFI_Y", "Cross check");
                                //Get index of already stored strength
                                int position = signalStrength.get(key);
                                Log.d("WIFI_Y", "Cross check -> Got: " + position);
                                //Get scanresult corresponding to that position
                                ScanResult updateItem = mItems.get(position);

                                //Compare and update
                                if (WifiManager.calculateSignalLevel(updateItem.level, 100) <
                                        WifiManager.calculateSignalLevel(result.level, 100)) {
                                    mItems.set(position, result);

                                    if(TextUtils.equals(wifiManager.getConnectionInfo().getBSSID(), result.BSSID)){
                                        StringmItems.set(position, "Connected<- " + result.SSID + " |Strength: " + WifiManager.calculateSignalLevel(result.level, 100) + " Type: " + wifiSec(wifitype(updateItem.capabilities)));
                                    }
                                    else StringmItems.set(position, result.SSID + " |Strength: " + WifiManager.calculateSignalLevel(result.level, 100) + " Type: " + wifiSec(wifitype(updateItem.capabilities)));


                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                        Log.d("WIFI_Y", "Exit: " + i);
                        i++;
                    }
                } catch (Exception e) {
                    Log.d("WIFI_Y", "Exception aa gaya. Exit. Class: " + e.toString());
                    e.printStackTrace();
                }

                Log.d("WIFI_Y", "Filtered");
                Log.d("WIFI_Y", mItems.toString());

                wifiScanList = mItems;



                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();



            }
        };

        //TODO: Remove this
        wifiStateChange = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(wifiManager.isWifiEnabled()) snackbar.dismiss();
            //wifion = false;
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiReceiver, intentFilter);

        IntentFilter if2 = new IntentFilter();
        if2.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        getApplicationContext().registerReceiver(wifiReceiver, if2);

        IntentFilter if3 = new IntentFilter("android.location.PROVIDERS_CHANGED");
        getApplicationContext().registerReceiver(new GpsLocationReceiver(), if3);

        //TODO: Broadcast Receiver shifted to private inner class
        /*IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter2.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter2.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        intentFilter2.addAction(WifiManager.EXTRA_WIFI_STATE);
        //intentFilter2.addAction(WifiManager.);
        getApplicationContext().registerReceiver(wifiStateChange, intentFilter);*/

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Basic App");

        permissionscycle();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Re Scanning for Wi-Fi access points", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    wifiManager.startScan();


                }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //wifiManager.
                View inflatedView = getLayoutInflater().inflate(R.layout.d_text_view, null, false);
                final TextView txtUrl = (TextView) inflatedView.findViewById(R.id.textView);
                selectedWifi =  wifiScanList.get(position);
                txtUrl.setHint("Password");
                Log.d("WIFI_Y", "Selected: " + selectedWifi.SSID + " Connected: " +wifiManager.getConnectionInfo().getSSID());

                if(TextUtils.equals(selectedWifi.BSSID, wifiManager.getConnectionInfo().getBSSID())){
                    Log.d("WIFI_Y", "yehi hai connected");
                    new AlertDialog.Builder(Home.this)
                            .setTitle("Test Module")
                            .setPositiveButton("Test", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    if(snackbar != null)
                                        snackbar.dismiss();
                                    snackbar = Snackbar.make(coordinatorLayout, "Sending UDP Packets to: \'" + sessionManager.getUrl()
                                            + "\' port: \'" + sessionManager.getPort() + "\' Msg: \"" + " \"TEST\" ", Snackbar.LENGTH_INDEFINITE);
                                    snackbar.setAction("Action", null).show();


                                    ds = null;

                                    AsyncTask async_cient_test = new AsyncTask() {

                                        public byte[] rcv;
                                        public InetAddress serverAddr;

                                        @Override
                                        protected Object doInBackground(Object[] params) {

                                            ba = ("TEST").getBytes(StandardCharsets.UTF_8);
                                            msg = new String(ba, StandardCharsets.UTF_8);


                                            try {
                                                serverAddr = InetAddress.getByName(sessionManager.getUrl());

                                            } catch (UnknownHostException e) {
                                                snackbar.dismiss();
                                                snackbar = Snackbar.make(coordinatorLayout, "Unknown Host. Check URL", Snackbar.LENGTH_LONG);
                                                snackbar.setAction("Action", null).show();

                                                return null;
                                            }

                                            dp = new DatagramPacket(ba, msg.length(), serverAddr, sessionManager.getPort());

                                            int retries = sessionManager.getUdpGetTimes();

                                            InetAddress localInetAddress = null;
                                            try {

                                                /*int localInetAddressInt = wifiManager.getDhcpInfo ().ipAddress;

                                                //Log.d("WIFI_Y", String.valueOf(localInetAddressInt));
                                                ByteBuffer tmp = ByteBuffer.allocate (4);

                                                tmp.putInt (localInetAddressInt);

                                                //reverseArray(tmp);
                                                byte swap;
                                                for(int i = 0; i < 2; i++) {

                                                    swap = tmp.get(i);
                                                    tmp.put(i, tmp.get(3 - i));
                                                    tmp.put(3 - i, swap);

                                                }

                                                for(int i = 0; i < 4; i++) {

                                                    Log.d("WIFI_Y", String.valueOf(tmp.get(i)));

                                                }



                                                localInetAddress = InetAddress.getByAddress (tmp.array ());



                                                ds = new DatagramSocket (sessionManager.getPort(), localInetAddress);*/
                                                //ds.setSoTimeout(sessionManager.getSocketTimeOut() * 1000);

                                                /*ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                                Network[] network = connectivityManager.getAllNetworks();
                                                if(network != null && network.length >0 ){
                                                    for(int i = 0 ; i < network.length ; i++){
                                                        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network[i]);
                                                        int networkType = networkInfo.getType();
                                                        if(ConnectivityManager.TYPE_MOBILE == networkType ){
                                                            connectivityManager.bindProcessToNetwork(network[i]);
                                                        }
                                                    }
                                                }*/



                                                /*
                                                if(mobileDataEnabled(getApplicationContext())){
                                                    appClosedData = true;
                                                    setMobileDataEnabled(getApplicationContext(), false);
                                                }*/

                                                int localInetAddressInt = wifiManager.getDhcpInfo ().ipAddress;

                                                //Log.d("WIFI_Y", String.valueOf(localInetAddressInt));
                                                ByteBuffer tmp = ByteBuffer.allocate (4);

                                                tmp.putInt (localInetAddressInt);

                                                //reverseArray(tmp);
                                                byte swap;
                                                for(int i = 0; i < 2; i++) {

                                                    swap = tmp.get(i);
                                                    tmp.put(i, tmp.get(3 - i));
                                                    tmp.put(3 - i, swap);

                                                }

                                                for(int i = 0; i < 4; i++) {

                                                    Log.d("WIFI_Y", String.valueOf(tmp.get(i)));

                                                }



                                                localInetAddress = InetAddress.getByAddress (tmp.array ());



                                                ds = new DatagramSocket (sessionManager.getPort(), localInetAddress);
                                                ds.setSoTimeout(sessionManager.getSocketTimeOut() * 1000);

                                                //Toast.makeText(mContext,mContext.getString(R.string.please_connect_to_internet),Toast.LENGTH_SHORT).show();



                                            } catch (SocketException e) {
                                                e.printStackTrace();
                                                Log.d("WIFI_Y", Log.getStackTraceString(e));
                                                Log.d("WIFI_Y", "Socket nahi bana");
                                                Log.d("WIFI_Y", localInetAddress.getCanonicalHostName());
                                            } catch (UnknownHostException e) {
                                                //e.printStackTrace();
                                                Log.d("WIFI_Y", "unknownhost");
                                            }

                                            while(true){
                                                try {
                                                    ds.send(dp);

                                                    rcv = new byte[sessionManager.getPacketLength()];
                                                    dp = new DatagramPacket(rcv, rcv.length);
                                                    ds.receive(dp);
                                                    msg = new String(rcv, 0, dp.getLength());

                                                    if(msg.length() > 0){
                                                        snackbar.dismiss();
                                                        snackbar = Snackbar.make(coordinatorLayout, "Received data: " + msg, Snackbar.LENGTH_LONG);
                                                        snackbar.setAction("Action", null).show();

                                                        /*Recieved confirmaion. Module works. Send hello string*/

                                                        if(TextUtils.equals(msg.toLowerCase(), "module_tested")){

                                                            startActivity(new Intent(Home.this, FanActivity.class));


                                                        }

                                                        if(ds != null)
                                                            ds.close();

                                                        //if(appClosedData) setMobileDataEnabled(getApplicationContext(), true);
                                                        return null;



                                                    }
                                                    // If you're not using an infinite loop:
                                                    //mDataGramSocket.close();

                                                } catch (SocketTimeoutException | NullPointerException e) {
                                                    // no response received after 1 second. continue sending

                                                    e.printStackTrace();
                                                    if(retries < 0){
                                                        snackbar.dismiss();
                                                        snackbar = Snackbar.make(coordinatorLayout, "Timed Out. Retried "+ sessionManager.getUdpGetTimes()+ " times", Snackbar.LENGTH_LONG);
                                                        snackbar.setAction("Action", null).show();
                                                        if(ds != null)
                                                            ds.close();
                                                        break;
                                                    }
                                                    else{
                                                        retries--;
                                                        snackbar.dismiss();
                                                        snackbar = Snackbar.make(coordinatorLayout, "Timed Out. Retrying " +
                                                                (sessionManager.getUdpGetTimes() - retries) + "th/st time", Snackbar.LENGTH_LONG);
                                                        snackbar.setAction("Action", null).show();

                                                    }

                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            return null;
                                        }
                                    };

                                    async_cient_test.execute();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .show();

                    return;

                }

                if(wifitype(selectedWifi.capabilities) == TYPE_OPEN){

                    connectWifi("Open");
                    return;
                }

                new AlertDialog.Builder(Home.this)
                        .setTitle("Enter Password")
                        .setMessage("Type the password below")
                        .setView(txtUrl)
                        .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String password = txtUrl.getText().toString();
                                wifiManager.disconnect();
                                snackbar = Snackbar.make(coordinatorLayout, "Connecting to: " + selectedWifi.SSID, Snackbar.LENGTH_INDEFINITE);
                                snackbar.setAction("Action", null).show();

                                connectWifi(password);



                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();
            }
        });



        //TODO: Reflect changes from Settings Activity

        listView.setLongClickable(true);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                View inflatedView = getLayoutInflater().inflate(R.layout.d_conf_view, null, false);
                final TextView uname = (TextView) inflatedView.findViewById(R.id.username);
                final TextView psswd = (TextView) inflatedView.findViewById(R.id.password);
                final ProgressBar pb = (ProgressBar) inflatedView.findViewById(R.id.progressBar);

                selectedWifi =  wifiScanList.get(position);

                if(!TextUtils.equals(wifiManager.getConnectionInfo().getBSSID(), selectedWifi.BSSID)){

                    new AlertDialog.Builder(Home.this)
                            .setTitle("Enter Configuration")
                            .setMessage("Connect to this Wi-Fi Access point First")
                            .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    View inflatedView = getLayoutInflater().inflate(R.layout.d_text_view, null, false);
                                    final TextView txtUrl = (TextView) inflatedView.findViewById(R.id.textView);

                                    if(wifitype(selectedWifi.capabilities) == TYPE_OPEN){

                                        connectWifi("Open");
                                        return;
                                    }

                                    new AlertDialog.Builder(Home.this)
                                            .setTitle("Enter Password")
                                            .setMessage("Type the password below")
                                            .setView(txtUrl)
                                            .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    String password = txtUrl.getText().toString();
                                                    wifiManager.disconnect();
                                                    snackbar = Snackbar.make(coordinatorLayout, "Connecting to: " + selectedWifi.SSID, Snackbar.LENGTH_INDEFINITE);
                                                    snackbar.setAction("Action", null).show();

                                                    connectWifi(password);



                                                }
                                            })
                                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                }
                                            })
                                            .show();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .show();
                    return true;
                }

                uname.setHint("Username");
                psswd.setHint("Password");



                /*sendOnlyUDP udpsendpacket = new sendOnlyUDP();
                udpsendpacket.execute("HELLO");*/

                sendOnlyUDP sudp = new sendOnlyUDP();
                sudp.execute("HELLO");


                new AlertDialog.Builder(Home.this)
                        .setTitle("Enter Configuration")
                        .setMessage("Type the credentials  below")
                        .setView(inflatedView)
                        .setPositiveButton("Configure", new DialogInterface.OnClickListener() {
                            public InetAddress serverAddr;

                            public void onClick(DialogInterface dialog, int whichButton) {
                                    uname.setVisibility(View.INVISIBLE);
                                    psswd.setVisibility(View.INVISIBLE);
                                    pb.setVisibility(View.VISIBLE);

                                    ba = ("HELLO USERNAME_ENTERED " + uname.getText().toString()
                                            + " PASSWORD_ENTERED " + psswd.getText().toString()
                                            + " CLOSECONNECTION").getBytes(StandardCharsets.UTF_8);
                                    msg = new String(ba, StandardCharsets.UTF_8);





                                if(snackbar != null)
                                    snackbar.dismiss();
                                snackbar = Snackbar.make(coordinatorLayout, "Sending UDP Packets to: \'" + sessionManager.getUrl()
                                        + "\' port: \'" + sessionManager.getPort() + "\' Msg: \"" + msg + "\"", Snackbar.LENGTH_INDEFINITE);
                                snackbar.setAction("Action", null).show();
                                ds = null;

                                AsyncTask async_cient = new AsyncTask() {
                                    @Override
                                    protected Object doInBackground(Object[] params) {

                                        try {
                                            serverAddr = InetAddress.getByName(sessionManager.getUrl());
                                        } catch (UnknownHostException e) {
                                            snackbar.dismiss();
                                            snackbar = Snackbar.make(coordinatorLayout, "Unknown Host. Check URL", Snackbar.LENGTH_LONG);
                                            snackbar.setAction("Action", null).show();
                                            return null;
                                        }

                                        dp = new DatagramPacket(ba, msg.length(), serverAddr, sessionManager.getPort());

                                        int retries = sessionManager.getUdpGetTimes();

                                        try {
                                            int localInetAddressInt = wifiManager.getDhcpInfo ().ipAddress;

                                            //Log.d("WIFI_Y", String.valueOf(localInetAddressInt));
                                            ByteBuffer tmp = ByteBuffer.allocate (4);

                                            tmp.putInt (localInetAddressInt);

                                            //reverseArray(tmp);
                                            byte swap;
                                            for(int i = 0; i < 2; i++) {

                                                swap = tmp.get(i);
                                                tmp.put(i, tmp.get(3 - i));
                                                tmp.put(3 - i, swap);

                                            }

                                            for(int i = 0; i < 4; i++) {

                                                Log.d("WIFI_Y", String.valueOf(tmp.get(i)));

                                            }


                                            InetAddress localInetAddress = InetAddress.getByAddress(tmp.array());



                                            ds = new DatagramSocket (sessionManager.getPort(), localInetAddress);
                                            ds.setSoTimeout(sessionManager.getSocketTimeOut() * 1000);

                                            //ds = new DatagramSocket();
                                        } catch (SocketException e) {
                                            e.printStackTrace();
                                        } catch (UnknownHostException e) {
                                            //e.printStackTrace();
                                        }

                                        while(true){
                                            try {
                                                ds.send(dp);

                                                byte[] rcv = new byte[sessionManager.getPacketLength()];
                                                dp = new DatagramPacket(rcv, rcv.length);
                                                ds.receive(dp);
                                                msg = new String(rcv, 0, dp.getLength());

                                                if(msg.length() > 0){
                                                    snackbar.dismiss();
                                                    snackbar = Snackbar.make(coordinatorLayout, "Received data: " + msg, Snackbar.LENGTH_LONG);
                                                    snackbar.setAction("Action", null).show();
                                                    if(ds != null)
                                                        ds.close();
                                                    return null;
                                                }
                                                // If you're not using an infinite loop:
                                                //mDataGramSocket.close();

                                            } catch (SocketTimeoutException | NullPointerException e) {
                                                // no response received after 1 second. continue sending

                                                e.printStackTrace();
                                                if(retries < 0){
                                                    snackbar.dismiss();
                                                    snackbar = Snackbar.make(coordinatorLayout, "Timed Out. Retried 20 times", Snackbar.LENGTH_LONG);
                                                    snackbar.setAction("Action", null).show();
                                                    if(ds != null)
                                                        ds.close();
                                                    break;
                                                }
                                                else{
                                                    retries--;
                                                    snackbar.dismiss();
                                                    snackbar = Snackbar.make(coordinatorLayout, "Timed Out. Retrying " +
                                                            (sessionManager.getUdpGetTimes() - retries) + "th/st time", Snackbar.LENGTH_LONG);
                                                    snackbar.setAction("Action", null).show();

                                                }

                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        return null;
                                    }
                                };

                                async_cient.execute();




                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();

                return true;
            }
        });

        displayLocationSettingsRequest(getApplicationContext());





    }

    @Override
    protected void onResume() {
        super.onResume();


        activityInFront = true;

        scantime = sessionManager.getFreq() * 1000;

        if(scantime < 0) scantime = 7500;

        Log.d("WIFI_Y", "onresume");

        if(timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (activityInFront) {
                        wifiManager.startScan();
                        Log.d("WIFI_Y", "scanning, scantime= ");
                    }
                }
            }, 0, scantime);
        }

        if (!mIsWifiReceiverRegistered) {
            if (mywifiReceiver == null)
                mywifiReceiver = new WifiReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
            intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
            registerReceiver(mywifiReceiver, intentFilter);
            mIsWifiReceiverRegistered = true;
        }

        if(!mIsWifiSSIDRegistered){
            if (mywifiSSIDSChanged == null)
                mywifiSSIDSChanged = new WifiSSIDSChanged();
            IntentFilter intentFilter2 = new IntentFilter();
            intentFilter2.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
            registerReceiver(mywifiReceiver, intentFilter2);
            mIsWifiSSIDRegistered = true;
        }
    }

    @Override
    protected void onPause() {

        super.onPause();

        activityInFront = false;

        timer = null;

        if (mIsWifiReceiverRegistered) {
            unregisterReceiver(mywifiReceiver);
            mywifiReceiver = null;
            mIsWifiReceiverRegistered = false;
        }

        else if (mIsWifiSSIDRegistered) {
            unregisterReceiver(mywifiSSIDSChanged);
            mywifiSSIDSChanged = null;
            mIsWifiSSIDRegistered = false;
        }

        // Other onPause() code here

    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            listView.setVisibility(show ? View.GONE : View.VISIBLE);
            listView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    listView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressCircle.setVisibility(show ? View.VISIBLE : View.GONE);
            progressCircle.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressCircle.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressCircle.setVisibility(show ? View.VISIBLE : View.GONE);
            listView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void permissionscycle(){
        // Here, thisActivity is the current activity

        if (ContextCompat.checkSelfPermission(Home.this,
                Manifest.permission.ACCESS_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED) {


            //TODO: Uncomment if rationale needed
            /*
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(Home.this,
                    Manifest.permission.ACCESS_WIFI_STATE)) {

                // Show an explanation to the user *asynchronously* -- don't block this thread waiting for the user's response! After the user sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
            */
            ActivityCompat.requestPermissions(Home.this,
                    new String[]{Manifest.permission.ACCESS_WIFI_STATE},
                    WIFI_STATE_CODE);


        }



        if (ContextCompat.checkSelfPermission(Home.this,
                Manifest.permission.CHANGE_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            //TODO: Uncomment if rationale needed
            /*
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(Home.this,
                    Manifest.permission.ACCESS_WIFI_STATE)) {

                // Show an explanation to the user *asynchronously* -- don't block this thread waiting for the user's response! After the user sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
            */
            ActivityCompat.requestPermissions(Home.this,
                    new String[]{Manifest.permission.CHANGE_WIFI_STATE},
                    CHANGE_WIFI_STATE_CODE);

        }

        if (ContextCompat.checkSelfPermission(Home.this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            //TODO: Uncomment if rationale needed
            /*
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(Home.this,
                    Manifest.permission.ACCESS_WIFI_STATE)) {

                // Show an explanation to the user *asynchronously* -- don't block this thread waiting for the user's response! After the user sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
            */
            ActivityCompat.requestPermissions(Home.this,
                    new String[]{Manifest.permission.INTERNET},
                    INTERNET_CODE);

        }

        if (ContextCompat.checkSelfPermission(Home.this,
                Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            //TODO: Uncomment if rationale needed
            /*
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(Home.this,
                    Manifest.permission.ACCESS_WIFI_STATE)) {

                // Show an explanation to the user *asynchronously* -- don't block this thread waiting for the user's response! After the user sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
            */
            ActivityCompat.requestPermissions(Home.this,
                    new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                    ACCESS_NETWORK_STATE_CODE);

        }

        if (ContextCompat.checkSelfPermission(Home.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //TODO: Uncomment if rationale needed
            /*
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(Home.this,
                    Manifest.permission.ACCESS_WIFI_STATE)) {

                // Show an explanation to the user *asynchronously* -- don't block this thread waiting for the user's response! After the user sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
            */
            ActivityCompat.requestPermissions(Home.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_CODE);

        }

        if (ContextCompat.checkSelfPermission(Home.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //TODO: Uncomment if rationale needed
            /*
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(Home.this,
                    Manifest.permission.ACCESS_WIFI_STATE)) {

                // Show an explanation to the user *asynchronously* -- don't block this thread waiting for the user's response! After the user sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
            */
            ActivityCompat.requestPermissions(Home.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    ACCESS_COARSE_LOCATION_CODE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // permission was granted, yay! Do the task you need to do.
                Log.d("WIFI_Y", "mili iski: " + requestCode);
        } else {
            Log.d("WIFI_Y", "Iski nahi:" + requestCode);
            permissionscycle();
        }
    }

    private class WifiReceiver extends BroadcastReceiver {

        WifiManager wifiManager;
        SupplicantState s;
        NetworkInfo.DetailedState state;
        @Override
        public void onReceive(Context context, Intent intent) {
            wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);


            s = wifiManager.getConnectionInfo().getSupplicantState();
            state = WifiInfo.getDetailedStateOf(s);

            Log.d("WIFI_Y", "Inner class, sup state: " +  state + " ssid: " + wifiManager.getConnectionInfo().getSSID());
            /*if(wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING ||
                    wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED)
                Log.d("WIFI_Y", "Inner Class: Disable hua");

            Log.d("WIFI_Y", "Change ho gaya wifi");*/

            if(state == NetworkInfo.DetailedState.CONNECTING){
                if(snackbar != null)
                    snackbar.dismiss();
                if(selectedWifi != null) {
                    snackbar = Snackbar.make(coordinatorLayout, "Connecting to: " + selectedWifi.SSID, Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("Action", null).show();
                }
            }

            if(state == NetworkInfo.DetailedState.AUTHENTICATING){
                if(snackbar != null)
                    snackbar.dismiss();
                if(selectedWifi != null) {
                    snackbar = Snackbar.make(coordinatorLayout, "Authenticating: " + selectedWifi.SSID, Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("Action", null).show();
                    authenticatingWifi = true;
                }
            }


            if(state == NetworkInfo.DetailedState.DISCONNECTED){

            }

            if(state == NetworkInfo.DetailedState.OBTAINING_IPADDR){
                if(snackbar != null)
                    snackbar.dismiss();
                if(selectedWifi != null) {
                    snackbar = Snackbar.make(coordinatorLayout, "Obtaining IP address: " + wifiManager.getConnectionInfo().getSSID(), Snackbar.LENGTH_SHORT);
                    snackbar.setAction("Action", null).show();
                }
            }

            if(state == NetworkInfo.DetailedState.CONNECTED){

                if(snackbar != null)
                    snackbar.dismiss();
                if(selectedWifi != null) {
                    snackbar = Snackbar.make(coordinatorLayout, "Connected to: " + selectedWifi.SSID, Snackbar.LENGTH_SHORT);
                    snackbar.setAction("Action", null).show();
                }
                wifiManager.startScan();
            }

            /*
            String action  = intent.getAction();
            if(action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)){
                Log.d("WifiReceiver", ">>>>SUPPLICANT_STATE_CHANGED_ACTION<<<<<<");
                SupplicantState supl_state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                switch(supl_state){
                    case ASSOCIATED:Log.i("SupplicantState", "ASSOCIATED");
                        break;
                    case ASSOCIATING:Log.i("SupplicantState", "ASSOCIATING");
                        break;

                    case AUTHENTICATING:Log.i("SupplicantState", "Authenticating...");
                        break;

                    case COMPLETED: Log.i("SupplicantState", "Connected");
                        break;
                    case DISCONNECTED:Log.i("SupplicantState", "Disconnected");
                        break;
                    case DORMANT:Log.i("SupplicantState", "DORMANT");
                        break;
                    case FOUR_WAY_HANDSHAKE:Log.i("SupplicantState", "FOUR_WAY_HANDSHAKE");
                        break;
                    case GROUP_HANDSHAKE:Log.i("SupplicantState", "GROUP_HANDSHAKE");
                        break;
                    case INACTIVE:Log.i("SupplicantState", "INACTIVE");
                        break;
                    case INTERFACE_DISABLED:Log.i("SupplicantState", "INTERFACE_DISABLED");
                        break;
                    case INVALID:Log.i("SupplicantState", "INVALID");
                        appRequest = false;
                        break;
                    case SCANNING:Log.i("SupplicantState", "SCANNING");
                        break;
                    case UNINITIALIZED:Log.i("SupplicantState", "UNINITIALIZED");
                        break;
                    default:Log.i("SupplicantState", "Unknown");
                        break;

                }


            }*/

            int supl_error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
            if(supl_error == WifiManager.ERROR_AUTHENTICATING){
                Log.i("SupplicantState", "ERROR_AUTHENTICATING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                if(snackbar != null)
                    snackbar.dismiss();
                if(selectedWifi != null) {
                    if(snackbar != null) {
                        snackbar = Snackbar.make(coordinatorLayout, "Failed Authentication: " + selectedWifi.SSID, Snackbar.LENGTH_SHORT);
                        snackbar.setAction("Action", null).show();
                        authenticatingWifi = false;

                    }
                }
            }



            if(wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED){
                //Log.d("WIFI_Y", "Enabled ho gaya wifi");
                enable_wifi.setVisibility(View.INVISIBLE);
                wifi_disabled.setVisibility(View.INVISIBLE);
                listView.setVisibility(View.VISIBLE);

            }

            else if(wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED
                    || wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING){
                Log.d("WIFI_Y", "Disabled ho gaya wifi");
                enable_wifi.setVisibility(View.VISIBLE);
                wifi_disabled.setVisibility(View.VISIBLE);
                listView.setVisibility(View.INVISIBLE);
            }

        }
    }

    private class WifiSSIDSChanged extends BroadcastReceiver{

        WifiManager wifiManager;

        @Override
        public void onReceive(Context context, Intent intent) {
            wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
            wifiManager.startScan();
        }
    }

    private int wifitype(String s){
        if(s.toUpperCase().contains("WPA")) return TYPE_WPA;
        else if(s.toUpperCase().contains("WEP")) return TYPE_WEP;

        return TYPE_OPEN;
    }

    private String wifiSec(int s){
        switch (s){
            case TYPE_WPA: return "WPA/WPA2";
            case TYPE_WEP: return "WEP";
        }

        return "OPEN";
    }

    private int connectWifi(String password){
        int networkId = -1;
        connectedWifi = true;
        try {

            Log.v("rht", "Item clicked, SSID " + selectedWifi.SSID + " Security : " + selectedWifi.capabilities);

            String networkSSID = selectedWifi.SSID;
            String networkPass = password;

            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quotes
            conf.status = WifiConfiguration.Status.ENABLED;
            conf.priority = 40;

            if (selectedWifi.capabilities.toUpperCase().contains("WEP")) {
                Log.v("rht", "Configuring WEP");
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

                if (networkPass.matches("^[0-9a-fA-F]+$")) {
                    conf.wepKeys[0] = networkPass;
                } else {
                    conf.wepKeys[0] = "\"".concat(networkPass).concat("\"");
                }

                conf.wepTxKeyIndex = 0;

            } else if (selectedWifi.capabilities.toUpperCase().contains("WPA")) {
                Log.v("rht", "Configuring WPA");

                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

                conf.preSharedKey = "\"" + networkPass + "\"";

            } else {
                Log.v("rht", "Configuring OPEN network");
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.allowedAuthAlgorithms.clear();
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            }


            networkId = wifiManager.addNetwork(conf);


            Log.v("rht", "Add result " + networkId);

            String allssidsconfs = "";
            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration i : list) {
                allssidsconfs.concat(i.SSID).concat(" ");
                Log.d("rht", "Configured: " + i.SSID);
                if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                    Log.v("rht", "WifiConfiguration SSID " + i.SSID);

                    /*boolean isDisconnected = wifiManager.disconnect();
                    Log.v("rht", "isDisconnected : " + isDisconnected);*/

                    boolean isEnabled;
                    isEnabled = wifiManager.enableNetwork(i.networkId, true);
                    Log.v("rht", "isEnabled : " + isEnabled);

                    boolean isReconnected = wifiManager.reconnect();
                    Log.v("rht", "isReconnected : " + isReconnected);



                    break;
                }
            }

            Log.d("rht", "All confs: " + allssidsconfs);

        } catch (Exception e) {
            e.printStackTrace();
        }



        return networkId;
    }

    private void stopPeriodicScan(){
        timer = null;
    }

    private void startPeriodicScan(){
        if(timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (activityInFront) {
                        wifiManager.startScan();
                        Log.d("WIFI_Y", "scanning, scantime= ");
                    }
                }
            }, 0, scantime);
        }
    }

    private void displayLocationSettingsRequest(final Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            static final int REQUEST_CHECK_SETTINGS = 1;

            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i("WIFI_Y", "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("WIFI_Y", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(Home.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i("WIFI_Y", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("WIFI_Y", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    public class GpsLocationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                //Toast.makeText(context, "in android.location.PROVIDERS_CHANGED",
                        //Toast.LENGTH_SHORT).show();
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                if( !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
                    //NO GPS
                    displayLocationSettingsRequest(getApplicationContext());
                }else{
                    //Gps is on
                }
            }
        }
    }



    public static void reverseArray(ByteBuffer[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        ByteBuffer tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    //the method below enables/disables mobile data depending on the Boolean 'enabled' parameter.
    private void setMobileDataEnabled(Context context, boolean enabled) {
        final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Class conmanClass = null;
        try {
            conmanClass = Class.forName(conman.getClass().getName());
            final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            final Object iConnectivityManager = iConnectivityManagerField.get(conman);
            final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
            final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);
            setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);

        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    // below method returns true if mobile data is on and vice versa
    private boolean mobileDataEnabled(Context context){
        boolean mobileDataEnabled = false; // Assume disabled
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true); // Make the method callable
            // get the setting for "mobile data"
            mobileDataEnabled = (Boolean)method.invoke(cm);
        } catch (Exception e) {
            // Some problem accessible private API
            // TODO do whatever error handling you want here
        }
        return mobileDataEnabled;
    }

    private class sendOnlyUDP extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... params) {

            ba = (params[0]).getBytes(StandardCharsets.UTF_8);
            msg = new String(ba, StandardCharsets.UTF_8);

            InetAddress serverAddr;
            try {
                serverAddr = InetAddress.getByName(sessionManager.getUrl());
            } catch (UnknownHostException e) {
                snackbar.dismiss();
                snackbar = Snackbar.make(coordinatorLayout, "Unknown Host. Check URL", Snackbar.LENGTH_LONG);
                snackbar.setAction("Action", null).show();
                return null;
            }

            dp = new DatagramPacket(ba, msg.length(), serverAddr, sessionManager.getPort());



            try {

                int localInetAddressInt = wifiManager.getDhcpInfo ().ipAddress;

                //Log.d("WIFI_Y", String.valueOf(localInetAddressInt));
                ByteBuffer tmp = ByteBuffer.allocate (4);

                tmp.putInt (localInetAddressInt);

                //reverseArray(tmp);
                byte swap;
                for(int i = 0; i < 2; i++) {

                    swap = tmp.get(i);
                    tmp.put(i, tmp.get(3 - i));
                    tmp.put(3 - i, swap);

                }

                for(int i = 0; i < 4; i++) {

                    Log.d("WIFI_Y", String.valueOf(tmp.get(i)));

                }


                InetAddress localInetAddress = InetAddress.getByAddress(tmp.array());



                ds = new DatagramSocket (sessionManager.getPort(), localInetAddress);
                ds.setSoTimeout(sessionManager.getSocketTimeOut() * 1000);
                //ds = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                //e.printStackTrace();
            }


            try {
                int sendtimes = sessionManager.getUdpGetTimes();
                while(sendtimes > 0){
                    ds.send(dp);
                    sendtimes--;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(ds != null)
                ds.close();
        }
    }


}

/*



* */