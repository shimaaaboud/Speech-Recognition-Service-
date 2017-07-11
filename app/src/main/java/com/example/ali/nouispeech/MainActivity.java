package com.example.ali.nouispeech;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private int mBindFlag;
    private Messenger mServiceMessenger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent service = new Intent(getApplicationContext(), myservice.class);
        getApplicationContext().startService(service);
        mBindFlag = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ? 0 : Context.BIND_ABOVE_CLIENT;


    }
    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, myservice.class), mServiceConnection, mBindFlag);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mServiceMessenger != null)
        {
            unbindService(mServiceConnection);
            mServiceMessenger = null;
        }
    }
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceMessenger = new Messenger(service);
            Message msg = new Message();
            msg.what = myservice.MSG_RECOGNIZER_START_LISTENING;

            try
            {
                mServiceMessenger.send(msg);
            }
            catch (RemoteException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceMessenger = null;

        }
    };
}
