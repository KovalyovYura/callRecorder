package com.yuriyk_israelb.callrecorder;


import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;



public class ContactAdapter extends CursorAdapter {
    public ContactAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.contact_list, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        SharedPreferences sp_blocked = context.getSharedPreferences("Blocks", Context.MODE_PRIVATE);
        SharedPreferences sp_record = context.getSharedPreferences("Records", Context.MODE_PRIVATE);

        TextView txtName = view.findViewById(R.id.txtContactNameID);
        TextView txtPhoneNumber = view.findViewById(R.id.txtNumberID);
        ImageView iconView = view.findViewById(R.id.imgContactImageID);
        ImageView iconRecord = view.findViewById(R.id.icCallRecordID);
        ImageView iconBlocked = view.findViewById(R.id.icCallBlockedID);
        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        txtName.setText(name);
        txtPhoneNumber.setText(number);
        int photoResourceId = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_FILE_ID));
        if (photoResourceId == 0)
            iconView.setImageResource(R.drawable.default_avatar);
        else
            iconView.setImageURI(ContentUris.withAppendedId(ContactsContract.DisplayPhoto.CONTENT_URI, photoResourceId));


        if (sp_record.getBoolean(name, false))
            iconRecord.setVisibility(View.VISIBLE);
        else
            iconRecord.setVisibility(View.INVISIBLE);

        if (sp_blocked.getBoolean(name, false))
            iconBlocked.setVisibility(View.VISIBLE);
        else
            iconBlocked.setVisibility(View.INVISIBLE);
    }

}



