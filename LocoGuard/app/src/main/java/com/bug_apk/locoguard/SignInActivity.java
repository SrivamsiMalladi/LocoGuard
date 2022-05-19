package com.bug_apk.locoguard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;

public class SignInActivity extends AppCompatActivity {

    user userDetails = new user();

    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;

    boolean staySignedIn = false;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    String[] languages = new String[]{"EN", "DE"};
    String selectedLanguage = "en";

    Spinner dropdown;
    int mPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        SharedPreferences prefs = getSharedPreferences("LocoGuard", MODE_PRIVATE);
        selectedLanguage = prefs.getString("language", "en");
        Log.i("SignInActivity","User language preference:" + selectedLanguage);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            selectedLanguage = Locale.getDefault().getLanguage();
            mPosition = selectedLanguage.equals("de") ? 1 : 0;
            Log.i("SignInActivity", "Android N: Language changed to " + selectedLanguage);
        }

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        boolean signUpSuccessful = getIntent().getBooleanExtra("signUpSuccessful", false);
        boolean resetSuccessful = getIntent().getBooleanExtra("ResetPassword",false);

        sharedPref = this.getSharedPreferences("LocoGuard",MODE_PRIVATE);
        editor = sharedPref.edit();

        LoadingDialog loadingDialog = new LoadingDialog(SignInActivity.this);

        if(signUpSuccessful) {
            ((TextView)findViewById(R.id.authorization)).setText(getString(R.string.signIn_verifyEmail1) + "\n" +getString(R.string.signIn_verifyEmail2));
        }
        if(resetSuccessful) {
            ((TextView)findViewById(R.id.authorization)).setText(getString(R.string.signIn_resetPassword));
        }

        ((CheckBox) findViewById(R.id.signedInCheckBox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                staySignedIn = isChecked;
                Log.i("SignInActivity", "Stay signed in toggled to:" + isChecked);
            }
        });

        ImageView backButton = (ImageView) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(SignInActivity.this, IntroActivity.class);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getRootView().getWindowToken(), 0);
                startActivity(mainIntent);
            }
        });

        TextView forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resetIntent = new Intent(SignInActivity.this,ResetPasswordActivity.class);
                startActivity(resetIntent);
            }
        });

        LinearLayout signUp = (LinearLayout) findViewById(R.id.signInLinLayout);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            }
        });

        ((Button) findViewById(R.id.signIn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getRootView().getWindowToken(), 0);
                String emailEntered = ((EditText)findViewById(R.id.email)).getText().toString().toLowerCase();
                String passwordEntered = ((EditText) findViewById(R.id.password)).getText().toString();
                loadingDialog.startLoadingDialog();
                if(emailEntered.equals("")) {
                    loadingDialog.dismissDialog();
                    ((TextView)findViewById(R.id.signInAuthorization)).setText(getString(R.string.signIn_enterEmail));
                }
                else if(passwordEntered.equals("")) {
                    loadingDialog.dismissDialog();
                    ((TextView)findViewById(R.id.signInAuthorization)).setText(getString(R.string.signIn_enterPassword));
                }
                else {
                    mAuth.signInWithEmailAndPassword(emailEntered, passwordEntered).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("SignInActivity", "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                if(user.isEmailVerified()) {
                                    Log.i("SignInActivity", "User name: " + user.getDisplayName());
                                    userDetails.setName(user.getDisplayName());
                                    userDetails.setEmail(user.getEmail());

                                    firebaseDatabase.getReference("users").child(userDetails.encodedEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(!dataSnapshot.exists()) {
                                                HashMap<String,String> preferences = new HashMap<String, String>();
                                                preferences.put("journeyInProgressGPSOn","true");
                                                preferences.put("journeyPaused","true");
                                                preferences.put("journeyResumed","true");
                                                preferences.put("journeySuccessful","true");
                                                preferences.put("journeyInProgressGPSOff","true");
                                                preferences.put("journeyCheckpointAdded","true");
                                                preferences.put("journeyStopped","true");
                                                preferences.put("journeyFinished","true");
                                                preferences.put("journeyRouteDeviated","true");
                                                preferences.put("journeyETAalert","true");
                                                preferences.put("journeyGuardianAdded","true");
                                                preferences.put("journeySOSalert","true");
                                                preferences.put("distance25P","true");
                                                preferences.put("distance50P","true");
                                                preferences.put("distance75P","true");
                                                userDetails.setPreferences(preferences);
                                                firebaseDatabase.getReference("users").child(userDetails.encodedEmail()).setValue(userDetails);
                                            }
                                            else {
                                                userDetails = dataSnapshot.getValue(user.class);
                                            }
                                            Log.i("SignInActivity", "Checking staySignedIn:"+staySignedIn);
                                            if(staySignedIn) {
                                                editor.putBoolean("staySignedIn", true);
                                                editor.commit();
                                            }
                                            else {
                                                editor.putBoolean("staySignedIn", false);
                                                editor.commit();
                                            }

                                            SharedPreferences.Editor editor = getSharedPreferences("LocoGuard", MODE_PRIVATE).edit();
                                            editor.putBoolean("guardianAdded", false);
                                            editor.putBoolean("atDestination", false);
                                            editor.putBoolean("takingBreak", false);
                                            editor.putBoolean("journeyResumed", false);
                                            editor.putBoolean("distance25P", false);
                                            editor.putBoolean("distance50P", false);
                                            editor.putBoolean("distance75P", false);

                                            editor.commit();

                                            Intent signInIntent = new Intent(SignInActivity.this, WelcomeActivity.class);
                                            signInIntent.putExtra("userDetails", userDetails);
                                            loadingDialog.dismissDialog();
                                            startActivity(signInIntent);
                                            finish();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                                else {
                                    loadingDialog.dismissDialog();
                                    ((TextView)findViewById(R.id.signInAuthorization)).setText(getString(R.string.signIn_verifyEmailBeforeSignin));
                                }
                            } else {
                                loadingDialog.dismissDialog();
                                if(new NoInternetHelper(SignInActivity.this).isInternetConnected()) {
                                    // If sign in fails, display a message to the user.
                                    Log.w("SignInActivity", "signInWithEmail:failure", task.getException());
                                    ((TextView)findViewById(R.id.signInAuthorization)).setText(getString(R.string.signIn_invalidCredentials));
                                }
                                else {
                                    // If internet is not connected, display a message to the user.
                                    Log.w("SignInActivity", "signInWithEmail:failure, no internet", task.getException());
                                    ((TextView)findViewById(R.id.signInAuthorization)).setText(getString(R.string.signIn_noInternet));
                                }

                            }
                        }
                    });
                }
            }
        });

        Spinner dropdown = findViewById(R.id.languageSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.layout_language_spinner_light, languages);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);
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
                            Toast.makeText(SignInActivity.this, getString(R.string.toast_noLanguageSwitchingSupport), Toast.LENGTH_SHORT).show();
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
                        setLocale(SignInActivity.this, "en");
                    } else {
                        //German
                        selectedLanguage = "de";
                        Log.i("IntroActivity", "German selected");
                        setLocale(SignInActivity.this, "de");
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

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SignInActivity.this,IntroActivity.class));
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
