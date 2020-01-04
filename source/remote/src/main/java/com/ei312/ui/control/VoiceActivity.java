package com.ei312.ui.control;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ei312.ui.MainActivity;
import com.ei312.ui.R;
import com.ei312.ui.util.ImageListener;
import com.google.gson.Gson;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class VoiceActivity extends AppCompatActivity {
    TextView voiceResult;
    private ImageView backImage;
    private static final String TAG = VoiceActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);

        voiceResult = findViewById(R.id.voice_result);
        backImage = findViewById(R.id.image);
        MainActivity.imgReceiver.setListener(new ImageListener() {
            @Override
            public void onImage(@NotNull Bitmap img) {
                backImage.setImageBitmap(img);
            }
        });


        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5bc5dd74 ");
    }

    public void open(View view) {
        initSpeech(this);
    }

    public void initSpeech(final VoiceActivity context) {
        //1.创建RecognizerDialog对象
        RecognizerDialog mDialog = new RecognizerDialog(context, null);
        //2.设置accent、language等参数
        mDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mDialog.setParameter(SpeechConstant.ACCENT, "mandarin");

        mDialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean isLast) {
                if (!isLast) {
                    String result = parseVoice(recognizerResult.getResultString());
                    voiceResult.setText(result);
                }
            }

            @Override
            public void onError(SpeechError speechError) {

            }
        });
        mDialog.show();
    }

    public String parseVoice(String resultString) {
        // TextView textleft = findViewById(R.id.text2);
        Gson gson = new Gson();
        Voice voiceBean = gson.fromJson(resultString, Voice.class);

        StringBuilder sb = new StringBuilder();
        ArrayList<Voice.WSBean> ws = voiceBean.ws;
        for (Voice.WSBean wsBean : ws) {
            String word = wsBean.cw.get(0).w;
            sb.append(word);
        }
        String text= sb.toString();
        String stringA = "前进";
        String stringB = "后退";
        String stringC = "停止";
        String stringD = "左转";
        String stringE = "右转";
        if(text.equals(stringA)) {
            Log.d(TAG, "forward");
            MainActivity.msgSender.send("A");
        }
        if(text.equals(stringB)) {
            Log.d(TAG, "backward");
            MainActivity.msgSender.send("B");
        }
        if(text.equals(stringC)) {
            Log.d(TAG, "stop");
            MainActivity.msgSender.send("P");
        }
        if(text.equals(stringD)) {
            Log.d(TAG, "left");
            MainActivity.msgSender.send("L");
        }
        if(text.equals(stringE)) {
            Log.d(TAG, "right");
            MainActivity.msgSender.send("R");
        }

        return text;
    }

    public class Voice {

        ArrayList<WSBean> ws;

        class WSBean {
            ArrayList<CWBean> cw;
        }
        class CWBean {
            String w;
        }
    }

}
