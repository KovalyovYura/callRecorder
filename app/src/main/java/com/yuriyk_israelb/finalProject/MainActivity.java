package com.yuriyk_israelb.finalProject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {
    public static final String DATABASE_NAME = "contacts.db";
    public static SQLiteDatabase contactsDB = null;
    private ListView lvContactsList;
    private SearchView searchView;
    private Cursor cursor;
    private ContactAdapter contactAdapter;
    private Button btn;
    private SharedPreferences sp_records;
    private boolean isStart = true;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // open SP for saving record and block settings per contact
        sp_records = getSharedPreferences("Records", MODE_PRIVATE);

        lvContactsList = findViewById(R.id.lvContactsListID);
        searchView = findViewById(R.id.searchContactsID);
        btn = findViewById(R.id.btnGoToRecordsID);

        // Open Database or create if isn't exist and create contact table
        try {
            contactsDB = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
            String sql = "CREATE TABLE IF NOT EXISTS records(name VARCHAR, phone_number VARCHAR" +
                    ", in_out boolean, record_ref VARCHAR, _id VARCHAR primary key, remark VARCHAR);";
            contactsDB.execSQL(sql);
        } catch (Exception e) {
            Toast.makeText(this, "DataBase ERROR", Toast.LENGTH_LONG).show();
        }

        while (!checkAllPermissons()) {
            requestAllPermissions();
        }

        cursor = getCursor();
        contactAdapter = new ContactAdapter(this, cursor);
        lvContactsList.setAdapter(contactAdapter);
        btn.setOnClickListener(new Listener());

        lvContactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) { LinearLayout linearLayoutParent = (LinearLayout) view;
                        LinearLayout nameAndPhone = (LinearLayout) linearLayoutParent.getChildAt(1);
                        TextView txtName = (TextView) nameAndPhone.getChildAt(0);
                        String contactName = txtName.getText().toString();
                        showContactSettingDialog(contactName);
                        }
                    });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                cursor = getSearchCursor(s);
                contactAdapter.changeCursor(cursor);
                return true;
            }
        });
        startForegroundService(new Intent(this, CallRecordService.class));

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
        return resolver.query(contactsTableUri, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");
    }

    public Cursor getSearchCursor(String query){
        ContentResolver resolver = getContentResolver();
        Uri contactsTableUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?";
        String[] selectionArgs = {"%"+query+"%"};
        return resolver.query(contactsTableUri, null, selection, selectionArgs, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(!isStart){
            cursor = getCursor();
            contactAdapter.changeCursor(cursor);
        }
        isStart = false;
    }


    @Override
    protected void onDestroy() {
        stopService(new Intent(this, CallRecordService.class));
        super.onDestroy();
    }


    public boolean checkAllPermissons(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
            return false;
        else
            return true;
    }

    public void requestAllPermissions(){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_PHONE_STATE},
                    111);
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Checking whether user granted the permission or not.
        for(int i=0; i<grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                showAppSettings();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void showAppSettings()
    {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }




    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        MenuItem menuUnknownRecord = menu.add("Record Unknown Contacts");
        MenuItem menuRecordAllContacts = menu.add("Record All Contacts");
        MenuItem menuAbout = menu.add("About");
        MenuItem menuExit = menu.add("Exit");
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

        menuRecordAllContacts.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                allContactsRecord();
                return true;
            }
        });



        return true;
    }



    private void showAboutDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(R.drawable.about_icon);
        alertDialog.setTitle("CallRecorder");
        alertDialog.setMessage("This application record phone calls\n\nBy YURIY KOVALYOV & ISRAEL BEN MENACHEM (c)");
        alertDialog.show();
    }

    private void showExitDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Exit App");
        alertDialog.setIcon(R.drawable.exit_icon);
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
        String str = "";
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Enable/Disable recording of unknown contacts");
        alertDialog.setCancelable(false);
        final SharedPreferences.Editor editor = sp_records.edit();
        if (sp_records.getBoolean("Unknown", false)) {
            alertDialog.setPositiveButton("Disable", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    editor.remove("Unknown");
                    editor.apply();
                }
            });
            str = "Disable";
            alertDialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) { }
            });
        } else {
            alertDialog.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    editor.putBoolean("Unknown", true);
                    editor.apply();
                }
            });
            str = "Enable";
            alertDialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });
        }
        alertDialog.setMessage("Do you want " + str + " record calls from unknown contacts?");
        alertDialog.show();
    }

    private void allContactsRecord()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Enable/Disable recording of all contacts");
        alertDialog.setMessage("Do you want to record calls from all contacts?");
        alertDialog.setCancelable(false);
        final SharedPreferences.Editor editor = sp_records.edit();
        cursor = getCursor();
        alertDialog.setPositiveButton("Enable", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if(cursor.moveToFirst()){
                    do{
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        editor.putBoolean(name, true);
                        editor.apply();
                    }while(cursor.moveToNext());
                    contactAdapter.changeCursor(getCursor());
                }

            }
        });
        alertDialog.setNegativeButton("Disable", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(cursor.moveToFirst()){
                    do{
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        editor.remove(name);
                        editor.apply();
                    }while(cursor.moveToNext());
                    contactAdapter.changeCursor(getCursor());
                }
            }
        });
        alertDialog.show();
    }



    private void showContactSettingDialog(final String contactName){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Contact Settings");
        alertDialog.setIcon(R.drawable.settings_icon);
        alertDialog.setCancelable(false);
        boolean[] selected = {sp_records.getBoolean(contactName, false)};
        final SharedPreferences.Editor editor_records = sp_records.edit();
        alertDialog.setMultiChoiceItems(R.array.contacts_setting, selected, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked)
                    editor_records.putBoolean(contactName, true);

                else
                    editor_records.remove(contactName);
            }
        });
        alertDialog.setPositiveButton("Save", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
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