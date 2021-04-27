package com.example.komp.gurles;

import java.util.Date;

public class Chatadapter {
    private String from;

    private Date time;



    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Chatadapter(){

    }
    public Chatadapter(String from, Date time) {
        this.from = from;
        this.time = time;

    }


}
