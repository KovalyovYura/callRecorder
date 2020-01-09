package com.yuriyk_israelb.callrecorder;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.icu.text.AlphabeticIndex;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;

import java.util.ArrayList;
import java.util.HashMap;

import static com.yuriyk_israelb.callrecorder.MainActivity.contactsDB;

public class RecordsActivity extends AppCompatActivity {
    private ListView listViewAll, listViewIn, listViewOut;
    //private ArrayList<RecordDetails> arrayListAll, arrayListIn, arrayListOut;
    private Cursor cursor;
    private RecordDetailsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        setTitle("Records");
        createTabs();

        listViewAll = findViewById(R.id.listview1);
        listViewIn = findViewById(R.id.listview2);
        listViewOut = findViewById(R.id.listview3);


        // Create an AndroidFlavorAdapter, whose data source is a list of AndroidFlavors.
        // The adapter knows how to create list item views for each item in the list.
        cursor = contactsDB.rawQuery("SELECT  * FROM records ORDER BY _id", null);
        adapter = new RecordDetailsAdapter(this, cursor);

        listViewAll.setAdapter(adapter);
        listViewIn.setAdapter(adapter);

        listViewAll.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                openRecordActivity(cursor.getString(cursor.getColumnIndexOrThrow( "name")), cursor.getString(cursor.getColumnIndexOrThrow( "_id")));
                //c.getString(c.getColumnIndexOrThrow( "name"));
            }
        });
    }

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
