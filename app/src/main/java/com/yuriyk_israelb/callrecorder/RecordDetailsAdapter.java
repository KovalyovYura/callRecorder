package com.yuriyk_israelb.callrecorder;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class RecordDetailsAdapter extends CursorAdapter {
    public RecordDetailsAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.custom_row_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView txtName =  view.findViewById(R.id.text1);
        TextView txtPhone = view.findViewById(R.id.text2);
        TextView txtDateAndTime = view.findViewById(R.id.text3);
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        String dateAndTime = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
        String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone_number"));
        //ImageView image = view.findViewById(R.id.imageID);
//        if(!phone.equals(""))
//            image.setColorFilter(Color.GREEN);
//        else
//            image.setColorFilter(Color.GRAY);
        txtName.setText(name);
        txtDateAndTime.setText(dateAndTime);
        txtPhone.setText(phone);
    }
}