package com.chat.omar.simplechat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService{
    private NotificationManager notificationManager;
    
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        System.out.println("HVORDAN VIRKER DET HER");
        //Setting up Notification channels for android O and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels();
        }
;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels() {

        CharSequence adminChannelName = "TEST1";
        String adminChannelDescription = "TEST2";

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel("ETELLERANDET", adminChannelName, NotificationManager.IMPORTANCE_LOW);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,NOTIFICATION_SERVICE);
        notificationBuilder.setColor(Color.RED);
        notificationManager.notify(2400,notificationBuilder.build());
    }


}
