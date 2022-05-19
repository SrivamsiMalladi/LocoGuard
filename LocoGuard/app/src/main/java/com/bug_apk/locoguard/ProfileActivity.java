package com.bug_apk.locoguard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    String[] languages = new String[]{"EN", "DE"};
    String selectedLanguage = "en";
    Spinner dropdown;
    int mPosition = 0;

    boolean hasWard = false;
    boolean hasOnGoingJourney = false;
    boolean toastCancel = true;

    user userDetails;
    journey journeyDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SharedPreferences prefs = getSharedPreferences("LocoGuard", MODE_PRIVATE);
        selectedLanguage = prefs.getString("language", "en");
        Log.i("ProfileActivity","User language preference:" + selectedLanguage);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            selectedLanguage = Locale.getDefault().getLanguage();
            mPosition = selectedLanguage.equals("de") ? 1 : 0;
            Log.i("ProfileActivity", "Android N: Language changed to " + selectedLanguage);
        }

        mAuth = FirebaseAuth.getInstance();

        sharedPref = this.getSharedPreferences("LocoGuard",MODE_PRIVATE);
        editor = sharedPref.edit();

        userDetails = getIntent().getParcelableExtra("userDetails");
        ((TextView) findViewById(R.id.username)).setText(userDetails.getName());

        dropdown = findViewById(R.id.languageSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.layout_language_spinner, languages);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);

        FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if(dataSnapshot.getValue(user.class).getWard()!=null) {
                        hasWard = true;
                        userDetails = dataSnapshot.getValue(user.class);
                        FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.getWard()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()) {
                                    journeyDetails = dataSnapshot.getValue(journey.class);
                                    ((ImageView) findViewById(R.id.grd_icn)).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent journeyIntent = new Intent(ProfileActivity.this, TrackJourneyActivity.class);
                                            journeyIntent.putExtra("userDetails", userDetails);
                                            journeyIntent.putExtra("userJourney", journeyDetails);
                                            startActivity(journeyIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
                FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.encodedEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (!dataSnapshot.getValue(journey.class).getJourneyCompleted().equals("true")) {
                                hasOnGoingJourney = true;
                                journeyDetails = dataSnapshot.getValue(journey.class);
                                ((ImageView) findViewById(R.id.grd_icn)).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent journeyIntent = new Intent(ProfileActivity.this, JourneyActivity.class);
                                        journeyIntent.putExtra("userDetails", userDetails);
                                        journeyIntent.putExtra("userJourney", journeyDetails);
                                        startActivity(journeyIntent);
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ((ImageView) findViewById(R.id.grd_icn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasWard) {
                    Intent journeyIntent = new Intent(ProfileActivity.this, TrackJourneyActivity.class);
                    journeyIntent.putExtra("userDetails", userDetails);
                    journeyIntent.putExtra("userJourney", journeyDetails);
                    startActivity(journeyIntent);
                }
                else if(hasOnGoingJourney) {
                    Intent journeyIntent = new Intent(ProfileActivity.this, JourneyActivity.class);
                    journeyIntent.putExtra("userDetails", userDetails);
                    journeyIntent.putExtra("userJourney", journeyDetails);
                    startActivity(journeyIntent);
                }
                else {
                    Intent journeyIntent = new Intent(ProfileActivity.this, GuardActivity.class);
                    journeyIntent.putExtra("userDetails", userDetails);
                    startActivity(journeyIntent);
                }
            }
        });

        ((ImageView) findViewById(R.id.sos)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasOnGoingJourney) {
                    Toast.makeText(ProfileActivity.this, getString(R.string.friendsList_pressHoldInEmergency), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(ProfileActivity.this, getString(R.string.friendsList_noOngoingJourney), Toast.LENGTH_SHORT).show();
                }
            }
        });

        ((ImageView) findViewById(R.id.sos)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    new CountDownTimer(2000,200) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            Toast toast = Toast.makeText(ProfileActivity.this, getString(R.string.friendsList_holdFor2Seconds), Toast.LENGTH_SHORT);
                            if (event.getAction() == MotionEvent.ACTION_UP) {

                                if(toastCancel) {
                                    toast.show();
                                    toastCancel = false;
                                }
                            }
                        }
                        public void onFinish() {
                            toastCancel = true;
                            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                                if (hasOnGoingJourney) {
                                    Toast.makeText(ProfileActivity.this, getString(R.string.friendsList_guardianAlerted), Toast.LENGTH_SHORT).show();
                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("journeys");
                                    reference.child(userDetails.encodedEmail()).child("sos").setValue(true);
                                    String message = (journeyDetails.getWardName() + " " + getString(R.string.guardianAdded_wardInDanger));
                                    if(journeyDetails.getPhoneContacts()!= null)
                                        for(int i=0;i<journeyDetails.getPhoneContacts().size();i++){

                                            String number = journeyDetails.getPhoneContacts().get(i);

                                            SmsManager mySmsManager = SmsManager.getDefault();
                                            mySmsManager.sendTextMessage(number,null, message, null, null);
                                        }

                                } else {
                                    Toast.makeText(ProfileActivity.this, getString(R.string.friendsList_noOngoingJourney), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }.start();
                }
                return true;
            }
        });

        ((ImageView)findViewById(R.id.cntcts_icn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent friendsListIntent = new Intent(ProfileActivity.this, FriendsListActivity.class);
                friendsListIntent.putExtra("userDetails",userDetails);
                startActivity(friendsListIntent);
            }
        });
        ((ImageView)findViewById(R.id.hme_icn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(ProfileActivity.this, WelcomeActivity.class);
                homeIntent.putExtra("userDetails", userDetails);
                startActivity(homeIntent);
            }
        });
        ((ImageButton)findViewById(R.id.bck_arw_cin)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(ProfileActivity.this, WelcomeActivity.class);
                backIntent.putExtra("userDetails",userDetails);
                startActivity(backIntent);
            }
        });
        ((LinearLayout)findViewById(R.id.bck_arw_trans)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(ProfileActivity.this, WelcomeActivity.class);
                backIntent.putExtra("userDetails",userDetails);
                startActivity(backIntent);
            }
        });
        ((LinearLayout)findViewById(R.id.notifications_trans)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent locationIntent = new Intent(ProfileActivity.this, NotificationsActivity.class);
                locationIntent.putExtra("userDetails", userDetails);
                startActivity(locationIntent);
            }
        });
        ((LinearLayout)findViewById(R.id.permissions_trans)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent locationIntent = new Intent(ProfileActivity.this, PermissionsActivity.class);
                locationIntent.putExtra("userDetails", userDetails);
                startActivity(locationIntent);
            }
        });
        ((LinearLayout)findViewById(R.id.journeyHistory_trans)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent locationIntent = new Intent(ProfileActivity.this, JourneyHistoryActivity.class);
                locationIntent.putExtra("userDetails", userDetails);
                startActivity(locationIntent);
            }
        });
        ((LinearLayout)findViewById(R.id.language_trans)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dropdown.performClick();
            }
        });
        ((LinearLayout)findViewById(R.id.help_trans)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent locationIntent = new Intent(ProfileActivity.this, ComingSoonActivity.class);
                locationIntent.putExtra("userDetails", userDetails);
                startActivity(locationIntent);
            }
        });

        ((LinearLayout)findViewById(R.id.logout_trans)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("LocoGuard", MODE_PRIVATE);
                boolean startedJourney = prefs.getBoolean("startedJourney", false);
                Log.i("ProfileActivity","startedJourney:" + prefs.getBoolean("startedJourney", false));

                if(!startedJourney) {
                    Intent broadcastIntent = new Intent("LogoutBroadcast");
                    LocalBroadcastManager.getInstance(ProfileActivity.this).sendBroadcast(broadcastIntent);

                    editor.putBoolean("staySignedIn", false);
                    editor.commit();

                    mAuth.signOut();

                    Intent logoutIntent = new Intent(ProfileActivity.this, SignInActivity.class);
                    startActivity(logoutIntent);
                    finish();
                }
                else {
                    Toast.makeText(ProfileActivity.this, getString(R.string.profile_cannotLogout), Toast.LENGTH_SHORT).show();
                }
            }
        });


        if(selectedLanguage.equals("de")) {
            dropdown.setTag("1");
            dropdown.setSelection(1);
        }
        else {
            dropdown.setTag("0");
            dropdown.setSelection(0);
        }

        ((Spinner)findViewById(R.id.languageSpinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    if ((!dropdown.getTag().toString().equals(Integer.toString(position))) && (mPosition == position)) {
                        if (!(Locale.getDefault().getLanguage().equals(languages[position].toLowerCase()))) {
                            Log.i("IntroActivity", "OnItemSelected called: API24 user wants to change language");
                            Toast.makeText(ProfileActivity.this, getString(R.string.toast_noLanguageSwitchingSupport), Toast.LENGTH_SHORT).show();
                            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                        }
                    }
                    if ((dropdown.getTag().toString().equals(Integer.toString(position))) && (mPosition == position)) {
                        mPosition = (mPosition==0) ? 1 : 0;
                    }
                    Log.i("IntroActivity", "Tag: " + dropdown.getTag().toString());
                    Log.i("IntroActivity", "position: " + position);
                    Log.i("IntroActivity", "mPosition: " + mPosition);
                    selectedLanguage = Locale.getDefault().getLanguage();
                    if(selectedLanguage.equals("de")) {
                        Log.i("IntroActivity", "OnItemSelected called: Updating spinner to show " + selectedLanguage);
                        dropdown.setTag("1");
                        dropdown.setSelection(1);
                    }
                    else {
                        Log.i("IntroActivity", "OnItemSelected called: Updating spinner to show " + selectedLanguage);
                        dropdown.setTag("0");
                        dropdown.setSelection(0);
                    }
                }
                else {
                    if (position == 0) {
                        //English
                        selectedLanguage = "en";
                        Log.i("IntroActivity", "English selected");
                        setLocale(ProfileActivity.this, "en");
                    } else {
                        //German
                        selectedLanguage = "de";
                        Log.i("IntroActivity", "German selected");
                        setLocale(ProfileActivity.this, "de");
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(new NoInternetHelper(this).noInternetBroadcastCallback,
                new IntentFilter("InternetStatusBroadcast"));
        new NoInternetHelper(this).checkInternet();
    }

    public void setLocale(Activity context, String languageCode) {
        Locale locale;
        locale = new Locale(languageCode);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        Locale.setDefault(locale);
        config.setLocale(locale);
        context.getBaseContext().getResources().updateConfiguration(config,
                context.getBaseContext().getResources().getDisplayMetrics());
        SharedPreferences prefs = getSharedPreferences("LocoGuard", MODE_PRIVATE);
        if(!selectedLanguage.equals(prefs.getString("language", "en"))) {
            SharedPreferences.Editor editor = context.getSharedPreferences("LocoGuard", MODE_PRIVATE).edit();
            editor.putString("language", languageCode);
            editor.apply();
            Intent intent = context.getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            context.finish();
            context.startActivity(intent);
//            Intent i = context.getBaseContext().getPackageManager()
//                    .getLaunchIntentForPackage( context.getBaseContext().getPackageName() );
//            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            context.startActivity(i);
        }
    }
}
