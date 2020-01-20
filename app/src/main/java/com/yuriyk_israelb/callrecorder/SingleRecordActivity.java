package com.yuriyk_israelb.callrecorder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import static com.yuriyk_israelb.callrecorder.MainActivity.contactsDB;



public class SingleRecordActivity extends AppCompatActivity {

    private TextView recordName, recordDateAndTime, recordPositionTv, recordLengthTv;
    private EditText recordRemark;
    private Button bt_start_pause, add_edit_save_btn;
    private SeekBar seekBar;
    private MediaPlayer mp;
    private boolean isPlaying;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_record);

        recordName = findViewById(R.id.recordName);
        recordDateAndTime = findViewById(R.id.dateaAndTime);
        recordPositionTv = findViewById(R.id.recordLength);
        recordLengthTv = findViewById(R.id.duration);
        add_edit_save_btn = findViewById(R.id.editsave);
        seekBar = findViewById(R.id.seekBar);
        recordRemark = findViewById(R.id.remark);
        recordRemark.setEnabled(false);
        bt_start_pause = (Button) findViewById(R.id.play_pause_button);
        isPlaying = false;

        String name = "", recordLength = "", dateAndTime = "", recordRef = "", remark = "";

        //clicked record id(date and time) sent from RecordsActivity by intent
        id = getIntent().getStringExtra("_id");

        Cursor c = contactsDB.rawQuery("SELECT * FROM records WHERE _id = '" + id + "';", null);

        if(c.moveToFirst()) {
            dateAndTime = c.getString(c.getColumnIndexOrThrow("_id"));
            name = c.getString(c.getColumnIndexOrThrow("name"));
            recordLength = c.getString(c.getColumnIndexOrThrow("record_length"));
            recordRef = c.getString(c.getColumnIndexOrThrow("record_ref"));
            remark = c.getString(c.getColumnIndexOrThrow("remark"));
            if(remark.equals(""))
            {
                remark = "No Remark for this Record";
                add_edit_save_btn.setText("Add Remark");
            }
            else
            {
                add_edit_save_btn.setText("Edit Remark");
            }
        }
        //getting the right record from raw folder
        int resID = getResources().getIdentifier(recordRef, "raw", getPackageName());

        mp = MediaPlayer.create(this, resID);

        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                seekBar.setMax(mp.getDuration());
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    mp.seekTo(progress);
                    recordPositionTv.setText(getDuration(mp.getCurrentPosition()/1000));
                }

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        setTitle(name + "'s Record");
        recordName.setText(name);
        recordDateAndTime.setText(dateAndTime);
        recordPositionTv.setText("0:00");
        recordLengthTv.setText(getDuration(mp.getDuration()/1000));
        recordRemark.setBackgroundColor(Color.LTGRAY);
        recordRemark.setText(remark);
        add_edit_save_btn.setOnClickListener(new Listener());
        bt_start_pause.setOnClickListener(new Listener());
    }


    //get the duration in time shape - 0:00
    private String getDuration(int duration) {
        int hour = duration/360;
        int min = duration/60;
        int sec = duration%60;
        String str_dur = "";
        if(hour > 0)
            str_dur += Integer.toString(hour) + ":";
        str_dur += Integer.toString(min) + ":";
        if(sec < 10)
            str_dur += "0" + Integer.toString(sec);
        else
            str_dur += Integer.toString(sec);
        return str_dur;
    }

    //change the seek bar according to audio progress
    private void changeSeekBar() {
        seekBar.setProgress(mp.getCurrentPosition());

        if(mp.isPlaying()){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(isPlaying)
                    {
                        if(seekBar.getProgress() == mp.getDuration())//audio is done
                        {
                            isPlaying = false;
                            seekBar.setProgress(0);
                            bt_start_pause.setBackgroundResource(R.drawable.play);
                            mp.seekTo(0);
                            return;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recordPositionTv.setText(getDuration(mp.getCurrentPosition()/1000));
                                seekBar.setProgress(mp.getCurrentPosition());
                            }
                        });
                        SystemClock.sleep(1000); // wait 1 sec
                    }
                }
            }).start();
        }
    }


    public class Listener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            //  play / pause Button for the record audio
            if(v.getId()== R.id.play_pause_button) {
                if(isPlaying && mp.isPlaying())
                {
                    Log.d("debug", "pause");
                    bt_start_pause.setBackgroundResource(R.drawable.play);
//                    mp.reset();
                    mp.pause();
                    isPlaying = false;
                }
                else
                {
                    bt_start_pause.setBackgroundResource(R.drawable.pause);
                    mp.start();
                    changeSeekBar();
                    isPlaying = true;
                }
            }
            //  add / adit / save Button for Remark
            else if(v.getId()== R.id.editsave)
            {
                Button b = (Button)v;
                //  add / edit case
                if(b.getText().toString().equals("Add Remark") || b.getText().toString().equals("Edit"))
                {
                    if(b.getText().toString().equals("Add Remark"))
                        recordRemark.setText("");
                    recordRemark.setEnabled(true);
                    recordRemark.requestFocus();
                    b.setText("Save");
                }
                //save case
                else
                {
                    if(recordRemark.getText().toString().equals("")) {
                        recordRemark.setText("No Remark for this Record");
                        b.setText("Add Remark");
                    }
                    else {
                        b.setText("Edit");
                        String recordremark = recordRemark.getText().toString();
                        String sql = "UPDATE records SET remark = '" + recordremark + "' WHERE _id = '" + id + "';";
                        contactsDB.execSQL(sql);
                    }
                    recordRemark.setEnabled(false);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mp.stop();
    }


    //----------Dialog Menu for delet record----------
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        MenuItem menuDelete = menu.add("Delete Record");

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
        alertDialog.setTitle("Delete Record");
        alertDialog.setMessage("Do you really want to delete this Record?");
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                contactsDB.delete("records", "_id = ?", new String[] {id} );
                dialog.cancel();
                Intent intent = new Intent(getApplicationContext(), RecordsActivity.class);
                finish();
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }
}