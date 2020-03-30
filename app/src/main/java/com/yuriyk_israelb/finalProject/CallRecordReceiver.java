package com.yuriyk_israelb.finalProject;

import android.app.Notification;
import android.app.PendingIntent;
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

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.yuriyk_israelb.finalProject.CallRecordService.CHANNEL_ID;
import static com.yuriyk_israelb.finalProject.CallRecordService.notificationId;
import static com.yuriyk_israelb.finalProject.CallRecordService.notificationManager;

public class CallRecordReceiver extends BroadcastReceiver {
        private Bundle bundle;
        private String state;
        private MediaRecorder recorder;
        private boolean inOutCall = false;
        private File audiofile;
        private boolean recordStarted = false;
        private String name, phoneNumber, outOrIn, recordPath, time;
        private boolean wasRinging = false;
        private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
        private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
        private SharedPreferences record;



        @Override
        public void onReceive(Context context, Intent intent) {
            // get Shared Preference for check if enable to record
            record = context.getSharedPreferences("Records", Context.MODE_PRIVATE);
            if (intent.getAction().equals(ACTION_OUT)) {
                if ((bundle = intent.getExtras()) != null) {
                    inOutCall = true;
                    phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                    outOrIn = "0";
                    name = getName(phoneNumber, context);

                    if(record.getBoolean(name, false)) {
                        startRecord();
                    }
                }
            }
            else if (intent.getAction().equals(ACTION_IN)) {

                if ((bundle = intent.getExtras()) != null) {
                    state = bundle.getString(TelephonyManager.EXTRA_STATE);
                    if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        phoneNumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        name = getName(phoneNumber, context);
                        wasRinging = true;
                    }
                    else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                        if (wasRinging && !recordStarted) {
                            outOrIn = "1";
                            if(record.getBoolean(name, false))
                                startRecord();
                        }
                    }
                    else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        wasRinging = false;
                        if(inOutCall && recordStarted){
                            recorder.stop();
                            recordPath = audiofile.getAbsolutePath();
                            inOutCall = false;
                            recordStarted = false;
                            setToDB();
                            showNotification("Outgoing Call Record Saved",
                                    "With "+ name +" at time "+time+"\n" +
                                            "saved successfully, " +
                                            "click to open record",context, name, time);
                        }
                        if (!inOutCall && recordStarted) {
                            Log.d("receiver", "IN END");
                            recorder.stop();
                            recordPath = audiofile.getAbsolutePath();
                            recordStarted = false;
                            setToDB();
                            showNotification("Incoming Call Record Saved",
                                    "With "+ name +" at time "+time+"\n" +
                                            "saved successfully, " +
                                            "click to open record",context, name, time);
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
                    "VALUES('" + name +"','" + phoneNumber + "','" + outOrIn +"','"+ recordPath +"','"+ time +"','');";
            MainActivity.contactsDB.execSQL(sql);
        }

        private void startRecord(){
            time = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
            String timeForPath = new SimpleDateFormat("dd-MM-yyyy hh-mm-ss").format(new Date());
            File sampleDir = new File(Environment.getExternalStorageDirectory(), "/CallRecordingData");
            if (!sampleDir.exists()) {
                sampleDir.mkdirs();
            }
            try {
                audiofile = File.createTempFile("Record "+ timeForPath, ".amr", sampleDir);
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

    public void showNotification(String notificationTitle, String notificationText,
                                 Context context, String name, String time)
    {
        Log.d("receiver", "in showNotification");
        Intent intent = new Intent(context, RecordsActivity.class);
//        intent.putExtra("name", name);
//        intent.putExtra("_id", time);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,0);

        // Build Notification with NotificationCompat.Builder
        // on Build.VERSION < Oreo the notification avoid the CHANEL_ID
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)         //Set the icon
                .setContentTitle(notificationTitle)         //Set the title of notification
                .setContentText(notificationText)           //Set the text for notification
                .setContentIntent(pendingIntent)            // Starts Intent when notification clicked
                //.setOngoing(true)                         // stick notification
                .setAutoCancel(true)                        // close notification when clicked
                .build();

        // Send the notification to the device Status bar.
        notificationManager.notify(notificationId, notification);

        notificationId++;  // for multiple(grouping) notifications on the same chanel
        Log.d("receiver", Integer.toString(notificationId));
    }
}
