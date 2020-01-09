package com.yuriyk_israelb.callrecorder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import static com.yuriyk_israelb.callrecorder.MainActivity.contactsDB;

public class SingleRecordActivity extends AppCompatActivity {

    private TextView recordName, recordDateAndTime, recordLengthTv;
    private Button bt_start_pause;
    private MediaPlayer mp;
    private boolean isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_record);

        recordName = findViewById(R.id.recordName);
        recordDateAndTime = findViewById(R.id.dateaAndTime);
        recordLengthTv = findViewById(R.id.recordLength);
        bt_start_pause = (Button) findViewById(R.id.play_pause_button);
        isPlaying = false;

        String name = "", recordLength = "", dateAndTime = "", recordRef = "";

        //clicked record id(date and time) sent from RecordsActivity by intent
        String id = getIntent().getStringExtra("_id");

        Cursor c = contactsDB.rawQuery("SELECT * FROM records;", null);

        if(c.moveToFirst()) {
            do {
                //primary key is the date and time str
                dateAndTime = c.getString(c.getColumnIndexOrThrow("_id"));
                if(dateAndTime.equals(id))
                {//pick our record's details
                    name = c.getString(c.getColumnIndexOrThrow("name"));
                    recordLength = c.getString(c.getColumnIndexOrThrow("record_length"));
                    recordRef = c.getString(c.getColumnIndexOrThrow("record_ref"));
                }
            }while (c.moveToNext());
        }
        //getting the right record from raw folder
        int resID = getResources().getIdentifier(recordRef, "raw", getPackageName());
//        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
//        metaRetriever.setDataSource(filePath);
//
//        String out = "";
//        // get mp3 info
//
//        // convert duration to minute:seconds
//        String duration =
//                metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//        Log.v("time", duration);

        mp = MediaPlayer.create(this, resID);
        Log.d("debug", "here");
        setTitle(name + "'s Record");
        recordName.setText(name);
        recordDateAndTime.setText(dateAndTime);
        recordLengthTv.setText(recordLength);
        bt_start_pause.setOnClickListener(new Listener());
    }


    public class Listener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            if(v.getId()== R.id.play_pause_button) {
                if(isPlaying)
                {
                    bt_start_pause.setBackgroundResource(R.drawable.play);
                    mp.reset();
                    mp.pause();
                    isPlaying = false;
                }
                else
                {
                    bt_start_pause.setBackgroundResource(R.drawable.pause);
                    mp.start();
                    isPlaying = true;
                }
//        Uri uri = "R.raw." + recordRef;
//        mp = MediaPlayer.create(this, uri);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mp.stop();
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        MenuItem menuDelete = menu.add("Delete Record");
        MenuItem menuRemark = menu.add("Add Remark");

        menuRemark.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                Log.d("debug", "remark");
                return true;
            }
        });

        menuDelete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                showDeleteDialog();
                return true;
            }
        });
        return true;
    }


    private void showDeleteDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//        alertDialog.setIcon(R.drawable.puzzle15);
        alertDialog.setTitle("Delete Record");
        alertDialog.setMessage("Do you really want to delete this Record?");
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Log.d("debug", "deleted");  // destroy this activity
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });
        alertDialog.show();
    }
}
