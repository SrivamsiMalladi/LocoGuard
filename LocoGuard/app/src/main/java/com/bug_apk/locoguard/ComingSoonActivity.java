package com.bug_apk.locoguard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ComingSoonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coming_soon);
        user userDetails = getIntent().getParcelableExtra("userDetails");

        ((ImageButton)findViewById(R.id.bck_arw_trans)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(ComingSoonActivity.this, ProfileActivity.class);
                backIntent.putExtra("userDetails",userDetails);
                startActivity(backIntent);
            }
        });

        ((TextView) findViewById(R.id.email)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = "teambug.apk@gmail.com";

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"+email)); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, email);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        ((TextView) findViewById(R.id.video)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://code.ovgu.de/steup/bug.apk/-/raw/Sprint-1/design/Video/HelpGuide.mp4"));
                startActivity(browserIntent);
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(new NoInternetHelper(this).noInternetBroadcastCallback,
                new IntentFilter("InternetStatusBroadcast"));
        new NoInternetHelper(this).checkInternet();
    }
}
