package com.yuriyk_israelb.callrecorder;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class CallRecordService extends Service {

    private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
    private CallRecordReceiver callReceiver;




    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(callReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("service", "in onStartCommand");

        // final String terminate =(String)
        // intent.getExtras().get("terminate");//
        // intent.getStringExtra("terminate");
        // Log.d("TAG", "service started");
        //
        // TelephonyManager telephony = (TelephonyManager)
        // getSystemService(Context.TELEPHONY_SERVICE); // TelephonyManager
        // // object
        // CustomPhoneStateListener customPhoneListener = new
        // CustomPhoneStateListener();
        // telephony.listen(customPhoneListener,
        // PhoneStateListener.LISTEN_CALL_STATE);
        // context = getApplicationContext();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OUT);
        filter.addAction(ACTION_IN);
        this.callReceiver = new CallRecordReceiver();
        this.registerReceiver(this.callReceiver, filter);

        // if(terminate != null) {
        // stopSelf();
        // }
        return START_NOT_STICKY;
    }

}
