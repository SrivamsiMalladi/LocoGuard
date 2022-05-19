package com.bug_apk.locoguard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class SelectGuardianActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference reference;
    RecyclerView recyclerView;
    ArrayList<user> list = new ArrayList<user>();
    ArrayList<String> friendsList = new ArrayList<>();

    DataSnapshot dataSnapshot;
    user userDetails;
    journey journey;
    ArrayList<String> guardianList = new ArrayList<String>();
    ArrayList<String> phoneContactsList = new ArrayList<>();
	long journeyDuration;
	float userEnteredETA;
    String number = new String();
    String code = new String();
	ProgressDialog progressDialog;
	ArrayList<String> friendNames = new ArrayList<>();
    DatabaseReference myRef = database.getReference("journeys");
    private EditText editTextNumber;
    private EditText editTextCode;
    ArrayList<String> guardiansList = new ArrayList<>();
    LoadingDialog loadingDialog;
    DatabaseReference reference1;
    SelectGuardianRecyclerViewAdapter adapter;
    boolean phoneContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_contacts);
        Intent i = getIntent();

        userDetails = i.getParcelableExtra("userDetails");
        journey = i.getParcelableExtra("userJourney");
        journeyDuration = i.getLongExtra("journeyDuration",0);
        userEnteredETA = i.getFloatExtra("userEnteredETA",0);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_select_guardian);
        recyclerView.setLayoutManager( new LinearLayoutManager(SelectGuardianActivity.this));
        adapter = new SelectGuardianRecyclerViewAdapter(SelectGuardianActivity.this, friendsList, phoneContactsList, friendNames, userDetails.encodedEmail());
        phoneContacts = false;
        editTextNumber = findViewById(R.id.editTextNumber);
        editTextCode = findViewById(R.id.editTextCode);

        editTextNumber.setVisibility(editTextNumber.INVISIBLE);
        editTextCode.setVisibility(editTextCode.INVISIBLE);
        reference1 = FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.encodedEmail());
        if(loadingDialog!=null)
            if(loadingDialog.isDialogShowing())
                loadingDialog.dismissDialog();


        ImageButton imageButton = (ImageButton)findViewById(R.id.enterNumber);
        ImageButton imageButton1 = (ImageButton)findViewById(R.id.numberentered);

        imageButton1.setVisibility(imageButton1.INVISIBLE);
        imageButton.setVisibility(imageButton.INVISIBLE);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED) {
            imageButton.setVisibility(imageButton.VISIBLE);
        }

        imageButton.setOnClickListener(new View.OnClickListener() {
            boolean visible;
            @Override
            public void onClick(View v) {
                String[] permissions = new String[]{"0", Manifest.permission.SEND_SMS};
                ActivityCompat.requestPermissions(SelectGuardianActivity.this, permissions, PackageManager.PERMISSION_GRANTED);
                visible = !visible;
                editTextNumber.setVisibility(visible ? View.VISIBLE: View.GONE);
                editTextCode.setVisibility(visible ? View.VISIBLE: View.GONE);
                imageButton1.setVisibility(visible ? View.VISIBLE: View.GONE);
            }
        });


        imageButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = editTextCode.getText().toString();
                number = editTextNumber.getText().toString();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getRootView().getWindowToken(), 0);
                reference1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        journey journey = dataSnapshot.getValue(com.bug_apk.locoguard.journey.class);
                        if(dataSnapshot.exists())
                        if (journey.getPhoneContacts() != null)
                            phoneContactsList = journey.getPhoneContacts();
                        if(code.charAt(0) == '+' || code.charAt(0) == '0') {
                            if (number != null && code != null && number.length() >= 10 && code.length() == 3) {
                                phoneContactsList.add(code + number);
                                friendNames.add(code + number);
                                adapter = new SelectGuardianRecyclerViewAdapter(SelectGuardianActivity.this, friendsList, phoneContactsList, friendNames, userDetails.encodedEmail());
                                recyclerView.setAdapter(adapter);
                                recyclerView.setAlpha(1);
                                ((TextView) findViewById(R.id.noFriendsError)).setVisibility(View.INVISIBLE);
                                if(journey!=null) {
                                    journey.setPhoneContacts(phoneContactsList);
                                }
                                reference1.child("phoneContacts").setValue(phoneContactsList);
                                ArrayList<String> contacts = new ArrayList<>();
                                if(journey != null) {
                                    if (journey.getContactNames() != null)
                                        contacts = journey.getContactNames();
                                }
                                contacts.add(code + number);
                                reference1.child("contactNames").setValue(contacts);
                                editTextNumber.setText("");
                            } else {
                                Toast.makeText(SelectGuardianActivity.this, "Enter valid Number", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(SelectGuardianActivity.this, "Enter valid Code", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                adapter.notifyDataSetChanged();
            }
        });


        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                journey journey = dataSnapshot.getValue(journey.class);
                if(journey!=null)
                    if(journey.getContactNames()!=null)
                        phoneContactsList.addAll(journey.getContactNames());
                    if(recyclerView.getVisibility() == View.INVISIBLE)
                        recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reference = FirebaseDatabase.getInstance().getReference().child("users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user user = dataSnapshot.child(userDetails.encodedEmail()).getValue(user.class);
                if(user.getFriends()!=null || !phoneContactsList.isEmpty()) {
                    if(user.getFriends()!=null)
                      if (!(user.getFriends().isEmpty())) {
                        friendsList.addAll(user.getFriends());
                        friendNames.addAll(friendsList);
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            user friend = snapshot.getValue(user.class);
                            for(int i = 0; i<friendsList.size(); i++) {
                                if(friend.encodedEmail().equals(friendsList.get(i)))
                                    friendNames.set(i, friend.getName());
                            }

                        }
                        friendNames.addAll(phoneContactsList);
                    }
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    if(loadingDialog!=null)
                        loadingDialog.dismissDialog();
                }
                else {
                    Log.i("Entered Else Case:","No Friends or Phone Contacts");
                    recyclerView.setAlpha(0);
                    ((TextView) findViewById(R.id.noFriendsError)).setText(getString(R.string.selectGuardian_noFriends));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SelectGuardianActivity.this, getString(R.string.selectGuardian_somethingWentWrong), Toast.LENGTH_SHORT).show();
            }
        });


        ((ImageButton) findViewById(R.id.addPhoneContact)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] permissions = new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS};
                int[] grantResults = new int[]{PackageManager.PERMISSION_GRANTED, PackageManager.PERMISSION_DENIED};
                ActivityCompat.requestPermissions(SelectGuardianActivity.this,permissions, PackageManager.PERMISSION_GRANTED);
//                onRequestPermissionsResult(PackageManager.PERMISSION_GRANTED, permissions,grantResults);
                phoneContacts = true;

                }


        });

        ((ImageView) findViewById(R.id.backButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent guardIntent = new Intent(SelectGuardianActivity.this, GuardActivity.class);
                guardIntent.putExtra("userDetails", userDetails);
                guardIntent.putExtra("userJourney", journey);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getRootView().getWindowToken(), 0);

                FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.encodedEmail()).child("guardiansList").removeValue();

                startActivity(guardIntent);
                finish();
            }
        });

        ((Button)findViewById(R.id.createJourney)).setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  myRef.child(userDetails.encodedEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
                      @Override
                      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                          if (dataSnapshot.exists() && ((dataSnapshot.getValue(journey.class).getGuardiansList())!=null || dataSnapshot.getValue(journey.class).getPhoneContacts()!=null)) {
                              journey wardJourney = dataSnapshot.getValue(journey.class);
                              if(wardJourney.getGuardiansList()!=null) {
                                  guardianList = wardJourney.getGuardiansList();
                                  journey.setGuardiansList(guardianList);
                              }

                              if(wardJourney.getPhoneContacts()!=null) {
                                  phoneContactsList = wardJourney.getPhoneContacts();
                                  journey.setPhoneContacts(phoneContactsList);
                                  journey.setContactNames(wardJourney.getContactNames());
                              }

                              if(wardJourney.getPreviousJourneys()!=null)
                              {
                                  HashMap<String, journey> previousJourneys = wardJourney.getPreviousJourneys();
                                  journey.setPreviousJourneys(previousJourneys);
                              }
                              Calendar calendar = Calendar.getInstance();
                              calendar.add(Calendar.SECOND, (int) (journeyDuration + userEnteredETA*60));
                              Date currentTime = calendar.getTime();

                              SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
                              String dateTime = dateFormat.format(currentTime);
                              Log.i("GuardActivity", "Current Date Time : " + dateTime);

                              journey.setETA(dateTime);

                              createJourney();
							  SharedPreferences.Editor editor = getSharedPreferences("LocoGuard", MODE_PRIVATE).edit();
                              editor.putBoolean("startedJourney", true);
                              editor.commit();

                              Log.i("SelectGuardianActivity","startedJourney + " + userDetails.encodedEmail() + ":" + getSharedPreferences("LocoGuard", MODE_PRIVATE).getBoolean("startedJourney", false));

                              boolean userLoggedIn =  getSharedPreferences("LocoGuard", MODE_PRIVATE).getBoolean("staySignedIn", false);
                              if(!userLoggedIn) {
                                  editor.putBoolean("staySignedInChanged", true);
                                  editor.putBoolean("staySignedIn", true);
                                  editor.commit();
                              }

                              Intent intent = new Intent(SelectGuardianActivity.this, JourneyActivity.class);
                              Log.i("SelectContactsActivity", "Source name: " + journey.getSourceName());
                              Log.i("SelectContactsActivity", "Destination name: " + journey.getDestinationName());
                              intent.putExtra("userDetails", userDetails);
                              intent.putExtra("userJourney", journey);
                              startActivity(intent);
                          }
                          else {
                              ((TextView) findViewById(R.id.friendsError)).setText(getString(R.string.selectGuardian_selectAtleastOneGuardian));
                          }
                      }


                      @Override
                      public void onCancelled(@NonNull DatabaseError databaseError) {

                      }
                  });
              }
            });


        LocalBroadcastManager.getInstance(this).registerReceiver(new NoInternetHelper(this).noInternetBroadcastCallback,
                new IntentFilter("InternetStatusBroadcast"));
        new NoInternetHelper(this).checkInternet();
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if(shouldShowRequestPermissionRationale(permissions[0])) {
                Toast.makeText(SelectGuardianActivity.this, R.string.phoneContacts_permission, Toast.LENGTH_SHORT).show();
            }

            else if(shouldShowRequestPermissionRationale(permissions[1])) {
                Toast.makeText(SelectGuardianActivity.this, R.string.sms_permission, Toast.LENGTH_SHORT).show();
            }

        else if(phoneContacts)  {
            Intent phoneContactsIntent = new Intent(SelectGuardianActivity.this, PhoneContactsActivity.class);
            phoneContactsIntent.putExtra("userDetails", userDetails);
            phoneContactsIntent.putExtra("userJourney", journey);
            phoneContactsIntent.putExtra("journeyDuration", journeyDuration);
            startActivity(phoneContactsIntent);
            loadingDialog = new LoadingDialog(SelectGuardianActivity.this);
            loadingDialog.startLoadingDialog();
        }
    }


    @Override
    public void onBackPressed() {
        FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.encodedEmail()).child("guardiansList").removeValue();

        Intent guardIntent = new Intent(SelectGuardianActivity.this, GuardActivity.class);
        guardIntent.putExtra("userDetails", userDetails);
        startActivity(guardIntent);
        super.onBackPressed();
    }

    private void createJourney() {
        myRef.child(journey.getWard()).setValue(journey);
        if(journey.getGuardiansList()!=null)
        for(String guardian:journey.getGuardiansList()) {
            database.getReference("users").child(guardian).child("ward").setValue(journey.getWard());
        }
    }
}
