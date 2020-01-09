package com.yuriyk_israelb.callrecorder;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    public static final String DATABASE_NAME = "contacts.db";
    public static SQLiteDatabase contactsDB = null;
    private ListView lvContactsList;
    private SearchView searchView;
    private ArrayList<Contact> contactsList;
    private ContactAdapter contactAdapter;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvContactsList = findViewById(R.id.lvContactsListID);
        searchView = findViewById(R.id.searchViewID);
        contactsList = new ArrayList<>();

        // Open Database or create if isn't exist and create contact table
        try
        {
            contactsDB = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
            String sql = "CREATE TABLE IF NOT EXISTS records(name VARCHAR, phone_number VARCHAR, record_length VARCHAR" +
                    ", in_out boolean, record_ref VARCHAR, _id VARCHAR primary key);";
            contactsDB.execSQL(sql);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "DataBase ERROR", Toast.LENGTH_LONG).show();
        }

        String sql = "REPLACE INTO records(name, phone_number, record_length, in_out, record_ref, _id) " +
                "VALUES('yura', '0543980555', '12:51', 1, 'ahoti', '12.12.12 22:22')," +
                "('israel', '0543980555', '12:51', 1, 'all_or_nothing', '16.12.12 22:22')," +
                "('sasha', '0543980555', '12:51', 1, '" +
                "', '15.12.12 22:22')," +
                "('shmuel', '0543980555', '12:51', 1, 'blublu', '14.12.12 22:22')," +
                "('eli', '0543980555', '12:51', 1, 'blublu', '13.12.12 22:22');";

        contactsDB.execSQL(sql);

        Cursor c = contactsDB.rawQuery("SELECT * FROM records;", null);

        if(c.moveToFirst()) {
            do {
                String id = c.getString(c.getColumnIndexOrThrow("_id"));
                String name = c.getString(c.getColumnIndexOrThrow( "name"));
                Log.d("debug", name +" "+ id);

            }while (c.moveToNext());
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    111);
        }
        else {
            initContactList();
            contactAdapter = new ContactAdapter(this, contactsList);
            lvContactsList.setAdapter(contactAdapter);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if(contactsList.contains(query)){
                        contactAdapter.getFilter().filter(query);
                    }else{
                        Toast.makeText(MainActivity.this, "No Match found",Toast.LENGTH_LONG).show();
                    }
                    return false;
                }

                @Override
                    public boolean onQueryTextChange(String newText) {
                    //contactAdapter.getFilter().filter(newText);
                    return false;
                }
            });
        }

        btn = findViewById(R.id.btnGoToRecordsID);
        btn.setOnClickListener(new Listener());
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


    @Override
    protected void onResume() {
        super.onResume();
        initContactList();
    }

    public void initContactList() {
        contactsList.clear();
        ContentResolver resolver = getContentResolver();
        Uri contactsTableUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = resolver.query(contactsTableUri, null, null, null, null);

        if (cursor != null) {
            if (cursor.moveToNext()) {
                // there is at least ONE contact
                do {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    int photoResourceId = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_FILE_ID));
                    Uri displayPhotoUri = ContentUris.withAppendedId(ContactsContract.DisplayPhoto.CONTENT_URI, photoResourceId);
                    Contact contact = new Contact(name, photoResourceId, displayPhotoUri);
                    contactsList.add(contact);
                }
                while (cursor.moveToNext());

                cursor.close();
            }
        }
    }






    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
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
        return true;
    }

    private void showAboutDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//        alertDialog.setIcon(R.drawable.document);
        alertDialog.setTitle("About Puzzle 15");
        alertDialog.setMessage("This game implements the Game Of Fifteen\n\nBy YURIY KOVALYOV & ISRAEL BEN MENACHEM (c)");
        alertDialog.show();
    }

    private void showExitDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//        alertDialog.setIcon(R.drawable.document);
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
















    // Check Runtime Permission for READ_CONTACTS
    public boolean isPermissionToReadContactsOK()
    {
        // check if permission for READ_CONTACTS is granted ?
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else
        {
            // show requestPermissions dialog
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 111);
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
                return true;
            finish();
        }
        return true;
    }

    public void showCenteredToast(String msg)
    {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}