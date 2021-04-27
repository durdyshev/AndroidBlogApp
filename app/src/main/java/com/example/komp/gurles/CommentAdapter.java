package com.example.komp.gurles;

import java.util.Date;



public class CommentAdapter {
    public String sms;
    public String userid;
    public Date wagt;

    public CommentAdapter(){

    }

    public String getSms() {
        return sms;
    }

    public void setSms(String sms) {
        this.sms = sms;
    }

    public String getUser_id() {
        return userid;
    }

    public void setUser_id(String userid) {
        this.userid = userid;
    }

    public Date getWagt() {
        return wagt;
    }

    public void setWagt(Date wagt) {
        this.wagt = wagt;
    }

    public CommentAdapter(String sms, String userid, Date wagt) {

        this.sms = sms;
        this.userid = userid;
        this.wagt = wagt;
    }
}
