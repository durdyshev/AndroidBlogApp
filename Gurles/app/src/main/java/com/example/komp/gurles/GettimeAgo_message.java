package com.example.komp.gurles;

import android.app.Application;
import android.content.Context;

import java.util.Date;

public class GettimeAgo_message extends Application {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final double DAY_MILLIS =  24 * HOUR_MILLIS;
    private static final double WEEK_MILLIS = 7 * DAY_MILLIS;
    private static final double MONTH_MILLIS = DAY_MILLIS * 30;
    private static final double YEAR_MILLIS = WEEK_MILLIS * 52;

    public static String getTimeAgo(long time, Context ctx) {
        final String sagat = android.text.format.DateFormat.format("hh:mm", new Date(time)).toString();
        final String gun=android.text.format.DateFormat.format("dd/MM",new Date(time)).toString();
        final String yyl=android.text.format.DateFormat.format("dd/MM/yyyy",new Date(time)).toString();

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
            return sagat;
        }
        else if (diff < 2 * MINUTE_MILLIS)
        {
            return sagat;
        }
        else if (diff < 50 * MINUTE_MILLIS)
        {

            double roundup = diff / MINUTE_MILLIS;
            int b = (int)(roundup);
            return sagat;
        }
        else if (diff < 90 * MINUTE_MILLIS)
        {
            return sagat;
        }
        else if (diff < 24 * HOUR_MILLIS)
        {
            double roundup = diff / HOUR_MILLIS;
            int b = (int)(roundup);
            return sagat;
        }
        else if (diff < 48 * HOUR_MILLIS)
        {
            return "düýn "+sagat;
        }
        else if (diff < 7 * DAY_MILLIS)
        {
            double roundup = diff / DAY_MILLIS;
            int b = (int)(roundup);
            return b + " gün öň "+sagat;
        }
        else if (diff < 2 * WEEK_MILLIS)
        {
            return gun+","+sagat;
        }
        else if (diff < DAY_MILLIS * 30.43675)
        {
            double roundup = diff / WEEK_MILLIS;
            int b = (int)(roundup);
            return gun+","+sagat;
        }
        else if (diff < 2 * MONTH_MILLIS)
        {
            return gun+","+sagat;
        }
        else if (diff < WEEK_MILLIS * 52.2)
        {
            double roundup = diff / MONTH_MILLIS;
            int b = (int)(roundup);
            return gun+","+sagat;
        }
        else if(diff < 2 * YEAR_MILLIS)
        {
            return yyl+","+sagat;
        }
        else
        {
            double roundup = diff / YEAR_MILLIS;
            int b = (int)(roundup);
            return yyl+","+sagat;
        }
    }

}
