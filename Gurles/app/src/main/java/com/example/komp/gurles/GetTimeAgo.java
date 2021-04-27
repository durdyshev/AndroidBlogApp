package com.example.komp.gurles;

import android.app.Application;
import android.content.Context;

import java.util.Date;

public class GetTimeAgo extends Application {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final double DAY_MILLIS =  24 * HOUR_MILLIS;
    private static final double WEEK_MILLIS = 7 * DAY_MILLIS;
    private static final double MONTH_MILLIS = DAY_MILLIS * 30;
    private static final double YEAR_MILLIS = WEEK_MILLIS * 52;


    public static String getTimeAgo(long time, Context ctx) {
        final String sagat = android.text.format.DateFormat.format("hh:mm", new Date(time)).toString();
        final String gun=android.text.format.DateFormat.format("dd",new Date(time)).toString();
        final String ay=android.text.format.DateFormat.format("MM",new Date(time)).toString();

        final String yyl=android.text.format.DateFormat.format("yyyy",new Date(time)).toString();

        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS)
        {
            return "Şu gün "+sagat ;
        }
        else if (diff < 2 * MINUTE_MILLIS)
        {
            return "Şu gün "+sagat;
        }
        else if (diff < 50 * MINUTE_MILLIS)
        {

            double roundup = diff / MINUTE_MILLIS;
            int b = (int)(roundup);
            return  "Şu gün "+sagat;
        }
        else if (diff < 90 * MINUTE_MILLIS)
        {
            return "Şu gün "+sagat;
        }
        else if (diff < 24 * HOUR_MILLIS)
        {
            double roundup = diff / HOUR_MILLIS;
            int b = (int)(roundup);
            return "Şu gün "+sagat;
        }
        else if (diff < 48 * HOUR_MILLIS)
        {
            return "Düýn "+sagat;
        }
        else if (diff < 7 * DAY_MILLIS)
        {
            double roundup = diff / DAY_MILLIS;
            int b = (int)(roundup);
            return gun+" "+wagt(ay)+" "+sagat;
        }
        else if (diff < 2 * WEEK_MILLIS)
        {
            return gun+" "+wagt(ay)+" "+sagat;
        }
        else if (diff < DAY_MILLIS * 30.43675)
        {
            double roundup = diff / WEEK_MILLIS;
            int b = (int)(roundup);
            return gun+" "+wagt(ay)+" "+sagat;
        }
        else if (diff < 2 * MONTH_MILLIS)
        {
            return gun+" "+wagt(ay)+" "+sagat;
        }
        else if (diff < WEEK_MILLIS * 52.2)
        {
            double roundup = diff / MONTH_MILLIS;
            int b = (int)(roundup);
            return gun+" "+wagt(ay)+" "+sagat;
        }
        else if(diff < 2 * YEAR_MILLIS)
        {
            return gun+"."+wagt(ay)+"."+yyl+","+sagat;

        }
        else
        {
            double roundup = diff / YEAR_MILLIS;
            int b = (int)(roundup);
            return gun+"."+wagt(ay)+"."+yyl+","+sagat;
        }
    }

    private static   String wagt(String ay) {
        String yanwar="01";String fewral="02";String mart="03";
        String aprel="04";String may="05";String iyun="06";
        String iyul="07";String awgust="08";String sentyabr="09";
        String oktyabr="10";String noyabr="11";String dekabr="12";
        String buay=null;


       if(ay.equals(yanwar)){
           return buay="yanwar";}
           if(ay.equals(fewral)){
           return buay="fewral";
           }
        if(ay.equals(mart)){
            return buay="mart";
        }
        if(ay.equals(aprel)){
            return buay="aprel";
        }
        if(ay.equals(may)){
            return buay="may";
        }
        if(ay.equals(iyun)){
            return buay="iyun";
        }
        if(ay.equals(iyul)){
            return buay="iyul";
        }
        if(ay.equals(awgust)){
            return buay="awgust";
        }
        if(ay.equals(sentyabr)){
            return buay="sentyabr";
        }
        if(ay.equals(oktyabr)){
            return buay="oktyabr";
        }
        if(ay.equals(noyabr)){
            return buay="noyabr";
        }
        if(ay.equals(dekabr)){
            return buay="dekabr";
        }
           return buay;



    }
}

