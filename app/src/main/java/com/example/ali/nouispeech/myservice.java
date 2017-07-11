package com.example.ali.nouispeech;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class  myservice extends Service {
    protected AudioManager mAudioManager;
    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    protected final Messenger mServerMessenger = new Messenger(new IncomingHandler(this));

    protected boolean mIsListening;
    protected volatile boolean mIsCountDownOn;

    static final int MSG_RECOGNIZER_START_LISTENING = 1;
    static final int MSG_RECOGNIZER_CANCEL = 2;

    @Override
    public void onCreate() {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");

        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());

    }

    protected  class IncomingHandler extends android.os.Handler {
        private WeakReference<myservice> mtarget;

        IncomingHandler(myservice target) {
            mtarget = new WeakReference<myservice>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            final myservice target = mtarget.get();
            switch (msg.what) {
                case MSG_RECOGNIZER_START_LISTENING:

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        // turn off beep sound
                        target.mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
                    }
                    if (!target.mIsListening) {
                        target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
                        target.mIsListening = true;
                    }
                    break;

                case MSG_RECOGNIZER_CANCEL:
                    target.mSpeechRecognizer.cancel();
                    target.mIsListening = false;
                    break;


            }
        }
    }
    protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(1000, 500)
    {
        @Override
        public void onTick(long millisUntilFinished) {
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);

        }

        @Override
        public void onFinish() {
            mIsCountDownOn = false;
            mSpeechRecognizer.stopListening();
            Message message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
            try
            {
                mServerMessenger.send(message);
                message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
                mServerMessenger.send(message);
            }
            catch (RemoteException e)
            {

            }

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mIsCountDownOn)
        {
            mNoSpeechCountDown.cancel();
        }
        if (mSpeechRecognizer != null)
        {
            mSpeechRecognizer.destroy();
        }
    }

    protected class SpeechRecognitionListener implements RecognitionListener
    {
        @Override
        public void onReadyForSpeech(Bundle params) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            {
                mIsCountDownOn = true;
                mNoSpeechCountDown.start();
            }

        }

        @Override
        public void onBeginningOfSpeech() {
            if (mIsCountDownOn)
            {
                mIsCountDownOn = false;
                mNoSpeechCountDown.cancel();
            }

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {


        }

        @Override
        public void onError(int error) {

            if (mIsCountDownOn) {
                mIsCountDownOn = true;
                mNoSpeechCountDown.cancel();
            }
            mIsListening = false;

            Message message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
            try {
                mServerMessenger.send(message);
            } catch (RemoteException e) {
                
            }
            mIsCountDownOn = true;
            mNoSpeechCountDown.start();
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String result = matches.get(0);
            String getresults = getresult(result);
            Toast.makeText(getApplicationContext(), getresults , Toast.LENGTH_LONG).show();

            if(RecognizerIntent.RESULT_NO_MATCH == 1) {
                Toast.makeText(getApplicationContext(), "sorry for that  ", Toast.LENGTH_LONG).show();
                mIsCountDownOn = false;
                mSpeechRecognizer.cancel();
                mNoSpeechCountDown.start();
            }
            mIsCountDownOn = true;
            mNoSpeechCountDown.start();
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    }
        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return mServerMessenger.getBinder();
        }



    public String minDistanceoflist(String word1) {
        String[] arr = new String[]{"open", "yes", "No", "cancel", "ignore" ,"Exit","dawnload" , "close" ,"process"};

        Map<String, Integer> map = new HashMap<>();
        int len1 = word1.length();
        int[][] dp = new int[len1 + 1][arr.length];
        for (String word : arr) {
            int mini =  minDistance(word1, word);
            map.put(word, mini);
        }
        String rightword = getMinKey(map, arr);
        return rightword;

    }

    public static int minDistance(String word1, String word2) {
        int len1 = word1.length();
        int len2 = word2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }
        for (int i = 0; i < len1; i++) {
            char c1 = word1.charAt(i);
            for (int j = 0; j < len2; j++) {
                char c2 = word2.charAt(j);
                if (c1 == c2) {
                    dp[i + 1][j + 1] = dp[i][j];
                } else {
                    int replace = dp[i][j] + 1;
                    int insert = dp[i][j + 1] + 1;
                    int delete = dp[i + 1][j] + 1;

                    int min = replace > insert ? insert : replace;
                    min = delete > min ? min : delete;
                    dp[i + 1][j + 1] = min;
                }
            }
        }
        return dp[len1][len2];
    }
    public static String getMinKey(Map<String, Integer> map, String... keys) {
        String minKey = null;
        int minValue = Integer.MAX_VALUE;
        for (String key : keys) {
            int value = map.get(key);
            if (value < minValue) {
                minValue = value;
                minKey = key;

                if (minValue >3){
                    minKey = "cant understand you ";
                }
            }
        }
        return minKey;
    }

    public static String MToast(String speechresult){
        String textToast = null;
        if(speechresult.contains("open"))
        {
            textToast  = "you want say open ";
        }
        else if (speechresult.contains("yes"))
        {
            textToast = " you want say yes  ";
        }
        else if (speechresult.contains("No"))
        {
            textToast = " you want say No ";
        }
        else if (speechresult.contains("cancel"))
        {
            textToast = "You want say cancel ";
        }
        else if (speechresult.contains("ignore"))
        {
            textToast = "You want say ignore ";
        }
        else if (speechresult.contains("Exit"))
        {
            textToast = "You want say Exit ";
        }
        else if (speechresult.contains("dawnload"))
        {
            textToast = "You want say dawnload ";
        }
        else if (speechresult.contains("process"))
        {
            textToast = "You want say process ";
        }
        else if (speechresult.contains("close"))
        {
            textToast = "You want say close ";
        }
        else if (speechresult.contains("cant understand you"))
        {
            textToast = "Please say again";
        }
        return  textToast;
    }
    public  String getresult(String word){
        String getresults;
        if (word.contains(" ")) {
            getresults = "please say one word ";

        } else {

            String correct = minDistanceoflist(word);
            String message = MToast(correct);
            getresults = correct + "$$" + message ;
           }
        return getresults;

    }

    }


