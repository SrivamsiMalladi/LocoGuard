package com.bug_apk.locoguard;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentTransaction;

public class NoInternetHelper {

    Context mContext;
    boolean internetConnected;

    public NoInternetHelper(Context context) {
        this.mContext = context;
    }

    public boolean isInternetConnected() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        internetConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
        return internetConnected;
    }

    public void checkInternet() {
        isInternetConnected();
        if(internetConnected) {
            Log.i("NoInternetHelpre", "Removing the NoInterentAlert");
            TextView noInternetAlert = (TextView)((Activity) mContext).findViewById(R.id.noInternetAlert);
            noInternetAlert.setVisibility(View.GONE);
        }
        else {
            Log.i("NoInternetHelpre", "Making the NoInterentAlert visible");
            TextView noInternetAlert = (TextView)((Activity) mContext).findViewById(R.id.noInternetAlert);
            noInternetAlert.setVisibility(View.VISIBLE);
        }
    }

    public BroadcastReceiver noInternetBroadcastCallback = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("NoInternetHelpre", "Internet broadcast received");
            internetConnected = intent.getBooleanExtra("InternetStatus", false);
            checkInternet();
        }
    };
}
