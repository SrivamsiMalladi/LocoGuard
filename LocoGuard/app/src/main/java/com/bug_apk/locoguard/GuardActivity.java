package com.bug_apk.locoguard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Image;
import android.media.audiofx.AcousticEchoCanceler;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
// Add an import statement for the client library.
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressComponents;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.PlusCode;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.security.Guard;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.google.android.libraries.places.api.Places.createClient;

public class GuardActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    String apiKey = "AIzaSyCcyeDCrnJQrLo1C-VrIawEZxrZt-oknyg";

    private FusedLocationProviderClient fusedLocationClient;
    LatLng currentLocation, destinationLocation, sourceLocation;
    Marker currentMarker, sourceMarker, destinationMarker;

    private SourceAutocompleteFragment sourceAutocompleteFragment = new SourceAutocompleteFragment();
    private DestinationAutocompleteFragment destinationAutocompleteFragment = new DestinationAutocompleteFragment();
    List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ID);

    //Execute Directions API request
    GeoApiContext geoApiContext = new GeoApiContext.Builder()
            .apiKey(apiKey)
            .build();

    EncodedPolyline sourceDestinationEncodedPolyline;
    Polyline sourceDestinationPolyline;

    journey journey = new journey();
    user userDetails;
    private Place destination;
    private Place source;
    private Long journeyDuration = 0L;
    private float userEnteredETA = 0;

    List<List<LatLng>> polylineList;
    List<Long> journeyDurationList;
    List<Polyline> sourceDestinationPolylineList;
    List<EncodedPolyline> sourceDestinationEncodedPolylineList;

    boolean routeSelected = false;
    boolean askedGPSPermission = false;

    ArrayList<String> modeOfTransport = new ArrayList<String>();
    String selectedModeOfTransport;
    TravelMode travelMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guard);

        userDetails = getIntent().getParcelableExtra("userDetails");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Places.initialize(getApplicationContext(), apiKey);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        LocalBroadcastManager.getInstance(GuardActivity.this).registerReceiver(sourceDestinationMessageReceiver,
                new IntentFilter("locationIDBroadcast"));

        journey.setGPSTracking("false");
        ((Switch) findViewById(R.id.GPSSwitch)).setChecked(false);

        modeOfTransport.add("driving");
        modeOfTransport.add("bicycling");
        modeOfTransport.add("walking");
        selectedModeOfTransport = modeOfTransport.get(0);

        ((ImageButton)findViewById(R.id.backButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(GuardActivity.this, WelcomeActivity.class);
                backIntent.putExtra("userDetails",userDetails);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getRootView().getWindowToken(), 0);
                startActivity(backIntent);
            }
        });

        ((ImageButton) findViewById(R.id.driving_light)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedModeOfTransport = modeOfTransport.get(0);
                drawRoute(TravelMode.DRIVING);
                ((ImageButton)findViewById(R.id.driving_fin)).setVisibility(View.VISIBLE);
                ((ImageButton)findViewById(R.id.driving_light)).setVisibility(View.GONE);
                ((ImageButton)findViewById(R.id.cycling_fin)).setVisibility(View.GONE);
                ((ImageButton)findViewById(R.id.cycling_light)).setVisibility(View.VISIBLE);
                ((ImageButton)findViewById(R.id.walking_fin)).setVisibility(View.GONE);
                ((ImageButton)findViewById(R.id.walking_light)).setVisibility(View.VISIBLE);
            }
        });

        ((ImageButton) findViewById(R.id.cycling_light)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedModeOfTransport = modeOfTransport.get(1);
                drawRoute(TravelMode.BICYCLING);
                ((ImageButton)findViewById(R.id.driving_fin)).setVisibility(View.GONE);
                ((ImageButton)findViewById(R.id.driving_light)).setVisibility(View.VISIBLE);
                ((ImageButton)findViewById(R.id.cycling_fin)).setVisibility(View.VISIBLE);
                ((ImageButton)findViewById(R.id.cycling_light)).setVisibility(View.GONE);
                ((ImageButton)findViewById(R.id.walking_fin)).setVisibility(View.GONE);
                ((ImageButton)findViewById(R.id.walking_light)).setVisibility(View.VISIBLE);
            }
        });

        ((ImageButton) findViewById(R.id.walking_light)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedModeOfTransport = modeOfTransport.get(2);
                drawRoute(TravelMode.WALKING);
                ((ImageButton)findViewById(R.id.driving_fin)).setVisibility(View.GONE);
                ((ImageButton)findViewById(R.id.driving_light)).setVisibility(View.VISIBLE);
                ((ImageButton)findViewById(R.id.cycling_fin)).setVisibility(View.GONE);
                ((ImageButton)findViewById(R.id.cycling_light)).setVisibility(View.VISIBLE);
                ((ImageButton)findViewById(R.id.walking_fin)).setVisibility(View.VISIBLE);
                ((ImageButton)findViewById(R.id.walking_light)).setVisibility(View.GONE);
            }
        });

        ((TextView) findViewById(R.id.source)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                String userSourceText = ((TextView) findViewById(R.id.source)).getText().toString();
                if(userSourceText.equals(getString(R.string.guard_enterSource)) || userSourceText.equals(getString(R.string.guard_yourLocation))) {
                    userSourceText="";
                }
                if(destinationAutocompleteFragment.isAdded()) {
                    ft.remove(destinationAutocompleteFragment);
                }
                sourceAutocompleteFragment = SourceAutocompleteFragment.newInstance(userSourceText);
                ft.add(R.id.sourceAutocompletePlaceholder, sourceAutocompleteFragment, "sourceAutocompleteFragment");
                ft.commit();
            }
        });
        ((TextView) findViewById(R.id.destination)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("GuardActivity","Destination clicked: "+((TextView) findViewById(R.id.source)).getText());
                if(((TextView) findViewById(R.id.source)).getText().toString().equals(getString(R.string.guard_enterSource))) {
                    Log.i("GuardActivity","Destination clicked: if case "+((TextView) findViewById(R.id.source)).getText());
                    ((TextView)findViewById(R.id.selectGuardiansError)).setText(getString(R.string.guard_validSource));
                }
                else {
                    Log.i("GuardActivity","Destination clicked: else case "+((TextView) findViewById(R.id.source)).getText());
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    String userDestinationText = ((TextView) findViewById(R.id.destination)).getText().toString();
                    if (userDestinationText.equals(getString(R.string.guard_enterDestination))) {
                        userDestinationText = "";
                    }
                    if (sourceAutocompleteFragment.isAdded()) {
                        ft.remove(sourceAutocompleteFragment);
                    }
                    destinationAutocompleteFragment = DestinationAutocompleteFragment.newInstance(userDestinationText);
                    ft.add(R.id.destinationAutocompletePlaceholder, destinationAutocompleteFragment, "destinationAutocompleteFragment");
                    ft.commit();
                }
            }
        });

        ((Button)findViewById(R.id.selectGuardians)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((((TextView)findViewById(R.id.source)).getText().toString().equals(getString(R.string.guard_enterSource)))) {
                    ((TextView)findViewById(R.id.selectGuardiansError)).setText(getString(R.string.guard_validSource));
                }
                else if ((((TextView)findViewById(R.id.destination)).getText().toString().equals(getString(R.string.guard_enterDestination)))) {
                    ((TextView)findViewById(R.id.selectGuardiansError)).setText(getString(R.string.guard_validDestination));
                }
                else if (!routeSelected) {
                    ((TextView)findViewById(R.id.selectGuardiansError)).setText(getString(R.string.guard_selectRoute));
                }
                else {
                    Intent intent = new Intent(GuardActivity.this,SelectGuardianActivity.class);
                    String ward = userDetails.encodedEmail();
                    journey.setWard(ward);
                    journey.setWardName(userDetails.getName());
                    journey.setSource(new com.bug_apk.locoguard.LatLng(sourceLocation.latitude,sourceLocation.longitude));
                    journey.setDestination(new com.bug_apk.locoguard.LatLng(destinationLocation.latitude,destinationLocation.longitude));
                    journey.setGPSTracking("" + ((Switch)findViewById(R.id.GPSSwitch)).isChecked());
                    if(journey.getGPSTracking().equals("true")) {
                        journey.setCurrentLocation(new com.bug_apk.locoguard.LatLng(currentLocation.latitude,currentLocation.longitude));
                    }
                    journey.setJourneyCompleted("false");
                    journey.setReachedDestination("false");
                    journey.setRouteDeviation("false");
                    journey.setSourceName(source.getName());
                    journey.setDestinationName(destination.getName());
                    journey.setSourceDestinationEncodedPolyline(sourceDestinationEncodedPolyline.toString().substring(18, sourceDestinationEncodedPolyline.toString().length()-1));
                    journey.setModeOfTransport(selectedModeOfTransport);

                    journey.setDistance25P("false");
                    journey.setDistance50P("false");
                    journey.setDistance75P("false");

                    Log.i("GuardActivity","Source name: "+journey.getSourceName());
                    Log.i("GuardActivity","Destination name: "+journey.getDestinationName());
                    intent.putExtra("userDetails", userDetails);
                    intent.putExtra("userJourney", journey);
                    intent.putExtra("journeyDuration", journeyDuration);
                    intent.putExtra("userEnteredETA", userEnteredETA);
                    startActivity(intent);
                }
            }
        });

        ((Switch) findViewById(R.id.GPSSwitch)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("GuardActivity", "GPS tracking toggled");
                if(((Switch) findViewById(R.id.GPSSwitch)).isChecked()) {
                    Log.i("GuardActivity", "GPS tracking toggled on");
                    mMap.clear();
                    sourceLocation=null;
                    destinationLocation=null;
                    sourceMarker=null;
                    destinationMarker=null;
                    ((TextView)findViewById(R.id.source)).setText(getString(R.string.guard_enterSource));
                    ((TextView)findViewById(R.id.destination)).setText(getString(R.string.guard_enterDestination));
                    ((TextView)findViewById(R.id.ETA)).setText("");
                    ((ImageButton)findViewById(R.id.AddETA)).setVisibility(View.GONE);
                    journeyDuration = 0L;
                    setUserLocation();
                }
                else {
                    Log.i("GuardActivity", "GPS tracking toggled off");
                    askedGPSPermission = false;
                    mMap.setMyLocationEnabled(false);
                    journey.setCurrentLocation(null);
                    sourceLocation=null;
                    sourceMarker=null;
                    destinationLocation=null;
                    destinationMarker=null;
                    mMap.clear();
                    ((TextView)findViewById(R.id.source)).setText(getString(R.string.guard_enterSource));
                    ((TextView)findViewById(R.id.destination)).setText(getString(R.string.guard_enterDestination));
                    ((TextView)findViewById(R.id.ETA)).setText("");
                    ((ImageButton)findViewById(R.id.AddETA)).setVisibility(View.GONE);
                    journeyDuration = 0L;
                }
            }
        });

        ((ImageButton) findViewById(R.id.AddETA)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((LinearLayout) findViewById(R.id.ETAInput)).getVisibility()==View.GONE) {
                    ((LinearLayout) findViewById(R.id.ETAInput)).setVisibility(View.VISIBLE);
                }
                else {
                    ((LinearLayout) findViewById(R.id.ETAInput)).setVisibility(View.GONE);
                }
            }
        });

        ((ImageButton) findViewById(R.id.ETAEntered)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((EditText) findViewById(R.id.userETAInput)).getText().toString().length()!=0) {
                    userEnteredETA = Float.parseFloat(((EditText) findViewById(R.id.userETAInput)).getText().toString());
                    ((TextView) findViewById(R.id.ETA)).setText(getString(R.string.guard_eta) + " " + String.format("%.2f", (journeyDuration / 60.0) + userEnteredETA) + " min");
                    ((LinearLayout) findViewById(R.id.ETAInput)).setVisibility(View.GONE);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getRootView().getWindowToken(), 0);
                }
            }
        });

        ((EditText) findViewById(R.id.userETAInput)).setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    userEnteredETA = Float.parseFloat(((EditText) findViewById(R.id.userETAInput)).getText().toString());
                    ((TextView) findViewById(R.id.ETA)).setText(getString(R.string.guard_eta) + " " + String.format("%.2f", (journeyDuration / 60.0) + userEnteredETA) + " min");
                    ((LinearLayout) findViewById(R.id.ETAInput)).setVisibility(View.GONE);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getRootView().getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(new NoInternetHelper(this).noInternetBroadcastCallback,
                new IntentFilter("InternetStatusBroadcast"));
        new NoInternetHelper(this).checkInternet();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMinZoomPreference(0.0f);
        mMap.setMaxZoomPreference(21.0f);

        mMap.moveCamera(CameraUpdateFactory.zoomTo(17f));
//        //Uncomment if markers are set to draggable
//        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
//            @Override
//            public void onMarkerDragStart(Marker marker) {
//
//            }
//
//            @Override
//            public void onMarkerDrag(Marker marker) {
//
//            }
//
//            @Override
//            public void onMarkerDragEnd(Marker marker) {
//                if(marker.getTitle().equals("Source")) {
//                    sourceLocation = marker.getPosition();
//                    if(sourceMarker!=null) {
//                        sourceMarker.remove();
//                    }
//                    sourceMarker = mMap.addMarker(new MarkerOptions().position(sourceLocation).title("Source").icon(BitmapDescriptorFactory.fromResource(R.drawable.a_finn)));
//
//                    Log.i("GuardActivity", "id: " + sourceMarker.getId());
//                    FetchPlaceRequest request = FetchPlaceRequest.newInstance(sourceMarker.getId(), placeFields);
//                    PlacesClient placesClient =  createClient(GuardActivity.this);
//
//                    placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
//                        ((TextView)findViewById(R.id.source)).setText(response.getPlace().getName());
//                    }).addOnFailureListener((exception) -> {
//                        if (exception instanceof ApiException) {
//                            ApiException apiException = (ApiException) exception;
//                            int statusCode = apiException.getStatusCode();
//                            // Handle error with given status code.
//                            Log.e("GuardActivity", "Place not found: " + exception.getMessage());
//                        }
//                    });
//                }
//                else if(marker.getTitle().equals("Destination")) {
//                    destinationLocation = marker.getPosition();
//                    if(destinationMarker!=null) {
//                        destinationMarker.remove();
//                    }
//                    destinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLocation).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.b_amazon)));
//
//                    FetchPlaceRequest request = FetchPlaceRequest.newInstance(destinationMarker.getId(), placeFields);
//                    PlacesClient placesClient =  createClient(GuardActivity.this);
//
//                    placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
//                        ((TextView)findViewById(R.id.destination)).setText(response.getPlace().getName());
//                    }).addOnFailureListener((exception) -> {
//                        if (exception instanceof ApiException) {
//                            ApiException apiException = (ApiException) exception;
//                            int statusCode = apiException.getStatusCode();
//                            // Handle error with given status code.
//                            Log.e("GuardActivity", "Place not found: " + exception.getMessage());
//                        }
//                    });
//                }
//            }
//        });

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                for(int i = 0; i< sourceDestinationPolylineList.size(); i++) {
                    if(sourceDestinationPolylineList.get(i).equals(polyline)) {
                        routeSelected = true;
                        sourceDestinationPolylineList.get(i).setColor(0xFF2D6A53);
                        sourceDestinationPolylineList.get(i).setWidth(12);
                        sourceDestinationPolylineList.get(i).setZIndex(1);
                        sourceDestinationPolyline = sourceDestinationPolylineList.get(i);
                        sourceDestinationEncodedPolyline = sourceDestinationEncodedPolylineList.get(i);
                        Log.i("GuardActivity","Selected route:" + sourceDestinationEncodedPolyline);
                        journeyDuration = journeyDurationList.get(i);
                        ((TextView) findViewById(R.id.ETA)).setText(getString(R.string.guard_eta) + " " + String.format("%.2f", (journeyDuration / 60.0) + userEnteredETA) + " min");
                        ((ImageButton) findViewById(R.id.AddETA)).setVisibility(View.VISIBLE);
                        ((LinearLayout) findViewById(R.id.ETAInput)).setVisibility(View.GONE);
                    }
                    else {
                        sourceDestinationPolylineList.get(i).setColor(0xFF6A2D57);
                        sourceDestinationPolylineList.get(i).setWidth(8);
                        sourceDestinationPolylineList.get(i).setZIndex(0);
                    }
                }
            }
        });
        if(journey.getGPSTracking().equals("true")) {
            setUserLocation();
        }
        else {
            LatLng tajMahal = new LatLng(27.1751, 78.0421);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(tajMahal));
        }
    }

    void setUserLocation() {
        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i("GuardActivity", "GPS Permission granted by user");
            ((Switch)findViewById(R.id.GPSSwitch)).setChecked(true);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                Log.i("GuardActivity", location.getLatitude() + " " + location.getLongitude());
                                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
//                                if(currentMarker!=null){
//                                    currentMarker.remove();
//                                }
//                                currentMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("User location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user_location)));
                                if(sourceMarker!=null) {
                                    sourceMarker.remove();
                                }
                                sourceMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("Source").icon(BitmapDescriptorFactory.fromResource(R.drawable.a_finn)));
                                ((TextView) findViewById(R.id.source)).setText(getString(R.string.guard_yourLocation));

                                Geocoder geocoder = new Geocoder(GuardActivity.this);
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(currentLocation.latitude, currentLocation.longitude, 1);
                                    Address obj = addresses.get(0);
                                    source = new Place() {
                                        @Nullable
                                        @Override
                                        public String getAddress() {
                                            return null;
                                        }

                                        @Nullable
                                        @Override
                                        public AddressComponents getAddressComponents() {
                                            return null;
                                        }

                                        @Nullable
                                        @Override
                                        public List<String> getAttributions() {
                                            return null;
                                        }

                                        @Nullable
                                        @Override
                                        public String getId() {
                                            return null;
                                        }

                                        @Nullable
                                        @Override
                                        public LatLng getLatLng() {
                                            return null;
                                        }

                                        @Nullable
                                        @Override
                                        public String getName() {
                                            return obj.getAddressLine(0).split(",")[0];
                                        }

                                        @Nullable
                                        @Override
                                        public OpeningHours getOpeningHours() {
                                            return null;
                                        }

                                        @Nullable
                                        @Override
                                        public String getPhoneNumber() {
                                            return null;
                                        }

                                        @Nullable
                                        @Override
                                        public List<PhotoMetadata> getPhotoMetadatas() {
                                            return null;
                                        }

                                        @Nullable
                                        @Override
                                        public PlusCode getPlusCode() {
                                            return null;
                                        }

                                        @Nullable
                                        @Override
                                        public Integer getPriceLevel() {
                                            return null;
                                        }

                                        @Nullable
                                        @Override
                                        public Double getRating() {
                                            return null;
                                        }

                                        @Nullable
                                        @Override
                                        public List<Type> getTypes() {
                                            return null;
                                        }

                                        @Nullable
                                        @Override
                                        public Integer getUserRatingsTotal() {
                                            return null;
                                        }

                                        @Nullable
                                        @Override
                                        public Integer getUtcOffsetMinutes() {
                                            return null;
                                        }

                                        @Nullable
                                        @Override
                                        public LatLngBounds getViewport() {
                                            return null;
                                        }

                                        @Nullable
                                        @Override
                                        public Uri getWebsiteUri() {
                                            return null;
                                        }

                                        @Override
                                        public int describeContents() {
                                            return 0;
                                        }

                                        @Override
                                        public void writeToParcel(Parcel dest, int flags) {

                                        }
                                    };
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                                sourceLocation = currentLocation;
                                mMap.setMyLocationEnabled(true);
                            }
                            else {
                                Log.i("GuardActivity","Error getting current location. Trying again...");
                                setUserLocation();
                            }
                        }
                    });
        } else if(!askedGPSPermission){
            Log.i("GuardActivity", "Asking user for GPS Permission");
            ((Switch)findViewById(R.id.GPSSwitch)).setChecked(false);
            // A local method to request required permissions;
            // See https://developer.android.com/training/permissions/requesting
            ActivityCompat.requestPermissions(GuardActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            ActivityCompat.requestPermissions(GuardActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    2);
            askedGPSPermission = true;
            setUserLocation();
        }
        else {
            Log.i("GuardActivity", "User denied the GPS Permission");
            ((Switch)findViewById(R.id.GPSSwitch)).setChecked(false);
            mMap.setMyLocationEnabled(false);
            askedGPSPermission = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1 || requestCode == 2) {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                Log.i("GuardActivity", "Permission granted");
                setUserLocation();
            }
        }
    }

    public BroadcastReceiver sourceDestinationMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String locationID = intent.getStringExtra("locationID");
//            Toast.makeText(GuardActivity.this,"ID: " + locationID,Toast.LENGTH_SHORT).show();

            FetchPlaceRequest request = FetchPlaceRequest.newInstance(locationID, placeFields);
            PlacesClient placesClient =  createClient(GuardActivity.this);

            placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                Place place = response.getPlace();
                Log.i("GuardActivity", "Place found: " + place.getName());

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Log.i("GuardActivity",""+ sourceAutocompleteFragment.isAdded());

                if(getCurrentFocus()==findViewById(R.id.sourceEditText)) {
                    ((ProgressBar)findViewById(R.id.sourceProgressBar)).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.source)).setText(place.getName());
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    ((TextView)findViewById(R.id.selectGuardiansError)).setText("");
                    imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getRootView().getWindowToken(), 0);
                    Log.i("GuardActivity","Removing autocomplete fragment");
                    ft.remove(sourceAutocompleteFragment);

                    sourceLocation = place.getLatLng();
                    source = place;
                    if(sourceMarker!=null) {
                        sourceMarker.remove();
                    }
                    sourceMarker = mMap.addMarker(new MarkerOptions().position(sourceLocation).title("Source").icon(BitmapDescriptorFactory.fromResource(R.drawable.a_finn)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(sourceLocation));
                }
//                else if (destinationAutocompleteFragment.isAdded()) {
                else if (getCurrentFocus()==findViewById(R.id.destinationEditText)) {
                    ft.remove(destinationAutocompleteFragment);
                    ((ProgressBar)findViewById(R.id.destinationProgressBar)).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.destination)).setText(place.getName());
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    ((TextView) findViewById(R.id.selectGuardiansError)).setText("");
                    imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getRootView().getWindowToken(), 0);
                    Log.i("GuardActivity", "Removing autocomplete fragment");


                    destinationLocation = place.getLatLng();
                    destination = place;
                    if (destinationMarker != null) {
                        destinationMarker.remove();
                    }

                    destinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLocation).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.b_amazon)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(destinationLocation));
//                    mMap.moveCamera(CameraUpdateFactory.zoomTo(12f));
                }
                //draw route
                if(selectedModeOfTransport.equals(modeOfTransport.get(0))) {
                    travelMode = TravelMode.DRIVING;
                }
                else if(selectedModeOfTransport.equals(modeOfTransport.get(1))) {
                    travelMode = TravelMode.BICYCLING;
                }
                else if(selectedModeOfTransport.equals(modeOfTransport.get(2))) {
                    travelMode = TravelMode.WALKING;
                }
                drawRoute(travelMode);
                ft.commitAllowingStateLoss();
                ((ProgressBar)findViewById(R.id.sourceProgressBar)).setVisibility(View.GONE);
                ((ProgressBar)findViewById(R.id.destinationProgressBar)).setVisibility(View.GONE);

            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();
                    // Handle error with given status code.
                    Log.e("GuardActivity", "Place not found: " + exception.getMessage());
                }
            });
        }
    };

    void getRoutes(TravelMode travelMode) {
        Log.i("GuardActivity", "getRoute called");
        DirectionsApiRequest req = DirectionsApi.getDirections(geoApiContext, sourceLocation.latitude + "," + sourceLocation.longitude, destinationLocation.latitude + "," + destinationLocation.longitude).alternatives(true).mode(travelMode);
        try {
            polylineList = new ArrayList<List<LatLng>>();
            journeyDurationList = new ArrayList<Long>();
            sourceDestinationPolylineList = new ArrayList<Polyline>();
            sourceDestinationEncodedPolylineList = new ArrayList<EncodedPolyline>();
            DirectionsResult res = req.await();
            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0) {
                Log.i("GuardActivity", "Number of routes: " + res.routes.length);
                for (DirectionsRoute route : res.routes) {
//                            DirectionsRoute route = res.routes[0];
                    sourceDestinationEncodedPolylineList.add(route.overviewPolyline);
                    String routePolylineString = route.overviewPolyline.toString();
                    Log.i("GuardActivity", "EncodedPolyline: " + routePolylineString);
                    Log.i("GuardActivity", "Sliced EncodedPolyline:" + routePolylineString.substring(18, routePolylineString.length() - 1));
                    //"shg}Hal`fAdEEr@Ch@C~@E?M@_@FkBDiALyD\\WLWBGDW`Ai_@HHLCFQ@WE]|DKCrB"
                    List<LatLng> path = new ArrayList();
                    List<com.google.maps.model.LatLng> coords1 = route.overviewPolyline.decodePath();
                    for (com.google.maps.model.LatLng coord1 : coords1) {
                        path.add(new LatLng(coord1.lat, coord1.lng));
                    }
                    polylineList.add(path);
                    if (route.legs != null) {
                        for (int i = 0; i < route.legs.length; i++) {
                            DirectionsLeg leg = route.legs[i];
                            Log.i("GuardActivity", "Leg duration: " + leg.duration.inSeconds);
                            journeyDuration = leg.duration.inSeconds;
                            journeyDurationList.add(journeyDuration);
//                                    if (leg.steps != null) {
//                                        for (int j=0; j<leg.steps.length;j++){
//                                            DirectionsStep step = leg.steps[j];
//                                            if (step.steps != null && step.steps.length >0) {
//                                                for (int k=0; k<step.steps.length;k++){
//                                                    DirectionsStep step1 = step.steps[k];
//                                                    EncodedPolyline points1 = step1.polyline;
//                                                    if (points1 != null) {
//                                                        //Decode polyline and add points to list of route coordinates
//                                                        List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
//                                                        for (com.google.maps.model.LatLng coord1 : coords1) {
//                                                            path.add(new LatLng(coord1.lat, coord1.lng));
//                                                        }
//                                                    }
//                                                }
//                                            } else {
//                                                EncodedPolyline points = step.polyline;
//                                                if (points != null) {
//                                                    //Decode polyline and add points to list of route coordinates
//                                                    List<com.google.maps.model.LatLng> coords = points.decodePath();
//                                                    for (com.google.maps.model.LatLng coord : coords) {
//                                                        path.add(new LatLng(coord.lat, coord.lng));
//                                                        Log.i("GuardActivity", coord.lat + " " + coord.lng);
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("GuardActivity", ex.getLocalizedMessage());
        }
    }

    void drawRoute(TravelMode travelMode) {
        if((sourceLocation!=null) && (destinationLocation!=null)) {
            Log.i("GuardActivity", "sourceLocation and destinationLocation not null");
            if (sourceDestinationPolylineList != null) {
                if (sourceDestinationPolylineList.size() > 0) {
                    for (Polyline sourceDestinationPolyline1 : sourceDestinationPolylineList) {
                        sourceDestinationPolyline1.remove();
                    }
                    Log.i("GuardActivity","Removing old polylines");
                    if(sourceDestinationPolyline!=null) {
                        sourceDestinationPolyline.remove();
                    }
                }
                else if(sourceDestinationPolyline != null) {
                    Log.i("GuardActivity","Removing old polyline");
                    sourceDestinationPolyline.remove();
                }
            }
            else if(sourceDestinationPolyline != null) {
                Log.i("GuardActivity","Removing old polyline");
                sourceDestinationPolyline.remove();
            }

            getRoutes(travelMode);

            //Draw the polyline
            if (polylineList.size() > 1) {
                routeSelected = false;
                Log.i("GuardActivity", "Total Polylines: " + polylineList.size());
                ((TextView) findViewById(R.id.ETA)).setText(polylineList.size() + " " + getString(R.string.guard_routesFound));
                ((ImageButton) findViewById(R.id.AddETA)).setVisibility(View.GONE);
                ((LinearLayout) findViewById(R.id.ETAInput)).setVisibility(View.GONE);

                for (List<LatLng> polyline : polylineList) {
                    Log.i("GuardianActivity", "Adding a polyline to map");
                    PolylineOptions opts = new PolylineOptions().addAll(polyline).color(0xFF6A2D57).width(8);
                    Polyline sourceDestinationPolyline = mMap.addPolyline(opts);
                    sourceDestinationPolyline.setClickable(true);
                    sourceDestinationPolylineList.add(sourceDestinationPolyline);
                }
            } else if (polylineList.size() > 0) {
                routeSelected = true;
                PolylineOptions opts = new PolylineOptions().addAll(polylineList.get(0)).color(0xFF6A2D57).width(8);
                sourceDestinationPolyline = mMap.addPolyline(opts);
                journeyDuration = journeyDurationList.get(0);
                sourceDestinationEncodedPolyline = sourceDestinationEncodedPolylineList.get(0);
                ((TextView) findViewById(R.id.ETA)).setText(getString(R.string.guard_eta) + " " + String.format("%.2f", (journeyDuration / 60.0)) + " min");
                ((ImageButton) findViewById(R.id.AddETA)).setVisibility(View.VISIBLE);
                ((LinearLayout) findViewById(R.id.ETAInput)).setVisibility(View.GONE);

            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent backIntent = new Intent(GuardActivity.this, WelcomeActivity.class);
        backIntent.putExtra("userDetails",userDetails);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getRootView().getWindowToken(), 0);
        startActivity(backIntent);
    }
}