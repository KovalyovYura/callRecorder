package com.yuriyk_israelb.callrecorder;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ContactAdapter extends ArrayAdapter<Contact>
{

    public ContactAdapter(Activity context, ArrayList<Contact> androidFlavors) {
        super(context, 0, androidFlavors);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if(listItemView == null)
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.contact_list, parent, false);


        // Get the {@link Contact} object located at this position in the list
        Contact currentContact = getItem(position);


        TextView nameTextView = listItemView.findViewById(R.id.txtContactNameID);
        nameTextView.setText(currentContact.getName());


        ImageView iconView = listItemView.findViewById(R.id.imgContactImageID);
        if(currentContact.getImgResourceID() == 0)
            iconView.setImageResource(R.drawable.default_avatar);
        else
            iconView.setImageURI(currentContact.getUri());




       if(currentContact.isBlocked())
           listItemView.findViewById(R.id.icCallBlockedID).setVisibility(View.VISIBLE);
       else
           listItemView.findViewById(R.id.icCallBlockedID).setVisibility(View.INVISIBLE);
       if(currentContact.isRecord())
           listItemView.findViewById(R.id.icCallRecordID).setVisibility(View.VISIBLE);
       else
           listItemView.findViewById(R.id.icCallRecordID).setVisibility(View.INVISIBLE);

        return listItemView;
    }

}

