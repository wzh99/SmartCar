package com.ei312.ui.control;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.ei312.ui.MainActivity;
import com.ei312.ui.R;
import com.ei312.ui.util.ImageListener;

import org.jetbrains.annotations.NotNull;

public class GestureActivity extends AppCompatActivity {

    private static final float FLIP_DISTANCE = 300;
    private GestureDetector mDetector;
    private ImageView mImageView;
    private ImageView backImage;
    private static final String TAG = GestureActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture);

        backImage = findViewById(R.id.img);

        MainActivity.imgReceiver.setListener(new ImageListener() {
            @Override
            public void onImage(@NotNull Bitmap img) {
                backImage.setImageBitmap(img);
            }
        });

        mDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            //region defaults
            @Override
            public boolean onDown(MotionEvent motionEvent) {

                return false;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {

            }
            //endregion

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
                float deltaX = e1.getX() - e2.getX();
                float deltaY = e1.getY() - e2.getY();
                if (deltaX > FLIP_DISTANCE) {
                    Log.d(TAG,"left");
                    MainActivity.msgSender.send("L");
                } else if (deltaX < -FLIP_DISTANCE) {
                    Log.d(TAG,"right");
                    MainActivity.msgSender.send("R");
                } else if (deltaY > FLIP_DISTANCE) {
                    Log.d(TAG,"forward");
                    MainActivity.msgSender.send("A");
                } else if (deltaY < -FLIP_DISTANCE) {
                    Log.d(TAG,"backward");
                    MainActivity.msgSender.send("B");
                } else {
                    Log.d(TAG,"stop");
                    MainActivity.msgSender.send("P");
                }
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
        }
        return mDetector.onTouchEvent(event);
    }


}
