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
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bug_apk.locoguard.user;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    user userDetails = new user();
    String userPassword;

    String[] languages = new String[]{"EN", "DE"};
    String selectedLanguage = "en";

    Spinner dropdown;
    int mPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        SharedPreferences prefs = getSharedPreferences("LocoGuard", MODE_PRIVATE);
        selectedLanguage = prefs.getString("language", "en");
        Log.i("SignUpActivity","User language preference:" + selectedLanguage);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            selectedLanguage = Locale.getDefault().getLanguage();
            mPosition = selectedLanguage.equals("de") ? 1 : 0;
            Log.i("SignUpActivity", "Android N: Language changed to " + selectedLanguage);
        }

        mAuth = FirebaseAuth.getInstance();

        LoadingDialog loadingDialog = new LoadingDialog(SignUpActivity.this);

        ImageView backButton = (ImageView) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getRootView().getWindowToken(), 0);
                startActivity(new Intent(SignUpActivity.this, IntroActivity.class));
            }
        });

        LinearLayout signIn = (LinearLayout) findViewById(R.id.signInLinLayout);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            }
        });

        Button signUp = (Button) findViewById(R.id.signUp);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getRootView().getWindowToken(), 0);
                loadingDialog.startLoadingDialog();
                if(((EditText) findViewById(R.id.name)).getText().toString().isEmpty()) {
                    loadingDialog.dismissDialog();
                    ((TextView) findViewById(R.id.signUpValidator)).setText(getString(R.string.signUp_enterName));
                }
                else if(((EditText) findViewById(R.id.email)).getText().toString().isEmpty()) {
                    loadingDialog.dismissDialog();
                    ((TextView) findViewById(R.id.signUpValidator)).setText(getString(R.string.signUp_enterEmail));
                }
                else if(((EditText) findViewById(R.id.password)).getText().toString().isEmpty()) {
                    loadingDialog.dismissDialog();
                    ((TextView) findViewById(R.id.signUpValidator)).setText(getString(R.string.stignUp_enterPassword));
                }
                else {
                    userDetails.setName(((EditText) findViewById(R.id.name)).getText().toString());
                    userDetails.setEmail(((EditText) findViewById(R.id.email)).getText().toString().toLowerCase());
                    userPassword = ((EditText) findViewById(R.id.password)).getText().toString();
                    mAuth.createUserWithEmailAndPassword(userDetails.getEmail(), userPassword)
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("SignUpActivity", "createUserWithEmail:success");
                                        Log.i("SignUpActivity", "userDetails.name" + userDetails.getName());

                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(userDetails.getName())
                                                .build();
                                        Log.i("SignUpActivity", "Current user: " + mAuth.getCurrentUser().getEmail());
                                        mAuth.getCurrentUser().updateProfile(profileUpdates)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d("SignUpActivity", "User profile updated.");
                                                            mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()) {
                                                                        Log.i("SignUpActivity", "mAuth current user display name: " + mAuth.getCurrentUser().getDisplayName());

                                                                        loadingDialog.dismissDialog();
                                                                        Intent signUpIntent = new Intent(SignUpActivity.this, SignInActivity.class);
                                                                        signUpIntent.putExtra("signUpSuccessful", true);
                                                                        startActivity(signUpIntent);
                                                                    }
                                                                    else {
                                                                        loadingDialog.dismissDialog();
                                                                        ((TextView) findViewById(R.id.signUpValidator)).setText(getString(R.string.signUp_signUpError1) + "\n" +getString(R.string.signUp_signUpError2));
                                                                    }
                                                                }
                                                            });
                                                        }
                                                        else {
                                                            loadingDialog.dismissDialog();
                                                            Log.d("SignUpActivity", "Updating User profile failed.");
                                                        }
                                                    }
                                                });
                                    } else {
                                        loadingDialog.dismissDialog();
                                        // If sign in fails, display a message to the user.
                                        Log.w("SignUpActivity", "createUserWithEmail:failure" + task.getException().getClass());
                                        if (task.getException().getClass() == FirebaseAuthWeakPasswordException.class) {
                                            ((TextView) findViewById(R.id.signUpValidator)).setText(((FirebaseAuthWeakPasswordException) task.getException()).getReason());
                                        }
                                        else if (task.getException().getClass() == FirebaseAuthInvalidCredentialsException.class) {
                                            ((TextView) findViewById(R.id.signUpValidator)).setText(task.getException().getLocalizedMessage());
                                        }
                                        else if (task.getException().getClass() == FirebaseAuthUserCollisionException.class) {
                                            Log.i("SignUpActivity","Signup Error: " + task.getException().getLocalizedMessage());
                                            ((TextView) findViewById(R.id.signUpValidator)).setText(task.getException().getLocalizedMessage());
                                        }
                                        else if (task.getException().getClass() == FirebaseNetworkException.class) {
                                            Log.i("SignUpActivity","Signup Error: " + task.getException().getLocalizedMessage());
                                            ((TextView) findViewById(R.id.signUpValidator)).setText(getString(R.string.signUp_noInternet));
                                        }
                                    }
                                }
                            });
                }
            }
        });

        dropdown = findViewById(R.id.languageSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.layout_language_spinner, languages);
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
                            Log.i("SignUpActivity", "OnItemSelected called: API24 user wants to change language");
                            Toast.makeText(SignUpActivity.this, getString(R.string.toast_noLanguageSwitchingSupport), Toast.LENGTH_SHORT).show();
                        }
                    }
                    if ((dropdown.getTag().toString().equals(Integer.toString(position))) && (mPosition == position)) {
                        mPosition = (mPosition==0) ? 1 : 0;
                    }
                    Log.i("SignUpActivity", "Tag: " + dropdown.getTag().toString());
                    Log.i("SignUpActivity", "position: " + position);
                    Log.i("SignUpActivity", "mPosition: " + mPosition);
                    selectedLanguage = Locale.getDefault().getLanguage();
                    if(selectedLanguage.equals("de")) {
                        Log.i("SignUpActivity", "OnItemSelected called: Updating spinner to show " + selectedLanguage);
                        dropdown.setTag("1");
                        dropdown.setSelection(1);
                    }
                    else {
                        Log.i("SignUpActivity", "OnItemSelected called: Updating spinner to show " + selectedLanguage);
                        dropdown.setTag("0");
                        dropdown.setSelection(0);
                    }
                }
                else {
                    if (position == 0) {
                        //English
                        selectedLanguage = "en";
                        Log.i("SignUpActivity", "English selected");
                        setLocale(SignUpActivity.this, "en");
                    } else {
                        //German
                        selectedLanguage = "de";
                        Log.i("SignUpActivity", "German selected");
                        setLocale(SignUpActivity.this, "de");
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
