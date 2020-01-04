package com.ei312.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ei312.ui.control.DirectionActivity;
import com.ei312.ui.control.GestureActivity;
import com.ei312.ui.control.GravityActivity;
import com.ei312.ui.control.StickActivity;
import com.ei312.ui.control.VoiceActivity;
import com.ei312.ui.util.ImageReceiver;
import com.ei312.ui.util.MessageSender;

public class MainActivity extends AppCompatActivity {

    private static int msgPort = 12345;
    private EditText et_ip;
    private TextView ipAddr;
    public static MessageSender msgSender;
    private static int imgPort = 54321;
    public static ImageReceiver imgReceiver;

    private Button btn_control,btn_conn,btn_gravity,btn_stick,btn_voice,btn_control_gesture;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        msgSender = new MessageSender(msgPort);
        imgReceiver = new ImageReceiver(imgPort);
        try {
            WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = manager.getConnectionInfo();
            int ip = info.getIpAddress();
            ipAddr = findViewById(R.id.textView);
            ipAddr.setText(String.format("IP Address: %d.%d.%d.%d", ip & 0xff, (ip >> 8) & 0xff, (ip >> 16) &0xff, (ip >> 24) &0xff));
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Cannot get IP address.");
        }

        btn_conn=(Button)findViewById(R.id.btn_conn);
        btn_control=(Button)findViewById(R.id.btn_control);
        btn_gravity=(Button)findViewById(R.id.btn_gravity);
        btn_control_gesture=(Button)findViewById(R.id.btn_control_gesture);
        btn_stick=(Button)findViewById(R.id.btn_stick);
        btn_voice= btn_voice=(Button)findViewById(R.id.btn_voice);
        et_ip=(EditText)findViewById(R.id.IP_input);

        //小车控制
        btn_conn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ip=et_ip.getText().toString();
                msgSender.setHost(ip);
            }
        });
        btn_control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, DirectionActivity.class));
                //MainActivity.this.finish();
            }
        });
        btn_gravity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, GravityActivity.class));
                //MainActivity.this.finish();
            }
        });
        btn_stick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, StickActivity.class));
                //MainActivity.this.finish();
            }
        });
        btn_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VoiceActivity.class);
                startActivity(intent);
            }
        });
        btn_control_gesture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, GestureActivity.class));
                //MainActivity.this.finish();
            }
        });
    }
}
