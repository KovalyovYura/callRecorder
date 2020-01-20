package com.yuriyk_israelb.finalProject;

public class RecordDetails {
    private String name, date, time, Rlong;

    public RecordDetails(String name, String date, String time, String Rlong)
    {
        this.name = name;
        this.date = date;
        this.time = time;
        this.Rlong = Rlong;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRlong() {
        return Rlong;
    }

    public void setRlong(String rlong) {
        Rlong = rlong;
    }
}
