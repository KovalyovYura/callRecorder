package com.yuriyk_israelb.callrecorder;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

public class CallRecordReceiver extends BroadcastReceiver {
        private Bundle bundle;
        private String state;
        private MediaRecorder recorder;
        private boolean inOutCall = false;
        private File audiofile;
        private boolean recordStarted = false;
        private Timer timer;
        private String name, phoneNumber, outOrIn, recordPath, time;
        private boolean wasRinging = false;
        private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
        private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
        private SharedPreferences record;
        private SharedPreferences block;



        @Override
        public void onReceive(Context context, Intent intent) {
            record = context.getSharedPreferences("Records", Context.MODE_PRIVATE);
            block = context.getSharedPreferences("Blocks", Context.MODE_PRIVATE);

            if (intent.getAction().equals(ACTION_OUT)) {
                if ((bundle = intent.getExtras()) != null) {
                    inOutCall = true;
                    phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    outOrIn = "out";
                    name = getName(phoneNumber, context);
                    Log.d("receiver", "OUT CALL "+ phoneNumber);
                    if(record.getBoolean(name, false))
                        startRecord();

                }
            }
            else if (intent.getAction().equals(ACTION_IN)) {

                if ((bundle = intent.getExtras()) != null) {
                    state = bundle.getString(TelephonyManager.EXTRA_STATE);
                    if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        phoneNumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        name = getName(phoneNumber, context);
                        wasRinging = true;
                        Log.d("receiver", "IN :" + phoneNumber);
                    }
                    else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                        if (wasRinging && !recordStarted) {

                            Log.d("receiver", "ANSWERED");
                            if(record.getBoolean(name, false))
                                startRecord();
                        }
                    }
                    else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        Log.d("receiver", "i'm in IDLE");
                        if(inOutCall && recordStarted){
                            Log.d("receiver", "OUT END");
                            recorder.stop();
                            recordPath = audiofile.getAbsolutePath();
                            inOutCall = false;
                            recordStarted = false;
                            setToDB();
                        }
                        wasRinging = false;
                        if (recordStarted) {
                            Log.d("receiver", "IN END");
                            recorder.stop();
                            recordPath = audiofile.getAbsolutePath();
                            recordStarted = false;
                            setToDB();
                        }
                    }
                }
            }
        }


        private String getName(String phoneNumber, Context context){
            String contactName = "Unknown";
            ContentResolver resolver = context.getContentResolver();
            Uri contactsTableUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ?";
            String[] selectionArgs = {"%"+phoneNumber.substring(1)+"%"};
            Cursor cursor = resolver.query(contactsTableUri, null, selection, selectionArgs, null);
            if(cursor.moveToFirst()){
                contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            }

            return contactName;
        }


        private void setToDB(){
            String sql = "INSERT INTO records(name, phone_number, in_out, record_ref, _id, remark) " +
                    "VALUES('" + name +"','" + phoneNumber + "','" + outOrIn +"','"+ recordPath +"','"+ time +"', '');";
            MainActivity.contactsDB.execSQL(sql);
        }

        private void startRecord(){
            time = new SimpleDateFormat("dd-MM-yyyy hh-mm-ss").format(new Date());
            File sampleDir = new File(Environment.getExternalStorageDirectory(), "/CallRecordingData");
            if (!sampleDir.exists()) {
                sampleDir.mkdirs();
            }
            try {
                audiofile = File.createTempFile("Record " + time, ".amr", sampleDir);
            } catch (IOException e) {
                e.printStackTrace();
            }

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(audiofile.getAbsolutePath());
            try {
                recorder.prepare();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            recorder.start();
            recordStarted = true;
        }
}
