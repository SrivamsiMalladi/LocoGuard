package com.bug_apk.locoguard;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.atomic.AtomicMarkableReference;

import static android.content.Context.MODE_PRIVATE;
import static com.bug_apk.locoguard.App.CHANNEL_ID;

public class ETABroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String wardName = intent.getStringExtra("wardName");
        Log.i("ETABroadcastReceiver","wardName" + wardName);
        SharedPreferences sharedPref = context.getSharedPreferences("LocoGuard", MODE_PRIVATE);
        boolean atDestination =  sharedPref.getBoolean("atDestination", false);
        if(!atDestination) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle(context.getString(R.string.ETA_journeyAlert))
                    .setContentText(wardName + " " + context.getString(R.string.ETA_wardIsSupposedToBeAtDestination))
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setSmallIcon(R.drawable.logo_locoguard);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(4, builder.build());

            SharedPreferences.Editor editor = context.getSharedPreferences("LocoGuard", MODE_PRIVATE).edit();
            editor.putBoolean("atDestination", true);
            editor.commit();
        }
    }
}
