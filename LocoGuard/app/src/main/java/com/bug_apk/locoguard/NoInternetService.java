package com.bug_apk.locoguard;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.FragmentTransaction;
import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Fragment;
import android.view.View;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NoInternetService extends IntentService {

    boolean internetConnected = false;

    public NoInternetService() {
        super("NoInternetService");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.i("NoInternetService", "onHandleIntent: No intenet service started");
            checkConnectivity();
            Intent broadcastIntent = new Intent("InternetStatusBroadcast");
            broadcastIntent.putExtra("InternetStatus", internetConnected);
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkConnectivity();
                }
            }, 1000);
        }
    }

    public ConnectivityManager.NetworkCallback connectivityCallback
            = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            internetConnected = true;
            Intent broadcastIntent = new Intent("InternetStatusBroadcast");
            broadcastIntent.putExtra("InternetStatus", internetConnected);
            LocalBroadcastManager.getInstance(NoInternetService.this).sendBroadcast(broadcastIntent);
            Log.i("NoInternetService", "INTERNET CONNECTED");
        }

        @Override
        public void onLost(Network network) {
            internetConnected = false;
            Intent broadcastIntent = new Intent("InternetStatusBroadcast");
            broadcastIntent.putExtra("InternetStatus", internetConnected);
            LocalBroadcastManager.getInstance(NoInternetService.this).sendBroadcast(broadcastIntent);
            Log.i("NoInternetService", "INTERNET LOST");
        }
    };

    // Method to check network connectivity in Main Activity
    public void checkConnectivity() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) NoInternetService.this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        internetConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        if(internetConnected) {
            Intent broadcastIntent = new Intent("InternetStatusBroadcast");
            broadcastIntent.putExtra("InternetStatus", internetConnected);
            LocalBroadcastManager.getInstance(NoInternetService.this).sendBroadcast(broadcastIntent);
            Log.i("NoInternetService", "INTERNET CONNECTED");
        }
        else {
            Intent broadcastIntent = new Intent("InternetStatusBroadcast");
            broadcastIntent.putExtra("InternetStatus", internetConnected);
            LocalBroadcastManager.getInstance(NoInternetService.this).sendBroadcast(broadcastIntent);
            Log.i("NoInternetService", "INTERNET LOST");
        }

        connectivityManager.registerNetworkCallback(
                new NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build(), connectivityCallback);
    }
}
