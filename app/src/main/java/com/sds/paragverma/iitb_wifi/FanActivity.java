package com.sds.paragverma.iitb_wifi;

import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.sds.paragverma.iitb_wifi.R.id.coordinatorLayout;

public class FanActivity extends AppCompatActivity implements View.OnClickListener {

    byte[] ba;
    String msg;
    CoordinatorLayout coordinatorLayout;
    Snackbar snackbar;
    SessionManager sessionManager;
    DatagramSocket ds;
    DatagramPacket dp;
    private InetAddress serverAddr;
    private String recv;

    WifiManager wifiManager;

    Switch switches[];
    private int i;

    private boolean hardCheck;
    private boolean simpleToggle;

    int abhiOnKarna;


    Switch selectedFan;
    Switch touchedFan;

    boolean cancelToggle;

    String sending;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fan);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        sessionManager = new SessionManager(getApplicationContext());

        int retries = sessionManager.getUdpGetTimes();

        cancelToggle = false;

        simpleToggle = false;

        sendRecieveUDPAskSpeed askspeed = new sendRecieveUDPAskSpeed();
        askspeed.execute("ASK_SPEED");

        int[] switchids = {R.id.speed1, R.id.speed2, R.id.speed3, R.id.speed4, R.id.speed5};

        switches = new Switch[5];

        for(i = 0; i < 5; i++){
            switches[i] = (Switch) findViewById(switchids[i]);
            switches[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
                    selectedFan = (Switch) buttonView;

                    if(simpleToggle){
                        Log.d("WIFI_Y", "simple return");
                        //selectedFan.toggle();
                        return;
                    }

                    if(cancelToggle){
                        selectedFan.toggle();
                        return;
                    }

                    if(hardCheck) {
                        selectedFan.toggle();
                        new AlertDialog.Builder(FanActivity.this)
                                .setTitle("Do you want to change the speed to this?")
                                .setMessage("Press Yes or No")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        sendRecieveUDPChangeSpeed sendRecieveUDPChangeSpeed = new sendRecieveUDPChangeSpeed();

                                        switch (buttonView.getId()){
                                            case R.id.speed1: sendRecieveUDPChangeSpeed.execute("SPEED1");
                                                break;
                                            case R.id.speed2: sendRecieveUDPChangeSpeed.execute("SPEED2");
                                                break;
                                            case R.id.speed3: sendRecieveUDPChangeSpeed.execute("SPEED3");
                                                break;
                                            case R.id.speed4: sendRecieveUDPChangeSpeed.execute("SPEED4");
                                                break;
                                            case R.id.speed5: sendRecieveUDPChangeSpeed.execute("SPEED5");
                                                break;
                                        }
                                        hardCheck = false;

                                        if(selectedFan.isChecked())
                                            selectedFan.toggle();

                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        if(selectedFan.isChecked()){
                                            hardCheck = false;
                                            selectedFan.toggle();
                                        }



                                    }
                                })
                                .setCancelable(false)
                                .show();

                    }




                    cancelToggle = false;


                }




            });

            switches[i].setOnTouchListener(new View.OnTouchListener() {
                Switch selSwitch;
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    selSwitch = (Switch)v;
                    hardCheck = true;
                    cancelToggle = selSwitch.isChecked();
                    touchedFan = selSwitch;
                    return false;

                }
            });
        }

        wifiManager =
                (WifiManager) getApplicationContext ()
                        .getSystemService (Context.WIFI_SERVICE);

        findViewById(R.id.stopfan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRecieveUDPChangeSpeed sendRecieveUDPChangeSpeed = new sendRecieveUDPChangeSpeed();
                sendRecieveUDPChangeSpeed.execute("SPEEDSTOP");
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRecieveUDPAskSpeed askspeed = new sendRecieveUDPAskSpeed();
                askspeed.execute("ASK_SPEED");



            }
        });


    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.speed1:
                break;
            case R.id.speed2:
                break;
            case R.id.speed3:
                break;
            case R.id.speed4:
                break;
            case R.id.speed5:
                break;

        }
    }

    private class sendRecieveUDPAskSpeed extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            String msg;

            ba = (params[0]).getBytes(StandardCharsets.UTF_8);
            msg = new String(ba, StandardCharsets.UTF_8);
            sending = params[0];

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
                if(wifiManager == null) wifiManager =
                        (WifiManager) getApplicationContext ()
                                .getSystemService (Context.WIFI_SERVICE);

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
                        return msg;
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

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            for (int i = 0; i < 5; i++) {
                if (switches[i].isChecked())
                    switches[i].toggle();
            }

            Log.d("WIFI_Y", "Received speed: " + s);

            if(s != null) {

                switch (s) {
                    case "SPEED1":
                        if (!switches[0].isChecked()) switches[0].toggle();
                        break;
                    case "SPEED2":
                        if (!switches[1].isChecked()) switches[1].toggle();
                        break;
                    case "SPEED3":
                        if (!switches[2].isChecked()) switches[2].toggle();
                        break;
                    case "SPEED4":
                        if (!switches[3].isChecked()) switches[3].toggle();
                        else Log.d("WIFI_Y", "Checked already");
                        break;
                    case "SPEED5":
                        if (!switches[4].isChecked()) switches[4].toggle();
                        break;
                    case "SPEEDSTOP":
                        for (int i = 0; i < 5; i++) {
                            if (switches[i].isChecked())
                                switches[i].toggle();
                        }
                }

                if (snackbar != null) snackbar.dismiss();
                snackbar = Snackbar.make(coordinatorLayout, "Changed", Snackbar.LENGTH_SHORT);
                snackbar.setAction("Action", null).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(snackbar != null) snackbar.dismiss();
            snackbar = Snackbar.make(coordinatorLayout, "Sending UDP Packets", Snackbar.LENGTH_SHORT);
            snackbar.setAction("Action", null).show();
        }
    }


    private class sendOnlyUDP extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... params) {
            ba = (params[0]).getBytes(StandardCharsets.UTF_8);
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
    }

    private class sendRecieveUDPChangeSpeed extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            ba = (params[0]).getBytes(StandardCharsets.UTF_8);
            msg = new String(ba, StandardCharsets.UTF_8);

            sending = params[0];
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
                        return msg;
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

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (TextUtils.equals(s, "CHANGED")) {

                if (TextUtils.equals(sending, "SPEEDSTOP")) {
                    simpleToggle = true;
                    //switch off all
                    for (i = 0; i < 5; i++) {
                        if (switches[i].isChecked()) switches[i].toggle();
                    }
                    simpleToggle = false;

                }

                else {

                    Log.d("WIFI_Y", "Switch toggle kar: " + touchedFan.getId());
                    // Switch off other switches
                    simpleToggle = true;

                    for (i = 0; i < 5; i++) {

                            if (switches[i].isChecked()){
                                Log.d("WIFI_Y", i + " : is checked");
                                switches[i].toggle();
                            }

                            if(switches[i].getId() == touchedFan.getId()){
                                Log.d("WIFI_Y", i + " : is selectedfan");
                            }

                    }

                    Log.d("WIFI_Y", selectedFan.getId() + " : Toggling");
                    // Switch on current wala
                    if (!touchedFan.isChecked()) touchedFan.toggle();

                    simpleToggle = false;
                }
            }
        }




        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(snackbar != null) snackbar.dismiss();
            snackbar = Snackbar.make(coordinatorLayout, "Sending UDP Packets", Snackbar.LENGTH_SHORT);
            snackbar.setAction("Action", null).show();
        }
    }






}


