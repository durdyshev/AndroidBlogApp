package com.example.komp.gurles;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.sinch.android.rtc.NotificationResult;
import com.sinch.android.rtc.SinchHelpers;

import java.util.Objects;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // make sure you have created a SinchClient
        if (SinchHelpers.isSinchPushPayload(remoteMessage.getData())) {

        }

    String messageTitle= Objects.requireNonNull(remoteMessage.getNotification()).getTitle();
        String messagebody= Objects.requireNonNull(remoteMessage.getNotification()).getBody();
        String click_action=remoteMessage.getNotification().getClickAction();
        String dataady=remoteMessage.getData().get("ady");
        String dataid=remoteMessage.getData().get("id");
        String datasurat=remoteMessage.getData().get("surat");

        NotificationCompat.Builder mBuilder=
                new NotificationCompat.Builder(this,getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.mipmap.ic_launcher)
               .setContentTitle(messageTitle)
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
}
