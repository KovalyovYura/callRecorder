package com.yuriyk_israelb.callrecorder;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.icu.text.AlphabeticIndex;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;

import java.util.ArrayList;
import java.util.HashMap;

import static com.yuriyk_israelb.callrecorder.MainActivity.contactsDB;

public class RecordsActivity extends AppCompatActivity {
    private ListView listViewAll, listViewIn, listViewOut;
    private ArrayList<String> names_all, names_in, names_out;
    private Cursor cursor_all, cursor_in, cursor_out;
    private RecordDetailsAdapter adapter_all, adapter_in, adapter_out;
    private SearchView search;
//    private boolean its_just_created = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
//        its_just_created = true;
        setTitle("Records");
        createTabs();

        listViewAll = findViewById(R.id.listview1);
        listViewIn = findViewById(R.id.listview2);
        listViewOut = findViewById(R.id.listview3);

        names_all = new ArrayList<String>();
        names_in = new ArrayList<String>();
        names_out = new ArrayList<String>();

        search = findViewById(R.id.search);

        // Create an AndroidFlavorAdapter, whose data source is a list of AndroidFlavors.
        // The adapter knows how to create list item views for each item in the list.
        cursor_all = contactsDB.rawQuery("SELECT  * FROM records ORDER BY _id", null);
        adapter_all = new RecordDetailsAdapter(this, cursor_all);
        if(cursor_all.moveToFirst())
        {
            do {
                names_all.add(cursor_all.getString(cursor_all.getColumnIndex("name")));
            }
            while(cursor_all.moveToNext());
        }

        cursor_in = contactsDB.rawQuery("SELECT  * FROM records WHERE in_out = '1' ORDER BY _id ", null);
        adapter_in = new RecordDetailsAdapter(this, cursor_in);
        if(cursor_in.moveToFirst())
        {
            do {
                names_in.add(cursor_in.getString(cursor_in.getColumnIndex("name")));
            }
            while(cursor_in.moveToNext());
        }

        cursor_out = contactsDB.rawQuery("SELECT  * FROM records WHERE in_out = '0' ORDER BY _id", null);
        adapter_out = new RecordDetailsAdapter(this, cursor_out);
        if(cursor_out.moveToFirst())
        {
            do {
                names_out.add(cursor_out.getString(cursor_out.getColumnIndex("name")));
            }
            while(cursor_out.moveToNext());
        }

        listViewAll.setAdapter(adapter_all);
        listViewIn.setAdapter(adapter_in);
        listViewOut.setAdapter(adapter_out);

        listViewAll.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                openRecordActivity(cursor_all.getString(cursor_all.getColumnIndexOrThrow( "name")), cursor_all.getString(cursor_all.getColumnIndexOrThrow( "_id")));
            }
        });

        listViewIn.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                openRecordActivity(cursor_in.getString(cursor_in.getColumnIndexOrThrow( "name")), cursor_in.getString(cursor_in.getColumnIndexOrThrow( "_id")));
            }
        });

        listViewOut.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                openRecordActivity(cursor_out.getString(cursor_out.getColumnIndexOrThrow( "name")), cursor_out.getString(cursor_out.getColumnIndexOrThrow( "_id")));
            }
        });

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(!s.equals(""))
                {
                    cursor_all = contactsDB.rawQuery("SELECT  * FROM records WHERE name LIKE '%" + s + "%' ORDER BY _id", null);
                    changeCursor(adapter_all, cursor_all);

                    cursor_in = contactsDB.rawQuery("SELECT  * FROM records WHERE in_out = '1' AND name LIKE '%" + s + "%' ORDER BY _id", null);
                    changeCursor(adapter_in, cursor_in);

                    cursor_out = contactsDB.rawQuery("SELECT  * FROM records WHERE in_out = '0' AND name LIKE '%" + s + "%' ORDER BY _id", null);
                    changeCursor(adapter_out, cursor_out);
                }
                else
                {
                    cursor_all = contactsDB.rawQuery("SELECT  * FROM records ORDER BY _id", null);
                    changeCursor(adapter_all, cursor_all);

                    cursor_in = contactsDB.rawQuery("SELECT  * FROM records WHERE in_out = '1' ORDER BY _id", null);
                    changeCursor(adapter_in, cursor_in);

                    cursor_out = contactsDB.rawQuery("SELECT  * FROM records WHERE in_out = '0' ORDER BY _id", null);
                    changeCursor(adapter_out, cursor_out);
                }
                return true;
            }
        });

    }

    public RecordDetailsAdapter changeCursor(RecordDetailsAdapter adapter, Cursor cursor)
    {
        adapter.changeCursor(cursor);
        return adapter;
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if(!its_just_created)
//        {
//            Intent intent = getIntent();
//            finish();
//            startActivity(intent);
//        }
//        its_just_created = false;
//    }

    public void openRecordActivity(String name, String id)
    {
        Intent intent = new Intent(this, SingleRecordActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("_id", id);
        startActivity(intent);
    }

    public void createTabs()
    {
        TabHost tabs = (TabHost) findViewById(R.id.tabhost);
        tabs.setup();
        TabHost.TabSpec spec = tabs.newTabSpec("tag1");
        spec.setContent(R.id.tab1);
        spec.setIndicator("ALL");
        tabs.addTab(spec);
        spec = tabs.newTabSpec("tag2");
        spec.setContent(R.id.tab2);
        spec.setIndicator("INCOME");
        tabs.addTab(spec);
        spec = tabs.newTabSpec("tag3");
        spec.setContent(R.id.tab3);
        spec.setIndicator("OUTCOME");
        tabs.addTab(spec);
    }
}