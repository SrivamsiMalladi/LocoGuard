package com.bug_apk.locoguard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.bug_apk.locoguard.App.CHANNEL_ID;

public class JourneyActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    String apiKey = "AIzaSyCcyeDCrnJQrLo1C-VrIawEZxrZt-oknyg";

//    private FusedLocationProviderClient fusedLocationClient;

    LatLng currentLocation, destinationLocation, sourceLocation;
    Marker currentMarker, sourceMarker, destinationMarker;
    ArrayList<Marker> breakMarkerList;
    ArrayList<Break> breakList;

    List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ID);

    user userDetails;
    journey journey;
    int count = 0;
    boolean toastCancel = true;

    GeoApiContext geoApiContext = new GeoApiContext.Builder()
            .apiKey(apiKey)
            .build();
    Polyline sourceDestinationPolyline;

//    private GeofencingClient geofencingClient;
//    private GeofenceHelper geofenceHelper;

//    private float GEOFENCE_RADIUS = 100f;

    private LocationTrackingService mService = null;
    private boolean mBound = false;

    private String destinationName;
    private String sourceName;

    public ArrayList<String> checkpointList;

    AlarmManager alarmMgr;
    Intent broadcastIntent;
    PendingIntent alarmIntent;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("JourneyActivity", "Connecting service");
            LocationTrackingService.LocalBinder binder = (LocationTrackingService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.requestLocationUpdates();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("JourneyActivity", "Disconnected service");

            mService = null;
            mBound = false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.journeyMap);
        mapFragment.getMapAsync(this);

//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Intent i = getIntent();
        userDetails = i.getParcelableExtra("userDetails");
        journey = i.getParcelableExtra("userJourney");
        sourceName = journey.getSourceName();
        destinationName = journey.getDestinationName();

        Log.i("JourneyActivity", "onCreate(): " + userDetails.getName());

        ((TextView) findViewById(R.id.eta)).setText(getString(R.string.journey_eta) + " " + journey.getETA());

        String message = userDetails.getName() + " " + getString(R.string.journey_wardStartedJourney); //Sending SMS
        if (journey.getPhoneContacts() != null)
            for (int j = 0; j < journey.getPhoneContacts().size(); j++) {

                String number = journey.getPhoneContacts().get(j);
                if(ContextCompat.checkSelfPermission(JourneyActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    SmsManager mySmsManager = SmsManager.getDefault();
                    mySmsManager.sendTextMessage(number, null, message, null, null);
                }
                else
                    ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_CONTACTS}, PackageManager.PERMISSION_GRANTED);
            }


        if (journey.getModeOfTransport().equals("driving")) {
            ((ImageButton) findViewById(R.id.modeOfTransport)).setBackground(getDrawable(R.drawable.driving_fin));
        } else if (journey.getModeOfTransport().equals("bicycling")) {
            ((ImageButton) findViewById(R.id.modeOfTransport)).setBackground(getDrawable(R.drawable.cycling_fin));
        } else if (journey.getModeOfTransport().equals("walking")) {
            ((ImageButton) findViewById(R.id.modeOfTransport)).setBackground(getDrawable(R.drawable.walking_fin));
        }


        sourceLocation = new LatLng(journey.getSource().getLatitude(), journey.getSource().getLongitude());
        destinationLocation = new LatLng(journey.getDestination().getLatitude(), journey.getDestination().getLongitude());
        if (journey.getGPSTracking().equals("true")) {
            currentLocation = new LatLng(journey.getCurrentLocation().getLatitude(), journey.getCurrentLocation().getLongitude());
        } else {
            ((ImageButton) findViewById(R.id.pausebutton)).setVisibility(View.GONE);
        }
        ((ImageButton) findViewById(R.id.pausebutton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ImageButton) findViewById(R.id.pausebutton)).setVisibility(View.GONE);
                ((ImageButton) findViewById(R.id.playbutton)).setVisibility(View.VISIBLE);
                Intent intent = new Intent("pauseJourneyBroadcast");
                LocalBroadcastManager.getInstance(v.getContext()).sendBroadcast(intent);
                Log.i("JourneyActivity", "clearing old break marker");
            }
        });
        ((ImageButton) findViewById(R.id.playbutton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ImageButton) findViewById(R.id.pausebutton)).setVisibility(View.VISIBLE);
                ((ImageButton) findViewById(R.id.playbutton)).setVisibility(View.GONE);
                Intent intent = new Intent("playJourneyBroadcast");
                LocalBroadcastManager.getInstance(v.getContext()).sendBroadcast(intent);
            }
        });

        ((TextView) findViewById(R.id.journeySource)).setText(sourceName);
        ((TextView) findViewById(R.id.journeyDestination)).setText(destinationName);

//        geofencingClient = LocationServices.getGeofencingClient(this);

        if (journey.getGPSTracking().equals("true")) {
            Intent serviceBindingIntent = new Intent(this, LocationTrackingService.class);
            serviceBindingIntent.putExtra("userDetails", userDetails);
            serviceBindingIntent.putExtra("journey", journey);
            bindService(serviceBindingIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            ((Button) findViewById(R.id.finishJourney)).setVisibility(View.VISIBLE);
            ((Button) findViewById(R.id.finishJourney)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.encodedEmail()).child("reachedDestination").setValue("true");
                    FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.encodedEmail()).child("journeyCompleted").setValue("true");
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("journeys").child(userDetails.encodedEmail());
                    journey.setJourneyCompleted("true");
                    journey.setReachedDestination("true");
                    Calendar calendar = Calendar.getInstance();
                    Date currentTime = calendar.getTime();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
                    String dateTime = dateFormat.format(currentTime);
                    myRef.child("journeyCompleted").setValue("true");
                    journey.setPreviousJourneys(null);
                    myRef.child("previousJourneys").child(dateTime.toString()).setValue(journey);
                    for(String guardian:journey.getGuardiansList()) {
                        FirebaseDatabase.getInstance().getReference("users").child(guardian).child("ward").removeValue();
                    }
                    FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.encodedEmail()).child("guardiansList").removeValue();
                    FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.encodedEmail()).child("phoneContacts").removeValue();
                    FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.encodedEmail()).child("contactNames").removeValue();
                    alarmMgr.cancel(alarmIntent);

                    SharedPreferences.Editor editor = getSharedPreferences("LocoGuard", MODE_PRIVATE).edit();
                    editor.putBoolean("startedJourney", false);

                    Intent welcomeIntent = new Intent(JourneyActivity.this, WelcomeActivity.class);
                    welcomeIntent.putExtra("userDetails", userDetails);
                    startActivity(welcomeIntent);
                }
            });
            alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            broadcastIntent = new Intent(this, WardETABroadcastReceiver.class);
            broadcastIntent.putExtra("ward", userDetails.getWard());
            alarmIntent = PendingIntent.getBroadcast(this, 0, broadcastIntent, 0);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");

            Date date = new Date();
            try {
                date = format.parse(journey.getETA());
                Log.i("GuardianAddedService", "Journey ETA: " + date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            alarmMgr.set(AlarmManager.RTC, date.getTime(), alarmIntent);
        }

        ((Button) findViewById(R.id.journeyAction)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (journey.getGPSTracking().equals("false")) {
                    FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.encodedEmail()).child("journeyCompleted").setValue("true");
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("journeys").child(userDetails.encodedEmail());
                    myRef.child("journeyCompleted").setValue("true");
                    Calendar calendar = Calendar.getInstance();
                    Date currentTime = calendar.getTime();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
                    String dateTime = dateFormat.format(currentTime);
                    myRef.child("journeyCompleted").setValue("true");
                    journey.setJourneyCompleted("true");
                    journey.setReachedDestination("false");
                    journey.setPreviousJourneys(null);
                    myRef.child("previousJourneys").child(dateTime.toString()).setValue(journey);
                    if(journey.getGuardiansList()!=null)
                    for(String guardian:journey.getGuardiansList()) {
                        database.getReference("users").child(guardian).child("ward").removeValue();
                    }
                    FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.encodedEmail()).child("guardiansList").removeValue();
                    FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.encodedEmail()).child("phoneContacts").removeValue();
                    FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.encodedEmail()).child("contactNames").removeValue();
                    alarmMgr.cancel(alarmIntent);
                    SharedPreferences.Editor editor = getSharedPreferences("LocoGuard", MODE_PRIVATE).edit();
                    editor.putBoolean("wardAtDestination", false);
                    editor.putBoolean("startedJourney", false);
                    editor.commit();
                }
                Intent intent = new Intent("stopJourneyBroadcast");
                intent.putExtra("stopJourney", true);
                LocalBroadcastManager.getInstance(v.getContext()).sendBroadcast(intent);

                ((Button) findViewById(R.id.finishJourney)).setVisibility(View.GONE);
                ((Button) findViewById(R.id.journeyAction)).setText(getString(R.string.journey_goHome));
                ((Button) findViewById(R.id.journeyAction)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent welcomeIntent = new Intent(JourneyActivity.this, WelcomeActivity.class);
                        welcomeIntent.putExtra("userDetails", userDetails);
                        startActivity(welcomeIntent);
                    }
                });
            }
        });

        ((ImageButton) findViewById(R.id.Guardianslist)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                FragmentManager fragmentManager = getSupportFragmentManager();
                ShowGuardiansFragment fragment = new ShowGuardiansFragment(userDetails.encodedEmail());
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.guardianslistfragment, fragment);
                fragmentTransaction.commit();
                if (count % 2 == 0)
                    fragmentTransaction.hide(fragment);
            }
        });


        ((ImageButton) findViewById(R.id.homebutton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(JourneyActivity.this, WelcomeActivity.class);
                intent.putExtra("Ongoing", true);
                intent.putExtra("userDetails", userDetails);
                startActivity(intent);

            }
        });

        ((Button) findViewById(R.id.sos)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(JourneyActivity.this, getString(R.string.journey_pressHoldInEmergency), Toast.LENGTH_SHORT).show();
            }
        });

        ((Button) findViewById(R.id.sos)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    new CountDownTimer(2000, 200) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            Toast toast = Toast.makeText(JourneyActivity.this, getString(R.string.journey_holdFor2Seconds), Toast.LENGTH_SHORT);
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
                                Toast.makeText(JourneyActivity.this, getString(R.string.journey_guardianAlerted), Toast.LENGTH_SHORT).show();
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("journeys");
                                ArrayList<String> guardiansList = journey.getGuardiansList();
                                reference.child(userDetails.encodedEmail()).child("sos").setValue(true);

                                String message = (journey.getWardName() + " " + getString(R.string.guardianAdded_wardInDanger));
                                if(journey.getPhoneContacts()!= null)
                                    for(int i=0;i<journey.getPhoneContacts().size();i++){

                                        String number = journey.getPhoneContacts().get(i);

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

        ((ImageButton) findViewById(R.id.Profile)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(JourneyActivity.this, FriendsListActivity.class);
                intent.putExtra("Ongoing", true);
                intent.putExtra("userDetails", userDetails);
                startActivity(intent);

            }
        });

        LocalBroadcastManager.getInstance(JourneyActivity.this).registerReceiver(doneActivity,
                new IntentFilter("doneJourneyBroadcast"));

        LocalBroadcastManager.getInstance(this).registerReceiver(new NoInternetHelper(this).noInternetBroadcastCallback,
                new IntentFilter("InternetStatusBroadcast"));
        new NoInternetHelper(this).checkInternet();
        breakMarkerList = new ArrayList<Marker>();

        FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.encodedEmail()).child("breakList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    GenericTypeIndicator<ArrayList<Break>> genericTypeIndicator = new GenericTypeIndicator<ArrayList<Break>>() {
                    };
                    breakList = dataSnapshot.getValue(genericTypeIndicator);
                    if (breakList.get(breakList.size() - 1).getBreakDuration() == null) {
                        ((ImageButton) findViewById(R.id.pausebutton)).setVisibility(View.GONE);
                        ((ImageButton) findViewById(R.id.playbutton)).setVisibility(View.VISIBLE);
                    }
                    Log.i("JourneyActivity", "clearing old break marker");
                    for (Marker breakMarker : breakMarkerList) {
                        if (breakMarker != null) {
                            breakMarker.remove();
                        }
                    }
                    FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.encodedEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String ETA = dataSnapshot.getValue(journey.getClass()).getETA();
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
                            Date date = new Date();
                            Calendar cal = Calendar.getInstance();
                            if (breakList.get(breakList.size() - 1).getBreakDuration() != null) {
                                try {
                                    date = format.parse(ETA);
                                    cal.setTime(date);
                                    cal.add(Calendar.SECOND, (int) (Double.parseDouble(breakList.get(breakList.size() - 1).getBreakDuration()) * 60));
                                    Log.i("GuardianAddedService", "Journey ETA: " + date);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.encodedEmail()).child("eta").setValue(format.format(cal.getTime()));
                                ((TextView) findViewById(R.id.eta)).setText(getString(R.string.journey_eta) + " " + format.format(cal.getTime()));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    for (Break break1 : breakList) {
                        Log.i("JourneyActivity", "adding break marker");
                        if (break1.getBreakDuration() != null) {
                            breakMarkerList.add(mMap.addMarker(new MarkerOptions().position(new LatLng(break1.getBreakLocation().getLatitude(), break1.getBreakLocation().getLongitude())).title(break1.getBreakDuration() + " min").icon(BitmapDescriptorFactory.fromResource(R.drawable.break_time_marker))));
                        } else {
                            breakMarkerList.add(mMap.addMarker(new MarkerOptions().position(new LatLng(break1.getBreakLocation().getLatitude(), break1.getBreakLocation().getLongitude())).title("Ongoing").icon(BitmapDescriptorFactory.fromResource(R.drawable.break_time_marker))));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(JourneyActivity.this, WelcomeActivity.class);
        intent.putExtra("Ongoing", true);
        intent.putExtra("userDetails", userDetails);
        startActivity(intent);
    }

    private BroadcastReceiver doneActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            com.bug_apk.locoguard.LatLng finishLocation = intent.getParcelableExtra("finishLocation");

            SharedPreferences.Editor editor = getSharedPreferences("LocoGuard", MODE_PRIVATE).edit();
            editor.putBoolean("startedJourney", false);

            if( getSharedPreferences("LocoGuard", MODE_PRIVATE).getBoolean("staySignedInChanged", false)) {
                editor.putBoolean("staySignedIn", false);
                editor.putBoolean("staySignedInChanged", false);
            }
            editor.commit();

            if(finishLocation!=null) {
                mMap.addMarker(new MarkerOptions().position(new LatLng(finishLocation.getLatitude(), finishLocation.getLongitude())).title("Stopped location").icon(BitmapDescriptorFactory.fromResource(R.drawable.finish_marker)));
            }
            ((Button)findViewById(R.id.journeyAction)).setText(getString(R.string.journey_goHome));
            ((Button)findViewById(R.id.journeyAction)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent welcomeIntent = new Intent(JourneyActivity.this, WelcomeActivity.class);
                    welcomeIntent.putExtra("userDetails", userDetails);
                    startActivity(welcomeIntent);
                }
            });
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMinZoomPreference(0.0f);
        mMap.setMaxZoomPreference(21.0f);

        mMap.moveCamera(CameraUpdateFactory.zoomTo(17f));
        if (journey.getGPSTracking().equals("true")) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(JourneyActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
                ActivityCompat.requestPermissions(JourneyActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        2);
                return;
            }
            mMap.setMyLocationEnabled(true);
        }

//        setUserLocation();
        setSourceDestination();
//        addGeofence(destinationLocation, GEOFENCE_RADIUS);

        //        geofenceHelper = new GeofenceHelper(this);
        if(journey.getGPSTracking().equals("false")) {
            FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.encodedEmail()).child("checkpointsList").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        GenericTypeIndicator<ArrayList<String>> genericTypeIndicator = new GenericTypeIndicator<ArrayList<String>>() {};
                        checkpointList = dataSnapshot.getValue(genericTypeIndicator);
                        for(String checkpoint: checkpointList) {
                            mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(checkpoint.split(",")[0]), Double.parseDouble(checkpoint.split(",")[1]))).title("Checkpoint").icon(BitmapDescriptorFactory.fromResource(R.drawable.checkpoint_marker)));
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.encodedEmail()).child("checkpointsList").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                GenericTypeIndicator<ArrayList<String>> genericTypeIndicator = new GenericTypeIndicator<ArrayList<String>>() {};
                                checkpointList = dataSnapshot.getValue(genericTypeIndicator);
                            }
                            else {
                                checkpointList = new ArrayList<String>();
                            }
                            checkpointList.add(latLng.latitude + "," + latLng.longitude);
                            FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.encodedEmail()).child("checkpointsList").setValue(checkpointList);
                            mMap.addMarker(new MarkerOptions().position(latLng).title("Checkpoint").icon(BitmapDescriptorFactory.fromResource(R.drawable.checkpoint_marker)));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });
        }

    }

//    private void addGeofence(LatLng location, float radius) {
//        Geofence geofence = geofenceHelper.getGeofence("destinationGeofence", location, radius, Geofence.GEOFENCE_TRANSITION_ENTER);
//        GeofencingRequest geofencingRequest = geofenceHelper.getGeofenceRequest(geofence);
//        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
//        Log.i("JourneyActivity", "Adding Geofence");
//        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.i("JourneyActivity", "Geofence added");
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.i("JourneyActivity", geofenceHelper.getErrorString(e));
//                    }
//                });
//    }

//    void setUserLocation() {
//        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
//        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//                || ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            fusedLocationClient.getLastLocation()
//                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                        @Override
//                        public void onSuccess(Location location) {
//                            // Got last known location. In some rare situations this can be null.
//                            if (location != null) {
//                                Log.i("JourneyActivity", "User location:" + location.getLatitude() + " " + location.getLongitude());
//                                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
//                                if(currentMarker!=null){
//                                    currentMarker.remove();
//                                }
////                                currentMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("User location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user_location)));
//                                mMap.addCircle(new CircleOptions().center(destinationLocation).radius(GEOFENCE_RADIUS).strokeColor(Color.argb(255, 255,0,0)).fillColor(Color.argb(64, 255,0,0)).strokeWidth(4));
//                                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
//                                sourceLocation = currentLocation;
//                            }
//                            else {
//                                Log.i("JourneyActivity","Error getting current location. Trying again...");
//                                setUserLocation();
//                            }
//                        }
//                    });
//        } else {
//            // A local method to request required permissions;
//            // See https://developer.android.com/training/permissions/requesting
//            ActivityCompat.requestPermissions(JourneyActivity.this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    0);
//            ActivityCompat.requestPermissions(JourneyActivity.this,
//                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
//                    0);
//        }
//    }

    private void setSourceDestination() {
        if(sourceMarker!=null){
            sourceMarker.remove();
        }
        destinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLocation).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.b_amazon)));
        sourceMarker = mMap.addMarker(new MarkerOptions().position(sourceLocation).title("Source").icon(BitmapDescriptorFactory.fromResource(R.drawable.a_finn)));
        drawRoute(sourceLocation, destinationLocation);
        if(journey.getGPSTracking().equals("true")) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        }
        else {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sourceLocation));
        }
//        mMap.addCircle(new CircleOptions().center(destinationLocation).radius(GEOFENCE_RADIUS).strokeColor(Color.argb(255, 255,0,0)).fillColor(Color.argb(64, 255,0,0)).strokeWidth(4));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
    }

    public void drawRoute(LatLng sourceLocation, LatLng destinationLocation) {
        List<LatLng> path = new ArrayList();
        if(sourceDestinationPolyline!=null) {
            sourceDestinationPolyline.remove();
        }
//        DirectionsApiRequest req = DirectionsApi.getDirections(geoApiContext, sourceLocation.latitude+","+sourceLocation.longitude, destinationLocation.latitude+","+destinationLocation.longitude);
//        try {
//            DirectionsResult res = req.await();
//            //Loop through legs and steps to get encoded polylines of each step
//            if (res.routes != null && res.routes.length > 0) {
//                DirectionsRoute route = res.routes[0];
//
//                if (route.legs !=null) {
//                    for(int i=0; i<route.legs.length; i++) {
//                        DirectionsLeg leg = route.legs[i];
//                        if (leg.steps != null) {
//                            for (int j=0; j<leg.steps.length;j++){
//                                DirectionsStep step = leg.steps[j];
//                                if (step.steps != null && step.steps.length >0) {
//                                    for (int k=0; k<step.steps.length;k++){
//                                        DirectionsStep step1 = step.steps[k];
//                                        EncodedPolyline points1 = step1.polyline;
//                                        if (points1 != null) {
//                                            //Decode polyline and add points to list of route coordinates
//                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
//                                            for (com.google.maps.model.LatLng coord1 : coords1) {
//                                                path.add(new LatLng(coord1.lat, coord1.lng));
//                                            }
//                                        }
//                                    }
//                                } else {
//                                    EncodedPolyline points = step.polyline;
//                                    if (points != null) {
//                                        //Decode polyline and add points to list of route coordinates
//                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
//                                        for (com.google.maps.model.LatLng coord : coords) {
//                                            path.add(new LatLng(coord.lat, coord.lng));
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        } catch(Exception ex) {
//            Log.e("GuardActivity", ex.getLocalizedMessage());
//        }

        EncodedPolyline sourceDestinationEncodedPolyline = new EncodedPolyline(journey.getSourceDestinationEncodedPolyline());
        List<com.google.maps.model.LatLng> coords1 = sourceDestinationEncodedPolyline.decodePath();
        for (com.google.maps.model.LatLng coord1 : coords1) {
            path.add(new LatLng(coord1.lat, coord1.lng));
        }

        //Draw the polyline
        if (path.size() > 0) {
            PolylineOptions opts = new PolylineOptions().addAll(path).color(0xff6a2d57).width(10);
            sourceDestinationPolyline = mMap.addPolyline(opts);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == 1 || requestCode == 2) {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                Log.i("GuardActivity", "Permission granted");
                mMap.setMyLocationEnabled(true);
                setSourceDestination();
            }
        }
    }
}
