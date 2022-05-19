package com.bug_apk.locoguard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ContactslistActivity extends AppCompatActivity {

    DatabaseReference reference;
    RecyclerView recyclerView;
    ArrayList<user> list = new ArrayList<user>();
    ArrayList<user> friendsList = new ArrayList<user>();
    ContactslistRecyclerViewAdapter adapter;
    ArrayList<user> filteredList = new ArrayList<>();
    boolean toastCancel = true;

    boolean hasWard = false;
    boolean hasOnGoingJourney = false;

    user userDetails;
    journey journeyDetails;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactslist);
        userDetails = getIntent().getParcelableExtra("userDetails");
        boolean journeyActivity = getIntent().getBooleanExtra("Ongoing", false);
        String email = userDetails.encodedEmail();
        Log.i("ContactListActivity", email);


        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_contacts_list);
        recyclerView.setLayoutManager( new LinearLayoutManager(this));

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
                                    ((ImageView) findViewById(R.id.Guard)).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent journeyIntent = new Intent(ContactslistActivity.this, TrackJourneyActivity.class);
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
                                ((ImageView) findViewById(R.id.Guard)).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent journeyIntent = new Intent(ContactslistActivity.this, JourneyActivity.class);
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

        ((ImageView) findViewById(R.id.Guard)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasWard) {
                    Intent journeyIntent = new Intent(ContactslistActivity.this, TrackJourneyActivity.class);
                    journeyIntent.putExtra("userDetails", userDetails);
                    journeyIntent.putExtra("userJourney", journeyDetails);
                    startActivity(journeyIntent);
                }
                else if(hasOnGoingJourney) {
                    Intent journeyIntent = new Intent(ContactslistActivity.this, JourneyActivity.class);
                    journeyIntent.putExtra("userDetails", userDetails);
                    journeyIntent.putExtra("userJourney", journeyDetails);
                    startActivity(journeyIntent);
                }
                else {
                    Intent journeyIntent = new Intent(ContactslistActivity.this, GuardActivity.class);
                    journeyIntent.putExtra("userDetails", userDetails);
                    startActivity(journeyIntent);
                }
            }
        });

        ((EditText)findViewById(R.id.search)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String filterPattern = ((EditText) findViewById(R.id.search)).getText().toString();
                filteredList = new ArrayList<>();
                Log.i("TextChangedListener",filterPattern);
                if(filterPattern.equals("")) {
                    filteredList.addAll(list);
                }
                else {
                    for(user users : list) {
                        if (users.getName().toLowerCase().contains(filterPattern)) {
                                filteredList.add(users);
                            }
                    }
                }
                adapter = new ContactslistRecyclerViewAdapter(ContactslistActivity.this,filteredList,userDetails.encodedEmail());
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                }

        });

        reference = FirebaseDatabase.getInstance().getReference().child("users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
                {
                    user user = dataSnapshot1.getValue(user.class);
                    list.add(user);
                    if(user.encodedEmail().equals(userDetails.encodedEmail()))
                        list.remove(user);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ContactslistActivity.this, "Oopsss.... Something is wrong", Toast.LENGTH_SHORT).show();
            }
        });

        adapter = new ContactslistRecyclerViewAdapter(ContactslistActivity.this,list, email);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        ((ImageView) findViewById(R.id.sos)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (journeyActivity) {
                    Toast.makeText(ContactslistActivity.this, "Press and hold in case of emergency", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(ContactslistActivity.this, "No Ongoing Journey", Toast.LENGTH_SHORT).show();
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
                            Toast toast = Toast.makeText(ContactslistActivity.this, "Press and hold for 2 seconds", Toast.LENGTH_SHORT);
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
                                    Toast.makeText(ContactslistActivity.this, "Guardian Alerted!", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(ContactslistActivity.this, "No Ongoing Journey", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }.start();
                }
                return true;
            }
        });


        ((ImageButton)findViewById(R.id.backarrow)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(ContactslistActivity.this, FriendsListActivity.class);
                backIntent.putExtra("userDetails",userDetails);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getRootView().getWindowToken(), 0);
                startActivity(backIntent);
            }
        });


        ((ImageButton)findViewById(R.id.Home)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(ContactslistActivity.this, WelcomeActivity.class);
                homeIntent.putExtra("userDetails", userDetails);
                startActivity(homeIntent);
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(new NoInternetHelper(this).noInternetBroadcastCallback,
                new IntentFilter("InternetStatusBroadcast"));
        new NoInternetHelper(this).checkInternet();
    }


}

