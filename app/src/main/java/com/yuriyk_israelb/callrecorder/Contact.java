package com.yuriyk_israelb.callrecorder;

import android.net.Uri;

public class Contact {
    private String name;
    private int imgResourceID;
    private Uri uri;
    private boolean isBlocked, isRecord;

    public Contact(String name, int imgResourceID, Uri uri){
        this.name = name;
        this.imgResourceID = imgResourceID;
        this.uri = uri;
    }

    public String getName() { return name; }

    public int getImgResourceID() { return imgResourceID; }

    public boolean isBlocked(){ return isBlocked; }

    public boolean isRecord() { return isRecord; }

    public Uri getUri(){ return uri; }

    public void setBlocked(boolean blocked) { this.isBlocked = blocked; }

    public void setRecord(boolean record) { this.isRecord = record; }
}
