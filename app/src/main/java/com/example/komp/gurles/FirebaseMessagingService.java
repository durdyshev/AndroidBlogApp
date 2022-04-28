package com.example.komp.gurles;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.RemoteMessage;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchHelpers;
import com.sinch.android.rtc.calling.Call;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {


    private SinchClass sinchClass;
    private Call call;
    private SinchClient sinchClient;
    private FirebaseAuth mAuth;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // make sure you have created a SinchClient
        if (SinchHelpers.isSinchPushPayload(remoteMessage.getData())) {
            mAuth = FirebaseAuth.getInstance();
            sinchClass=new SinchClass(sinchClient,call,this,mAuth.getCurrentUser().getUid());

        }

    String messageTitle= Objects.requireNonNull(remoteMessage.getNotification()).getTitle();
        String messagebody= Objects.requireNonNull(remoteMessage.getNotification()).getBody();
        String click_action=remoteMessage.getNotification().getClickAction();
        String dataady=remoteMessage.getData().get("ady");
        String dataid=remoteMessage.getData().get("id");
        String datasurat=remoteMessage.getData().get("surat");
        Uri defaultSoundUri =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);












        NotificationCompat.Builder mBuilder=
                new NotificationCompat.Builder(this,getString(R.string.default_notification_channel_id))
                .setLargeIcon(hey(datasurat))
                .setSmallIcon(R.drawable.signal_round)
               .setContentTitle(messageTitle)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(defaultSoundUri)
                        .setVibrate(new long[] { 1000, 0, 0, 0, 0 })


                .setContentText(messagebody);
        Intent intent=new Intent(click_action);
        intent.putExtra("ady",dataady);
        intent.putExtra("id",dataid);
        intent.putExtra("surat",datasurat);
        PendingIntent resultPendingIntent=PendingIntent.getActivity(
                this,
                0
        ,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);


        int mNotificationId= (int)System.currentTimeMillis();
        NotificationManager mNotifyMgr=
                (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        assert mNotifyMgr != null;
        mNotifyMgr.notify(mNotificationId,mBuilder.build());
    }

    private Bitmap hey(String datasurat) {
        InputStream in = null;Bitmap bmp;
        try {
            in = new URL(datasurat).openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        bmp = BitmapFactory.decodeStream(in);
        return bmp;
    }
}
