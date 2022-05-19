package com.bug_apk.locoguard;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static android.content.Context.MODE_PRIVATE;
import static com.bug_apk.locoguard.App.CHANNEL_ID;

public class WardETABroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String ward = intent.getStringExtra("ward");
        SharedPreferences sharedPref = context.getSharedPreferences("LocoGuard", MODE_PRIVATE);
        boolean wardAtDestination =  sharedPref.getBoolean("wardAtDestination", false);
        if(!wardAtDestination) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle("Journey alert")
                    .setContentText("You were supposed to be at the destination by now.")
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setSmallIcon(R.drawable.logo_locoguard);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(2, builder.build());

            SharedPreferences.Editor editor = context.getSharedPreferences("LocoGuard", MODE_PRIVATE).edit();
            editor.putBoolean("wardAtDestination", true);
            editor.commit();
        }
    }
}
