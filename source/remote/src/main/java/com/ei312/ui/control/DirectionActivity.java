package com.ei312.ui.control;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.ei312.ui.MainActivity;
import com.ei312.ui.R;
import com.ei312.ui.util.ImageListener;

import androidx.appcompat.widget.Toolbar;

import org.jetbrains.annotations.NotNull;


public class DirectionActivity extends AppCompatActivity {

    private static final String TAG = DirectionActivity.class.getSimpleName();
    private ImageView btn_forward,btn_backward,btn_left,btn_right,btn_stop;
    private ImageView backImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        backImage = findViewById(R.id.image);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("方向控制");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        MainActivity.imgReceiver.setListener(new ImageListener() {
            @Override
            public void onImage(@NotNull Bitmap img) {
                backImage.setImageBitmap(img);
            }
        });
        btn_stop = (ImageView) findViewById(R.id.stop);
        btn_forward = (ImageView) findViewById(R.id.forwarding);
        btn_backward = (ImageView) findViewById(R.id.back);
        btn_left=(ImageView) findViewById(R.id.left);
        btn_right=(ImageView) findViewById(R.id.right);
        btn_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "forward");
                MainActivity.msgSender.send("A");
            }
        });
        btn_backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "backward");
                MainActivity.msgSender.send("B");
            }
        });
        btn_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "left");
                MainActivity.msgSender.send("L");
            }
        });
        btn_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "right");
                MainActivity.msgSender.send("R");

            }
        });
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "stop");
                MainActivity.msgSender.send("P");

            }
        });
    }
}
