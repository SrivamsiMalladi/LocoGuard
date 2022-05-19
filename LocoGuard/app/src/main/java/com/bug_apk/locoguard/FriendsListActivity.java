package com.bug_apk.locoguard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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

public class FriendsListActivity extends AppCompatActivity {

    DatabaseReference reference;
    RecyclerView recyclerView;
    ArrayList<user> list = new ArrayList<user>();
    ArrayList<String> friendsList = new ArrayList<>();
    FriendsListRecyclerViewAdapter adapter;
    DataSnapshot dataSnapshot;
    boolean toastCancel = true;

    boolean hasWard = false;
    boolean hasOnGoingJourney = false;

    user userDetails;
    journey journeyDetails;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_friends_list);
        userDetails = getIntent().getParcelableExtra("userDetails");
        journey journey = getIntent().getParcelableExtra("userJourney");
        boolean journeyActivity = getIntent().getBooleanExtra("Ongoing",false);
        String email = userDetails.encodedEmail();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_friends_list);
        recyclerView.setLayoutManager( new LinearLayoutManager(this));


        adapter = new FriendsListRecyclerViewAdapter(FriendsListActivity.this,friendsList,email);
        Log.i("FriendsList Activity",email);

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
                                            Intent journeyIntent = new Intent(FriendsListActivity.this, TrackJourneyActivity.class);
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
                                        Intent journeyIntent = new Intent(FriendsListActivity.this, JourneyActivity.class);
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
                if (hasWard) {
                    Intent journeyIntent = new Intent(FriendsListActivity.this, TrackJourneyActivity.class);
                    journeyIntent.putExtra("userDetails", userDetails);
                    journeyIntent.putExtra("userJourney", journeyDetails);
                    startActivity(journeyIntent);
                } else if (hasOnGoingJourney) {
                    Intent journeyIntent = new Intent(FriendsListActivity.this, JourneyActivity.class);
                    journeyIntent.putExtra("userDetails", userDetails);
                    journeyIntent.putExtra("userJourney", journeyDetails);
                    startActivity(journeyIntent);
                } else {
                    Intent journeyIntent = new Intent(FriendsListActivity.this, GuardActivity.class);
                    journeyIntent.putExtra("userDetails", userDetails);
                    startActivity(journeyIntent);
                }
            }
        });

//        ((EditText)findViewById(R.id.search)).addTextChangedListener(new TextWatcher() {
////            @Override
////            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
////
////            }
////
////            @Override
////            public void onTextChanged(CharSequence s, int start, int before, int count) {
////
////            }
////
////            @Override
////            public void afterTextChanged(Editable s) {
////
////                ArrayList<user> filteredList = new ArrayList<>();
////
////                String filterpattern = ((EditText)findViewById(R.id.search)).getText().toString();
////
////                if (filterpattern == null) {
////                    filteredList.addAll(list);
////                } else {
////
////                    for (user users : list) {
////                        if (users.getName().toLowerCase().contains(filterpattern)) {
////                            filteredList.add(users);
////                        }
////                    }
////
////                }
////
////                adapter = new FriendsListRecyclerViewAdapter(FriendsListActivity.this);
////
////
////                recyclerView.setAdapter(adapter);
////
////
////
////            }
////        });

        reference = FirebaseDatabase.getInstance().getReference().child("users").child(email);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                user user = dataSnapshot.getValue(user.class);
                if(user.getFriends()!=null) {
                    friendsList.addAll(user.getFriends());
                    adapter.notifyDataSetChanged();
//                for(int i = 0; i<friendsList.size(); i++)
//                Log.i("FriendsListActivity", friendsList.get(i));
//                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren())
//                {
//                    ArrayList friends = dataSnapshot1.getValue(user.getFriends());
//                    friendsList.addAll(user.getFriends());
//                }
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(FriendsListActivity.this, getString(R.string.friendsList_somethingWentWrong), Toast.LENGTH_SHORT).show();
            }
        });



        ((ImageButton)findViewById(R.id.findButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(FriendsListActivity.this, ContactslistActivity.class);
                mainIntent.putExtra("userDetails", userDetails);
                mainIntent.putExtra("Ongoing", journeyActivity);
                startActivity(mainIntent);
            }
        });

        ((ImageView) findViewById(R.id.sos)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (journeyActivity) {
                    Toast.makeText(FriendsListActivity.this, getString(R.string.friendsList_pressHoldInEmergency), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(FriendsListActivity.this, getString(R.string.friendsList_noOngoingJourney), Toast.LENGTH_SHORT).show();
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
                            Toast toast = Toast.makeText(FriendsListActivity.this, getString(R.string.friendsList_holdFor2Seconds), Toast.LENGTH_SHORT);
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
                                    Toast.makeText(FriendsListActivity.this, getString(R.string.friendsList_guardianAlerted), Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(FriendsListActivity.this, getString(R.string.friendsList_noOngoingJourney), Toast.LENGTH_SHORT).show();
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
                Intent welcomeActivity = new Intent(FriendsListActivity.this, WelcomeActivity.class);
                welcomeActivity.putExtra("userDetails", userDetails);
                startActivity(welcomeActivity);
            }
        });


        ((ImageButton)findViewById(R.id.Home)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent welcomeActivity = new Intent(FriendsListActivity.this, WelcomeActivity.class);
                welcomeActivity.putExtra("userDetails", userDetails);
                startActivity(welcomeActivity);
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(new NoInternetHelper(this).noInternetBroadcastCallback,
                new IntentFilter("InternetStatusBroadcast"));
        new NoInternetHelper(this).checkInternet();

    }

}
