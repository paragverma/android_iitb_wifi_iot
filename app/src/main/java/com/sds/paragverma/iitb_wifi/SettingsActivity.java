package com.sds.paragverma.iitb_wifi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    SessionManager details;
    EditText freq, url, port, plet, udptimes_et, bindpet, stet;
    TextView freqtv, urltv, porttv, pltv, udptimestv, bindptv, sttv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        String temp;
        details = new SessionManager(getApplicationContext());
        freq = (EditText) findViewById(R.id.scanfreq);
        freqtv = (TextView) findViewById(R.id.freqtv);
        url = (EditText) findViewById(R.id.urllink);
        urltv = (TextView) findViewById(R.id.urltv);
        port = (EditText) findViewById(R.id.porttxt);
        porttv = (TextView) findViewById(R.id.porttv);
        pltv = (TextView)findViewById(R.id.pltv);
        plet = (EditText) findViewById(R.id.packetlength);
        udptimes_et = (EditText) findViewById(R.id.udptimes_et);
        udptimestv = (TextView) findViewById(R.id.udptimestv);
        bindptv = (TextView) findViewById(R.id.bindptv);
        bindpet = (EditText) findViewById(R.id.bindpet);
        stet = (EditText) findViewById(R.id.stet);
        sttv = (TextView) findViewById(R.id.sttv);

        if(details.getFreq() > 0)
            temp = "Frequency: " + details.getFreq();
        else temp = "Frequency: No yet set";
        freqtv.setText(temp);
        if(details.getUrl() != null)
            temp = "UrL: " + details.getUrl();
        else temp = "Url: Not yet set";

        urltv.setText(temp);
        if(details.getPort() > 0)
            temp = "Port: " + details.getPort();
        else temp = "Port: Not yet set";
        porttv.setText(temp);

        udptimestv.setText(String.valueOf(details.getUdpGetTimes()));

        pltv.setText(String.valueOf(details.getPacketLength()));

        bindptv.setText(String.valueOf(details.getDevicePort()));

        sttv.setText(String.valueOf(details.getSocketTimeOut()));

        findViewById(R.id.sfreq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                details.setFreq(Integer.parseInt(freq.getText().toString()));
                Toast.makeText(getApplicationContext(), "Frequency changed",
                        Toast.LENGTH_LONG).show();
                recreate();
            }
        });

        findViewById(R.id.urlbt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                details.setUrl(url.getText().toString());
                Toast.makeText(getApplicationContext(), "url changed",
                        Toast.LENGTH_LONG).show();
                recreate();
            }
        });

        findViewById(R.id.portbt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                details.setPort(Integer.parseInt(port.getText().toString()));
                Toast.makeText(getApplicationContext(), "Port changed",
                        Toast.LENGTH_LONG).show();
                recreate();
            }
        });

        findViewById(R.id.packetlengthchange).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                details.setPacketLength(Integer.parseInt(plet.getText().toString()));
                Toast.makeText(getApplicationContext(), "Port changed",
                        Toast.LENGTH_LONG).show();
                recreate();
            }
        });

        findViewById(R.id.udptimesbt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                details.setUdpSendTimes(Integer.parseInt(udptimes_et.getText().toString()));
                Toast.makeText(getApplicationContext(), "UDP Times changed",
                        Toast.LENGTH_LONG).show();
                recreate();
            }
        });

        findViewById(R.id.bindpbt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                details.setDevicePort(Integer.parseInt(bindpet.getText().toString()));
                Toast.makeText(getApplicationContext(), "Bind Port changed",
                        Toast.LENGTH_LONG).show();
                recreate();
            }
        });

        findViewById(R.id.stbt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                details.setSocketTimeOut(Integer.parseInt(stet.getText().toString()));
                Toast.makeText(getApplicationContext(), "Socket Timeout changed",
                        Toast.LENGTH_LONG).show();
                recreate();
            }
        });
    }
}
