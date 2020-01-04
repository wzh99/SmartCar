package com.ei312.ui.control;

import com.ei312.ui.MainActivity;
import com.ei312.ui.R;
import com.ei312.ui.util.ImageListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.ImageView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.jetbrains.annotations.NotNull;

public class GravityActivity extends AppCompatActivity {

    private SensorManager mSensorManager;
    private MySensorEventLisener mMySensorEventLisener;
    private static final String TAG = GravityActivity.class.getSimpleName();
    private ImageView mImageView;
    private Sensor mSensor = null;
    private ImageView backImage;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gravity);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        backImage = findViewById(R.id.image);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("重力感应");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);
        mMySensorEventLisener = new MySensorEventLisener();
        Log.d(TAG, "come in ");
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(mMySensorEventLisener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mImageView=(ImageView) findViewById(R.id.stop);

        MainActivity.imgReceiver.setListener(new ImageListener() {
            @Override
            public void onImage(@NotNull Bitmap img) {
                backImage.setImageBitmap(img);
            }
        });
    }

    private class MySensorEventLisener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            //Log.d(TAG, "change");
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = sensorEvent.values[SensorManager.DATA_X];
                float y = sensorEvent.values[SensorManager.DATA_Y];
//                float z = sensorEvent.values[SensorManager.DATA_Z];
                switch (getDirecation(x, y)) {
                    case 1: {
//                        mTextView.setText("FORWARDING");
                        Log.d(TAG, "forward");
                        MainActivity.msgSender.send("A");
                        mImageView.setVisibility(View.VISIBLE);
                        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.forward));
                        break;
                    }
                    case 2: {
                        Log.d(TAG, "back");
                        MainActivity.msgSender.send("B");
//                        mTextView.setText("BACK");
                        mImageView.setVisibility(View.VISIBLE);
                        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.backward));
                        break;
                    }
                    case 3: {
                        Log.d(TAG, "left");
//                        mTextView.setText("LEFT");
                        MainActivity.msgSender.send("L");
                        mImageView.setVisibility(View.VISIBLE);
                        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.left));
                        break;
                    }
                    case 4: {
                        Log.d(TAG, "right");
                        MainActivity.msgSender.send("R");
//                        mTextView.setText("RIGHT");
                        mImageView.setVisibility(View.VISIBLE);
                        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.right));
                        break;
                    }
                    default: {
                        Log.d(TAG, "stop");
                        MainActivity.msgSender.send("P");
                        mImageView.setVisibility(View.INVISIBLE);
//                        mTextView.setText("RIGHT");
                        //mImageView.setImageDrawable(getResources().getDrawable(R.drawable.right));
                        break;

                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }
    private int getDirecation(float x, float y) {
        if (-1 < x && x < 1) {
            if (y < -3) { //forwarding
                return 1;
            } else if (y > 3) { //back
                return 2;
            }
        } else if (x > 4) { //left
            return 3;
        } else if (x < -4) { //right
            return 4;
        }
        return -1;
    }


}
