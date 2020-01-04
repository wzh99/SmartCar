package com.ei312.ui.control;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.ei312.ui.MainActivity;
import com.ei312.ui.util.ImageListener;
import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ei312.ui.R;

import org.jetbrains.annotations.NotNull;

public class StickActivity extends AppCompatActivity {

    private static final String TAG = StickActivity.class.getSimpleName();
    private ImageView backImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stick);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        backImage = findViewById(R.id.image);
        MainActivity.imgReceiver.setListener(new ImageListener() {
            @Override
            public void onImage(@NotNull Bitmap img) {
                backImage.setImageBitmap(img);
            }
        });

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("手柄控制");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Joystick joystick = (Joystick)findViewById(R.id.joystick);
        joystick.setJoystickListener(new JoystickListener() {
            @Override
            public void onDown() {
            }

            @Override
            public void onDrag(float degrees, float offset) {
                int direction = getTheDirection(degrees);
                switch (direction) {
                    case 1 : {
                        Log.d(TAG, "forward");
                        MainActivity.msgSender.send("A");
                        break;
                    }
                    case 2:{
                        Log.d(TAG, "left");
                        MainActivity.msgSender.send("L");
                        break;
                    }
                    case 3 :{
                        Log.d(TAG, "backward");
                        MainActivity.msgSender.send("B");
                        break;
                    }
                    case 4 : {
                        Log.d(TAG, "right");
                        MainActivity.msgSender.send("R");
                        break;
                    }
                    case -1:{
                        Log.d(TAG, "stop");
                        MainActivity.msgSender.send("P");
                        break;
                    }
                }

            }

            @Override
            public void onUp() {
            }
        });
    }
    private int getTheDirection(float degrees) {
        if ( 70 < degrees && degrees < 110) {
            return 1;//forward
        } else if ((160 < degrees && degrees < 180) || (-180 < degrees && degrees < -160)) {
            return 2; //left
        } else if (-110 < degrees && degrees < -70) {
            return 3; //back
        } else if (-20 < degrees && degrees < 20) {
            return 4; //right
        }
        else return -1;
    }
}
