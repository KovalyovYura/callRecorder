package com.yuriyk_israelb.callrecorder;

import androidx.appcompat.app.AppCompatActivity;


import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {
    public static final String DATABASE_NAME = "contacts.db";
    public static SQLiteDatabase contactsDB = null;
    private ListView lvContactsList;
    private EditText edtSearch;
    private Cursor cursor;
    private ContactAdapter contactAdapter;
    private Button btn;
    private SharedPreferences sp_records;
    private SharedPreferences sp_blocks;
    private boolean isStart = true;
    private static final int REQUEST_CODE = 0;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isStart = false;

        // open SP for saving record and block settings per contact
        sp_records = getSharedPreferences("Records", MODE_PRIVATE);
        sp_blocks = getSharedPreferences("Blocks", MODE_PRIVATE);

        lvContactsList = findViewById(R.id.lvContactsListID);
        edtSearch = findViewById(R.id.edtSearchID);
        btn = findViewById(R.id.btnGoToRecordsID);

        try {
            // Initiate DevicePolicyManager.
            mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            mAdminName = new ComponentName(this, DeviceAdminDemo.class);
            if (!mDPM.isAdminActive(mAdminName)) {
                Log.d("debug", "inside if");
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Click on Activate button to secure your application.");
                startActivityForResult(intent, REQUEST_CODE);
            } else {
                Log.d("debug", "inside else");
                startService(new Intent(this, CallRecordService.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Open Database or create if isn't exist and create contact table
        try
        {
            contactsDB = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
            String sql = "CREATE TABLE IF NOT EXISTS records(name VARCHAR, phone_number VARCHAR" +
                    ", in_out boolean, record_ref VARCHAR, _id VARCHAR primary key, remark VARCHAR);";
            contactsDB.execSQL(sql);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "DataBase ERROR", Toast.LENGTH_LONG).show();
        }

//        String sql = "REPLACE INTO records(name, phone_number, record_length, in_out, record_ref, _id) " +
//                "VALUES('yura', '0543980555', '12:51', 1, 'ahoti', '12.12.12 22:22')," +
//                "('israel', '0543980555', '12:51', 1, 'all_or_nothing', '16.12.12 22:22')," +
//                "('sasha', '0543980555', '12:51', 1, 'sound_got', '15.12.12 22:22')," +
//                "('shmuel', '0543980555', '12:51', 1, 'blublu', '14.12.12 22:22')," +
//                "('eli', '0543980555', '12:51', 1, 'blublu', '13.12.12 22:22');";
//
//        contactsDB.execSQL(sql);

        //TODO: ask for all premissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    111);
        }
        else {
            cursor = getCursor();
            contactAdapter = new ContactAdapter(this, cursor);
            lvContactsList.setAdapter(contactAdapter);

            lvContactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    LinearLayout linearLayoutParent = (LinearLayout) view;
                    LinearLayout nameAndPhone = (LinearLayout) linearLayoutParent.getChildAt(1);
                    TextView txtName = (TextView)nameAndPhone.getChildAt(0);
                    String contactName = txtName.getText().toString();
                    showContactSettingDialog(contactName);
                }
            });

            edtSearch.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                    cursor = getSearchCursor(s.toString());
                    contactAdapter.changeCursor(cursor);
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                }
            });
        }

        btn.setOnClickListener(new Listener());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (REQUEST_CODE == requestCode) {
            Intent intent = new Intent(MainActivity.this, CallRecordService.class);
            startService(intent);
        }
    }

    public class Listener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            open_game_activity();
        }
    }

    public void open_game_activity()
    {
        Intent intent = new Intent(this, RecordsActivity.class);
        startActivity(intent);
    }

    public Cursor getCursor(){
        ContentResolver resolver = getContentResolver();
        Uri contactsTableUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        return resolver.query(contactsTableUri, null, null, null, null);
    }

    public Cursor getSearchCursor(String query){
        ContentResolver resolver = getContentResolver();
        Uri contactsTableUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?";
        String[] selectionArgs = {"%"+query+"%"};
        return resolver.query(contactsTableUri, null, selection, selectionArgs, null);
    }


    @Override
    protected void onPause() {
        Log.d("debug", "i'm in onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("debug", "i'm in onStop");
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isStart){
            cursor = getCursor();
            contactAdapter.changeCursor(cursor);
        }
    }

    @Override
    protected void onStart() {
        Log.d("debug", "i'm in onStart");
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        Log.d("debug", "i'm in onDestroy");
        stopService(new Intent(this, CallRecordService.class));
        super.onDestroy();
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        MenuItem menuAbout = menu.add("About");
        MenuItem menuExit = menu.add("Exit");
        MenuItem menuUnknownRecord = menu.add("Unknown contacts");

        menuAbout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                showAboutDialog();
                return true;
            }
        });

        menuExit.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                showExitDialog();
                return true;
            }
        });

        menuUnknownRecord.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                unknownRecord();
                return true;
            }
        });

        return true;
    }

    private void showAboutDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//        alertDialog.setIcon(R.drawable.document);
        alertDialog.setTitle("CallRecorder");
        alertDialog.setMessage("This application record phone calls\n\nBy YURIY KOVALYOV & ISRAEL BEN MENACHEM (c)");
        alertDialog.show();
    }

    private void showExitDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Exit App");
        alertDialog.setMessage("Do you really want to exit?");
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                finish();  // destroy this activity
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        alertDialog.show();
    }

    private void unknownRecord()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Enable/Disable recording of unknown contacts");
        alertDialog.setMessage("Do you want to record calls from unknown contacts?");
        alertDialog.setCancelable(false);
        final SharedPreferences.Editor editor = sp_records.edit();
        alertDialog.setPositiveButton("Enable", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                  editor.putBoolean("Unknown", true);
                  editor.apply();
            }
        });
        alertDialog.setNegativeButton("Disable", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editor.remove("Unknown");
                editor.apply();
            }
        });
        alertDialog.show();
    }



    private void showContactSettingDialog(final String contactName){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Contact Settings");
        alertDialog.setIcon(R.drawable.settings_icon);
        alertDialog.setCancelable(false);
        boolean[] selected = {sp_records.getBoolean(contactName, false), sp_blocks.getBoolean(contactName, false)};
        final SharedPreferences.Editor editor_blocks = sp_blocks.edit();
        final SharedPreferences.Editor editor_records = sp_records.edit();
        alertDialog.setMultiChoiceItems(R.array.contacts_setting, selected, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    Log.d("debug", "i'm checked");
                    // If the user checked the item, add it to the selected items
                    if(which == 1)
                        editor_blocks.putBoolean(contactName, true);
                    else
                        editor_records.putBoolean(contactName, true);
                }
                else {
                    if(which == 1)
                        editor_blocks.remove(contactName);
                    else
                        editor_records.remove(contactName);
                }
            }
        });
        alertDialog.setPositiveButton("Save", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                editor_blocks.apply();
                editor_records.apply();
                cursor = getCursor();
                contactAdapter.changeCursor(cursor);
            }
        });
        alertDialog.setNegativeButton("Close", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.show();
    }



}