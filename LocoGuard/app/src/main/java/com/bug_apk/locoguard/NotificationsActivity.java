package com.bug_apk.locoguard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationsActivity extends AppCompatActivity {

    boolean hasWard = false;
    boolean hasOnGoingJourney = false;
    boolean toastCancel = true;

    journey journeyDetails;
    user userDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        userDetails = getIntent().getParcelableExtra("userDetails");

        if (userDetails.getPreferences()!=null) {
            ((Switch) findViewById(R.id.journeyInProgressGPSOn)).setChecked(userDetails.getPreferences().get("journeyInProgressGPSOn").equals("true"));
            ((Switch) findViewById(R.id.journeyPaused)).setChecked(userDetails.getPreferences().get("journeyPaused").equals("true"));
            ((Switch) findViewById(R.id.journeyResumed)).setChecked(userDetails.getPreferences().get("journeyResumed").equals("true"));
            ((Switch) findViewById(R.id.journeySuccessful)).setChecked(userDetails.getPreferences().get("journeySuccessful").equals("true"));
//            ((Switch) findViewById(R.id.journeyInProgressGPSOff)).setChecked(userDetails.getPreferences().get("journeyInProgressGPSOff").equals("true"));
            ((Switch) findViewById(R.id.journeyCheckpointAdded)).setChecked(userDetails.getPreferences().get("journeyCheckpointAdded").equals("true"));
            ((Switch) findViewById(R.id.journeyStopped)).setChecked(userDetails.getPreferences().get("journeyStopped").equals("true"));
            ((Switch) findViewById(R.id.journeyFinished)).setChecked(userDetails.getPreferences().get("journeyFinished").equals("true"));
            ((Switch) findViewById(R.id.journeyRouteDeviated)).setChecked(userDetails.getPreferences().get("journeyRouteDeviated").equals("true"));
            ((Switch) findViewById(R.id.journeySOS)).setChecked(userDetails.getPreferences().get("distance25P").equals("true"));
            ((Switch) findViewById(R.id.distance25P)).setChecked(userDetails.getPreferences().get("distance50P").equals("true"));
            ((Switch) findViewById(R.id.distance50P)).setChecked(userDetails.getPreferences().get("distance75P").equals("true"));
            ((Switch) findViewById(R.id.distance75P)).setChecked(userDetails.getPreferences().get("journeyETAalert").equals("true"));
            ((Switch) findViewById(R.id.journeyGuardianAdded)).setChecked(userDetails.getPreferences().get("journeyGuardianAdded").equals("true"));
            ((Switch) findViewById(R.id.journeySOS)).setChecked(userDetails.getPreferences().get("journeySOSalert").equals("true"));
        }

        Log.i("NotificationsActivity", "User:"+userDetails.getEmail());


        FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i("NotificationsActivity", "Checking for wards");
                if (dataSnapshot.exists()) {
                    if(dataSnapshot.getValue(user.class).getWard()!=null) {
                        Log.i("NotificationsActivity", "ward exists");
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
                                            Intent journeyIntent = new Intent(NotificationsActivity.this, TrackJourneyActivity.class);
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
                    else {
                        Log.i("NotificationsActivity", "ward exists");

                    }
                }
                FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.encodedEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (!dataSnapshot.getValue(journey.class).getJourneyCompleted().equals("true")) {
                                Log.i("NotificationsActivity", "User has on going journey");
                                hasOnGoingJourney = true;
                                journeyDetails = dataSnapshot.getValue(journey.class);
                                ((ImageView) findViewById(R.id.grd_icn)).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent journeyIntent = new Intent(NotificationsActivity.this, JourneyActivity.class);
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
                    Intent journeyIntent = new Intent(NotificationsActivity.this, TrackJourneyActivity.class);
                    journeyIntent.putExtra("userDetails", userDetails);
                    journeyIntent.putExtra("userJourney", journeyDetails);
                    startActivity(journeyIntent);
                }
                else if(hasOnGoingJourney) {
                    Intent journeyIntent = new Intent(NotificationsActivity.this, JourneyActivity.class);
                    journeyIntent.putExtra("userDetails", userDetails);
                    journeyIntent.putExtra("userJourney", journeyDetails);
                    startActivity(journeyIntent);
                }
                else {
                    Intent journeyIntent = new Intent(NotificationsActivity.this, GuardActivity.class);
                    journeyIntent.putExtra("userDetails", userDetails);
                    startActivity(journeyIntent);
                }
            }
        });

        ((ImageView) findViewById(R.id.sos)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasOnGoingJourney) {
                    Toast.makeText(NotificationsActivity.this, getString(R.string.welcome_pressHoldInEmergency), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(NotificationsActivity.this, getString(R.string.welcome_noOngoingJourney), Toast.LENGTH_SHORT).show();
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
                            Toast toast = Toast.makeText(NotificationsActivity.this, getString(R.string.welcome_holdFor2Seconds), Toast.LENGTH_SHORT);
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
                                    Toast.makeText(NotificationsActivity.this, getString(R.string.welcome_guardianAlerted), Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(NotificationsActivity.this, getString(R.string.welcome_noOngoingJourney), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }.start();
                }
                return true;
            }
        });

        ((LinearLayout)findViewById(R.id.bck_arw_trans)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(NotificationsActivity.this, ProfileActivity.class);
                backIntent.putExtra("userDetails",userDetails);
                startActivity(backIntent);
            }
        });

        ((ImageView)findViewById(R.id.cntcts_icn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent friendsListIntent = new Intent(NotificationsActivity.this, FriendsListActivity.class);
                friendsListIntent.putExtra("userDetails",userDetails);
                startActivity(friendsListIntent);
            }
        });
        ((ImageView)findViewById(R.id.hme_icn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(NotificationsActivity.this, WelcomeActivity.class);
                homeIntent.putExtra("userDetails", userDetails);
                startActivity(homeIntent);
            }
        });
        ((Switch)findViewById(R.id.journeyInProgressGPSOn)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("NotificationsActivity","journeyInProgressGPSOn");
                if (isChecked){
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeyInProgressGPSOn").setValue("true");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeyInProgressGPSOn","true");
                    userDetails.setPreferences(editedUserDetails);
                }
                else{
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeyInProgressGPSOn").setValue("false");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeyInProgressGPSOn","false");
                    userDetails.setPreferences(editedUserDetails);
                }

            }
        });

        ((Switch)findViewById(R.id.journeyPaused)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("NotificationsActivity","journeyPaused");
                if (isChecked){
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeyPaused").setValue("true");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeyPaused","true");
                    userDetails.setPreferences(editedUserDetails);
                }
                else{
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeyPaused").setValue("false");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeyPaused","false");
                    userDetails.setPreferences(editedUserDetails);
                }

            }
        });

        ((Switch)findViewById(R.id.journeyResumed)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("NotificationsActivity","journeyResumed");
                if (isChecked){
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeyResumed").setValue("true");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeyResumed","true");
                    userDetails.setPreferences(editedUserDetails);
                }
                else{
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeyResumed").setValue("false");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeyResumed","false");
                    userDetails.setPreferences(editedUserDetails);
                }

            }
        });

        ((Switch)findViewById(R.id.journeySuccessful)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("NotificationsActivity","journeySuccessful");
                if (isChecked){
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeySuccessful").setValue("true");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeySuccessful","true");
                    userDetails.setPreferences(editedUserDetails);
                }
                else{
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeySuccessful").setValue("false");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeySuccessful","false");
                    userDetails.setPreferences(editedUserDetails);
                }

            }
        });

//        ((Switch)findViewById(R.id.journeyInProgressGPSOff)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                Log.i("NotificationsActivity","journeyInProgressGPSOff");
//                if (isChecked){
//                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeyInProgressGPSOff").setValue("true");
//                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
//                    editedUserDetails.put("journeyInProgressGPSOff","true");
//                    userDetails.setPreferences(editedUserDetails);
//                }
//                else{
//                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeyInProgressGPSOff").setValue("false");
//                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
//                    editedUserDetails.put("journeyInProgressGPSOff","false");
//                    userDetails.setPreferences(editedUserDetails);
//                }
//
//            }
//        });

        ((Switch)findViewById(R.id.journeyCheckpointAdded)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("NotificationsActivity","journeyCheckpointAdded");
                if (isChecked){
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeyCheckpointAdded").setValue("true");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeyCheckpointAdded","true");
                    userDetails.setPreferences(editedUserDetails);
                }
                else{
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeyCheckpointAdded").setValue("false");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeyCheckpointAdded","false");
                    userDetails.setPreferences(editedUserDetails);
                }

            }
        });

        ((Switch)findViewById(R.id.journeyStopped)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("NotificationsActivity","journeyStopped");
                if (isChecked){
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeyStopped").setValue("true");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeyStopped","true");
                    userDetails.setPreferences(editedUserDetails);
                }
                else{
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeyStopped").setValue("false");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeyStopped","false");
                    userDetails.setPreferences(editedUserDetails);
                }

            }
        });

        ((Switch)findViewById(R.id.journeyFinished)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("NotificationsActivity","journeyFinished");
                if (isChecked){
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeyFinished").setValue("true");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeyFinished","true");
                    userDetails.setPreferences(editedUserDetails);
                }
                else{
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeyFinished").setValue("false");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeyFinished","false");
                    userDetails.setPreferences(editedUserDetails);
                }

            }
        });

        ((Switch)findViewById(R.id.journeyRouteDeviated)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("NotificationsActivity","journeyRouteDeviated");
                if (isChecked){
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeyRouteDeviated").setValue("true");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeyRouteDeviated","true");
                    userDetails.setPreferences(editedUserDetails);
                }
                else{
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeyRouteDeviated").setValue("false");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeyRouteDeviated","false");
                    userDetails.setPreferences(editedUserDetails);
                }

            }
        });

            ((Switch)findViewById(R.id.distance25P)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("NotificationsActivity","distance25P");
                if (isChecked){
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("distance25P").setValue("true");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("distance25P","true");
                    userDetails.setPreferences(editedUserDetails);
                }
                else{
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("distance25P").setValue("false");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("distance25P","false");
                    userDetails.setPreferences(editedUserDetails);
                }
            }
        });

        ((Switch)findViewById(R.id.distance50P)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("NotificationsActivity","distance50P");
                if (isChecked){
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("distance50P").setValue("true");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("distance50P","true");
                    userDetails.setPreferences(editedUserDetails);
                }
                else{
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("distance50P").setValue("false");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("distance50P","false");
                    userDetails.setPreferences(editedUserDetails);
                }
            }
        });

        ((Switch)findViewById(R.id.distance75P)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("NotificationsActivity","distance75P");
                if (isChecked){
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("distance75P").setValue("true");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("distance75P","true");
                    userDetails.setPreferences(editedUserDetails);
                }
                else{
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("distance75P").setValue("false");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("distance75P","false");
                    userDetails.setPreferences(editedUserDetails);
                }
            }
        });

        ((Switch)findViewById(R.id.journeyETA)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("NotificationsActivity","journeyETAalert");
                if (isChecked){
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeyETAalert").setValue("true");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeyETAalert","true");
                    userDetails.setPreferences(editedUserDetails);
                }
                else{
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeyETAalert").setValue("false");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeyETAalert","false");
                    userDetails.setPreferences(editedUserDetails);
                }

            }
        });

        ((Switch)findViewById(R.id.journeyGuardianAdded)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("NotificationsActivity","journeyGuardianAdded");
                if (isChecked){
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeyGuardianAdded").setValue("true");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeyGuardianAdded","true");
                    userDetails.setPreferences(editedUserDetails);
                }
                else{
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeyGuardianAdded").setValue("false");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeyGuardianAdded","false");
                    userDetails.setPreferences(editedUserDetails);
                }

            }
        });

        ((Switch)findViewById(R.id.journeySOS)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("NotificationsActivity","journeySOSalert");
                if (isChecked){
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeySOSalert").setValue("true");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeySOSalert","true");
                    userDetails.setPreferences(editedUserDetails);
                }
                else{
                    FirebaseDatabase.getInstance().getReference("users").child(userDetails.encodedEmail()).child("preferences").child("journeySOSalert").setValue("false");
                    HashMap<String,String> editedUserDetails = userDetails.getPreferences();
                    editedUserDetails.put("journeySOSalert","false");
                    userDetails.setPreferences(editedUserDetails);
                }

            }
        });

    }

}