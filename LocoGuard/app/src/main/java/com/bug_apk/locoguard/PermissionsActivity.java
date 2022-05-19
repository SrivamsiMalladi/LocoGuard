package com.bug_apk.locoguard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.URI;
import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.SEND_SMS;

public class PermissionsActivity extends AppCompatActivity {

    private int Location_Permission_Code = 1;
    private int Contact_Permission_Code = 2;
    private int SMS_Permission_Code = 3;

    boolean hasWard = false;
    boolean hasOnGoingJourney = false;
    boolean toastCancel = true;

    user userDetails;
    journey journeyDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            ((Switch)findViewById(R.id.permissionLocationSharing)).setChecked(true);
            ((LinearLayout)findViewById(R.id.locationUpdateFrequency)).setVisibility(View.VISIBLE);
        }

        if (ContextCompat.checkSelfPermission(this, READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            ((Switch)findViewById(R.id.permissionContactsSharing)).setChecked(true);
        }

        if (ContextCompat.checkSelfPermission(this, SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            ((Switch)findViewById(R.id.permissionSMS)).setChecked(true);
        }

        userDetails = getIntent().getParcelableExtra("userDetails");

        SharedPreferences sharedPreferences = getSharedPreferences("LocoGuard", MODE_PRIVATE);
        ((EditText) findViewById(R.id.locationUpdateFrequencyValue)).setText(sharedPreferences.getString("locationUpdateFrequency", "1"));

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
                                            Intent journeyIntent = new Intent(PermissionsActivity.this, TrackJourneyActivity.class);
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
                                        Intent journeyIntent = new Intent(PermissionsActivity.this, JourneyActivity.class);
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
                    Intent journeyIntent = new Intent(PermissionsActivity.this, TrackJourneyActivity.class);
                    journeyIntent.putExtra("userDetails", userDetails);
                    journeyIntent.putExtra("userJourney", journeyDetails);
                    startActivity(journeyIntent);
                }
                else if(hasOnGoingJourney) {
                    Intent journeyIntent = new Intent(PermissionsActivity.this, JourneyActivity.class);
                    journeyIntent.putExtra("userDetails", userDetails);
                    journeyIntent.putExtra("userJourney", journeyDetails);
                    startActivity(journeyIntent);
                }
                else {
                    Intent journeyIntent = new Intent(PermissionsActivity.this, GuardActivity.class);
                    journeyIntent.putExtra("userDetails", userDetails);
                    startActivity(journeyIntent);
                }
            }
        });

        ((ImageView) findViewById(R.id.sos)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PermissionsActivity.this, getString(R.string.journey_pressHoldInEmergency), Toast.LENGTH_SHORT).show();
            }
        });

        ((ImageView) findViewById(R.id.sos)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    new CountDownTimer(2000, 200) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            Toast toast = Toast.makeText(PermissionsActivity.this, getString(R.string.journey_holdFor2Seconds), Toast.LENGTH_SHORT);
                            if (event.getAction() == MotionEvent.ACTION_UP) {
                                if (toastCancel) {
                                    toast.show();
                                    toastCancel = false;
                                }

                            }
                        }

                        public void onFinish() {
                            toastCancel = true;
                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                Toast.makeText(PermissionsActivity.this, getString(R.string.journey_guardianAlerted), Toast.LENGTH_SHORT).show();
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
                Intent backIntent = new Intent(PermissionsActivity.this, ProfileActivity.class);
                backIntent.putExtra("userDetails",userDetails);
                startActivity(backIntent);
            }
        });

        ((Switch) findViewById(R.id.permissionLocationSharing)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("PermissionsActivity", "GPS tracking toggled");
                if(((Switch) findViewById(R.id.permissionLocationSharing)).isChecked()) {
                    Log.i("PermissionsActivity", "GPS tracking toggled on");
                    requestLocationPermission();
                }
                else {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package",getPackageName(),null);
                        intent.setData(uri);
                        startActivity(intent);
                }
            }
        });

        ((EditText) findViewById(R.id.locationUpdateFrequencyValue)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.i("PermissionsActivity", "Some key pressed on editText");
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    SharedPreferences.Editor editor = getSharedPreferences("LocoGuard", MODE_PRIVATE).edit();
                    editor.putString("locationUpdateFrequency", ((CustomEditText)findViewById(R.id.locationUpdateFrequencyValue)).getText().toString());
                    editor.apply();
                    ((EditText) findViewById(R.id.locationUpdateFrequencyValue)).clearFocus();

                    Toast.makeText(PermissionsActivity.this, R.string.profile_permissions_locationUpdateFrequency, Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });

        ((Switch) findViewById(R.id.permissionContactsSharing)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("PermissionsActivity", "Read Contacts toggled");
                if(((Switch) findViewById(R.id.permissionContactsSharing)).isChecked()) {
                    Log.i("PermissionsActivity", "Read Contacts toggled on");
                    requestContactsPermission();
                }
                else {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package",getPackageName(),null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }
        });

        ((Switch) findViewById(R.id.permissionSMS)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("PermissionsActivity", "SEND_SMS toggled");
                if(((Switch) findViewById(R.id.permissionSMS)).isChecked()) {
                    Log.i("PermissionsActivity", "SEND_SMS toggled on");
                    requestSMSPermission();
                }
                else {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package",getPackageName(),null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }
        });


        ((ImageView)findViewById(R.id.grd_icn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent guardIntent = new Intent(PermissionsActivity.this, GuardActivity.class);
                guardIntent.putExtra("userDetails", userDetails);
                startActivity(guardIntent);
            }
        });
        ((ImageView)findViewById(R.id.cntcts_icn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent friendsListIntent = new Intent(PermissionsActivity.this, FriendsListActivity.class);
                friendsListIntent.putExtra("userDetails",userDetails);
                startActivity(friendsListIntent);
            }
        });
        ((ImageView)findViewById(R.id.hme_icn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(PermissionsActivity.this, WelcomeActivity.class);
                homeIntent.putExtra("userDetails", userDetails);
                startActivity(homeIntent);
            }
        });
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, Location_Permission_Code);
    }

    private void requestContactsPermission() {
        ActivityCompat.requestPermissions(this,
                new String[] {Manifest.permission.READ_CONTACTS}, Contact_Permission_Code);
    }

    private void requestSMSPermission() {
        ActivityCompat.requestPermissions(this,
                new String[] {Manifest.permission.SEND_SMS}, SMS_Permission_Code);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == Location_Permission_Code) {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                Log.i("PermissionActivity", "Location Permission granted");
                ((LinearLayout)findViewById(R.id.locationUpdateFrequency)).setVisibility(View.VISIBLE);
            }
            else {
                ((Switch)findViewById(R.id.permissionLocationSharing)).setChecked(false);
                ((LinearLayout)findViewById(R.id.locationUpdateFrequency)).setVisibility(View.GONE);
            }
        }

        if(requestCode == Contact_Permission_Code) {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                Log.i("PermissionActivity", "Contact Permission granted");

            }
            else {
                ((Switch)findViewById(R.id.permissionContactsSharing)).setChecked(false);
            }
        }

        if(requestCode == SMS_Permission_Code) {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                Log.i("PermissionActivity", "SMS Permission granted");

            }
            else {
                ((Switch)findViewById(R.id.permissionSMS)).setChecked(false);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            ((Switch)findViewById(R.id.permissionLocationSharing)).setChecked(true);
        }

        if (ContextCompat.checkSelfPermission(this, READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            ((Switch)findViewById(R.id.permissionContactsSharing)).setChecked(true);
        }

        if (ContextCompat.checkSelfPermission(this, SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            ((Switch)findViewById(R.id.permissionSMS)).setChecked(true);
        }
    }
}