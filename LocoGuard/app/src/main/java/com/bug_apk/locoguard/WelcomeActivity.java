package com.bug_apk.locoguard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static com.bug_apk.locoguard.App.CHANNEL_ID;

public class WelcomeActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();

    user userDetails;
    journey journeyDetails;
    boolean pressAgaintoExit = false;
    boolean journeyActivity = false;
    String wardName;
    boolean toastCancel = true;
    boolean firstTime = false;


    @SuppressLint("ClickableViewAccessibility")
    void setJourneyActivity() {
        ((Button)findViewById(R.id.startJourney)).setText(getString(R.string.welcome_trackJourney));
        journeyActivity = true;
        ((Button) findViewById(R.id.startJourney)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent journeyIntent = new Intent(WelcomeActivity.this, JourneyActivity.class);
                journeyIntent.putExtra("userDetails", userDetails);
                journeyIntent.putExtra("userJourney", journeyDetails);
                startActivity(journeyIntent);
            }
        });
        ((ImageView)findViewById(R.id.Guard)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent journeyIntent = new Intent(WelcomeActivity.this, JourneyActivity.class);
                journeyIntent.putExtra("userDetails", userDetails);
                journeyIntent.putExtra("userJourney", journeyDetails);
                startActivity(journeyIntent);
            }
        });

        ((ImageView) findViewById(R.id.sos)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (journeyActivity) {
                    Toast.makeText(WelcomeActivity.this, getString(R.string.welcome_pressHoldInEmergency), Toast.LENGTH_SHORT).show();
                    }
                 else {
                    Toast.makeText(WelcomeActivity.this, getString(R.string.welcome_noOngoingJourney), Toast.LENGTH_SHORT).show();
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
                            Toast toast = Toast.makeText(WelcomeActivity.this, getString(R.string.welcome_holdFor2Seconds), Toast.LENGTH_SHORT);
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
                                if (journeyActivity) {
                                    Toast.makeText(WelcomeActivity.this, getString(R.string.welcome_guardianAlerted), Toast.LENGTH_SHORT).show();
                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("journeys");
                                    ArrayList<String> guardiansList = journeyDetails.getGuardiansList();
                                    reference.child(userDetails.encodedEmail()).child("sos").setValue(true);
                                    String message = (journeyDetails.getWardName() + " " + getString(R.string.guardianAdded_wardInDanger));
                                    if(journeyDetails.getPhoneContacts()!= null)
                                        for(int i=0;i<journeyDetails.getPhoneContacts().size();i++){

                                            String number = journeyDetails.getPhoneContacts().get(i);

                                            SmsManager mySmsManager = SmsManager.getDefault();
                                            mySmsManager.sendTextMessage(number,null, message, null, null);
                                        }

                                } else {
                                    Toast.makeText(WelcomeActivity.this, getString(R.string.welcome_noOngoingJourney), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }.start();
                }
                return true;
            }
        });

    }

    void setTrackJourneyActivity() {
        ((Button)findViewById(R.id.startJourney)).setText(getString(R.string.welcome_trackWard) + " " + wardName);
        journeyActivity = true;
        ((Button)findViewById(R.id.startJourney)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent trackJourneyActivity = new Intent(WelcomeActivity.this, TrackJourneyActivity.class);
                trackJourneyActivity.putExtra("userDetails", userDetails);
                trackJourneyActivity.putExtra("userJourney", journeyDetails);
                startActivity(trackJourneyActivity);
            }
        });
        ((ImageView)findViewById(R.id.Guard)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent trackJourneyActivity = new Intent(WelcomeActivity.this, TrackJourneyActivity.class);
                trackJourneyActivity.putExtra("userDetails", userDetails);
                trackJourneyActivity.putExtra("userJourney", journeyDetails);
                startActivity(trackJourneyActivity);
            }
        });
        ((ImageView) findViewById(R.id.sos)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (journeyActivity) {
                    Toast.makeText(WelcomeActivity.this, getString(R.string.welcome_pressHoldInEmergency), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(WelcomeActivity.this, getString(R.string.welcome_noOngoingJourney), Toast.LENGTH_SHORT).show();
                }
            }
        });

        ((ImageView) findViewById(R.id.sos)).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(WelcomeActivity.this, getString(R.string.welcome_noOngoingJourney), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    void setGuardActivity() {
        ((Button)findViewById(R.id.startJourney)).setText(getString(R.string.welcome_createJourney));
        ((Button)findViewById(R.id.startJourney)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent guardIntent = new Intent(WelcomeActivity.this, GuardActivity.class);
                guardIntent.putExtra("userDetails", userDetails);
                startActivity(guardIntent);
            }
        });

        ((ImageView)findViewById(R.id.Guard)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent guardIntent = new Intent(WelcomeActivity.this, GuardActivity.class);
                guardIntent.putExtra("userDetails", userDetails);
                startActivity(guardIntent);
            }
        });
        ((ImageView) findViewById(R.id.sos)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(WelcomeActivity.this, getString(R.string.welcome_noOngoingJourney), Toast.LENGTH_SHORT).show();
            }
        });
    }

    ValueEventListener journeyCheck = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.i("WelcomeActivity", "Checking ward of "+userDetails.getName());
            if(dataSnapshot.exists()) {
                Log.i("WelcomeActivity","Journey exists");
                journeyDetails =  dataSnapshot.getValue(journey.class);
                Log.i("WelcomeActivity",""+journeyDetails.getWard());
                Log.i("WelcomeActivity",""+journeyDetails.getJourneyCompleted());
                if(journeyDetails.getJourneyCompleted() == null) {
                    setGuardActivity();
                }
                else if(journeyDetails.getJourneyCompleted().equals("false")) {
                    Log.i("WelcomeActivity","Journey on going");
                    setJourneyActivity();
                }
                else {
                    Log.i("WelcomeActivity","Old journey record");
                    DatabaseReference myRef = database.getReference("users").child(userDetails.encodedEmail());
                    myRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Log.i("WelcomeActivity", "Ward exist");
                                userDetails = dataSnapshot.getValue(user.class);
                                if(userDetails.getWard()!=null) {
                                    DatabaseReference myRef = database.getReference("journeys").child(userDetails.getWard());
                                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            journeyDetails = dataSnapshot.getValue(journey.class);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                    database.getReference().child("users").child(userDetails.getWard()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            user ward = dataSnapshot.getValue(user.class);
                                            wardName = ward.getName();
                                            setTrackJourneyActivity();

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                                else {
                                    Log.i("WelcomeActivity","No ward found");
                                    setGuardActivity();
                                }
                            }
                            else {
                                Log.i("WelcomeActivity","No ward found");
                                setGuardActivity();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
            else {
                Log.i("WelcomeActivity", "No previous journey data found");
                DatabaseReference myRef = database.getReference("users").child(userDetails.encodedEmail()).child("ward");
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String ward = dataSnapshot.getValue(String.class);
                            Log.i("WelcomeActivity", "Ward exist");
                            DatabaseReference myRef = database.getReference("journeys").child(ward);
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    journeyDetails = dataSnapshot.getValue(journey.class);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            database.getReference().child("users").child(userDetails.getWard()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    user ward = dataSnapshot.getValue(user.class);
                                    wardName = ward.getName();
                                    setTrackJourneyActivity();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        else {
                            Log.i("WelcomeActivity","NNo ward found");
                            setGuardActivity();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            Log.w("signIn", "loadPost:onCancelled", databaseError.toException());
        }
    };

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(pressAgaintoExit)
        {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
//            finishAndRemoveTask();
//            Intent intent = new Intent(WelcomeActivity.this, IntroActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.putExtra("EXIT", true);
//            startActivity(intent);
        }
        else
        {
            pressAgaintoExit=true;
            Toast.makeText(this,getString(R.string.welcome_pressAgainToExit), Toast.LENGTH_SHORT).show();
            int intervalTime = 2200;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    pressAgaintoExit = false;
                }
            },intervalTime);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if(timeOfDay >= 0 && timeOfDay < 5){
            ((ImageView) findViewById(R.id.night_wallpaper)).setVisibility(View.VISIBLE);
        }else if(timeOfDay >= 5 && timeOfDay < 16){
            ((ImageView) findViewById(R.id.night_wallpaper)).setVisibility(View.GONE);
            ((ImageView) findViewById(R.id.morning_wallpaper)).setVisibility(View.VISIBLE);
        }else if(timeOfDay >= 16 && timeOfDay < 21){
            ((ImageView) findViewById(R.id.morning_wallpaper)).setVisibility(View.GONE);
            ((ImageView) findViewById(R.id.evening_wallpaper)).setVisibility(View.VISIBLE);
        }else if(timeOfDay >= 21 && timeOfDay < 24){
            ((ImageView) findViewById(R.id.evening_wallpaper)).setVisibility(View.GONE);
            ((ImageView) findViewById(R.id.night_wallpaper)).setVisibility(View.VISIBLE);
        }


        Locale locale = getResources().getConfiguration().locale;
        Locale.setDefault(locale);
        String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime());
        TextView textViewDate = findViewById(R.id.text_view_date);
        textViewDate.setText(currentDate);

        userDetails = getIntent().getParcelableExtra("userDetails");
        journeyActivity = getIntent().getBooleanExtra("Ongoing",false);

        Log.i("WelcomeActivity", "onCreate(): "+userDetails.getName());


//        if(userDetails.getDp() == null) {
//            ((TextView) findViewById(R.id.welcometext)).setText(getString(R.string.welcome_welcomeName));
//            Log.i("WelcomeActivity", "Splash screen time was not enough. Retrieving userData again");
//            ((TextView) findViewById(R.id.welcometext)).setText(getString(R.string.welcome_welcomeName) + " " + userDetails.getName());
//            SharedPreferences sharedPref = this.getSharedPreferences("LocoGuard", Context.MODE_PRIVATE);
//            FirebaseAuth mAuth = FirebaseAuth.getInstance();
//            boolean userLoggedIn = sharedPref.getBoolean("staySignedIn", false);
//            Log.i("WelcomeActivity", "sharedPreference staySignedIn:" + userLoggedIn);
//            if (userLoggedIn) {
//                if (mAuth.getCurrentUser() != null) {
//                    Log.i("WelcomeActivity", "userDetails.getEmail():" + userDetails.getEmail());
////                    userDetails.setEmail(mAuth.getCurrentUser().getEmail());
//                    database.getReference("users").child(userDetails.encodedEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            if (dataSnapshot.exists()) {
//                                userDetails = dataSnapshot.getValue(user.class);
//                                ((TextView) findViewById(R.id.welcometext)).setText(getString(R.string.welcome_welcomeName) + " " + userDetails.getName());
//
//                                if(userDetails.getWard()!=null) {
//                                    database.getReference().child("users").child(userDetails.getWard()).addValueEventListener(new ValueEventListener() {
//                                        @Override
//                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                            Log.i("WelcomeActivity", "Checking ward of  "+userDetails.getName());
//                                            user ward = dataSnapshot.getValue(user.class);
//                                            wardName = ward.getName();
//                                        }
//
//                                        @Override
//                                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                                        }
//                                    });
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                        }
//                    });
//                }
//            }
//        }

        if(userDetails.getWard()!=null) {
            database.getReference().child("users").child(userDetails.getWard()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.i("WelcomeActivity", "Checking ward of  "+userDetails.getName());
                    user ward = dataSnapshot.getValue(user.class);
                    wardName = ward.getName();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        if(userDetails.getName() != null) {
            ((TextView) findViewById(R.id.welcometext)).setText(getString(R.string.welcome_welcomeName) + " " + userDetails.getName());
        }

        DatabaseReference myRef = database.getReference("journeys").child(userDetails.encodedEmail());
        myRef.addListenerForSingleValueEvent(journeyCheck);

        ((ImageView)findViewById(R.id.Profile)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
			Intent friendsListIntent = new Intent(WelcomeActivity.this, FriendsListActivity.class);
                friendsListIntent.putExtra("userDetails",userDetails);
                friendsListIntent.putExtra("userJourney", journeyDetails);
                friendsListIntent.putExtra("Ongoing",journeyActivity);
				startActivity(friendsListIntent);
            }
        });

        ((ImageView)findViewById(R.id.sos)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Toast.makeText(WelcomeActivity.this, getString(R.string.welcome_noOngoingJourney), Toast.LENGTH_SHORT).show();
            }
        });

		((ImageButton)findViewById(R.id.Menu)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logoutIntent = new Intent(WelcomeActivity.this, ProfileActivity.class);
                logoutIntent.putExtra("userDetails",userDetails);
                startActivity(logoutIntent);
            }
        });
        Intent intent = new Intent(WelcomeActivity.this, GuardianAddedService.class);
        intent.putExtra("userDetails", userDetails);

        startService(intent);

        LocalBroadcastManager.getInstance(WelcomeActivity.this).registerReceiver(guardianAdded,
                new IntentFilter("guardAddedBroadcast"));

        LocalBroadcastManager.getInstance(this).registerReceiver(new NoInternetHelper(this).noInternetBroadcastCallback,
                new IntentFilter("InternetStatusBroadcast"));
        new NoInternetHelper(this).checkInternet();
    }

    private BroadcastReceiver guardianAdded = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String ward = intent.getStringExtra("ward");
            SharedPreferences sharedPref = context.getSharedPreferences("LocoGuard", Context.MODE_PRIVATE);
            boolean guardianAdded =  sharedPref.getBoolean("guardianAdded", false);
            if ((userDetails.getPreferences()!=null) && (userDetails.getPreferences().get("journeyGuardianAdded").equals("true")) && !guardianAdded) {
                database.getReference().child("users").child(userDetails.getWard()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.i("WelcomeActivity", "Checking ward of  "+userDetails.getName());
                        user ward = dataSnapshot.getValue(user.class);
                        wardName = ward.getName();

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(WelcomeActivity.this, CHANNEL_ID)
                                .setContentTitle("Guardian added")
                                .setContentText(wardName + " " + getString(R.string.welcomeActivity_addedAsGuardian))
                                .setPriority(Notification.PRIORITY_HIGH)
                                .setSmallIcon(R.drawable.logo_locoguard);

                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(WelcomeActivity.this);
                        notificationManager.notify(1, builder.build());

                        SharedPreferences.Editor editor = context.getSharedPreferences("LocoGuard", MODE_PRIVATE).edit();
                        editor.putBoolean("guardianAdded", true);
                        editor.commit();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        }
    };
}
