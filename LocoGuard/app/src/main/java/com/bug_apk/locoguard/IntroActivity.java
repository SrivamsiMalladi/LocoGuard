package com.bug_apk.locoguard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class IntroActivity extends AppCompatActivity {

    String[] languages = new String[]{"EN", "DE"};
    String selectedLanguage = "en";
    Spinner dropdown;
    int mPosition = 0;
    boolean pressAgaintoExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        SharedPreferences prefs = getSharedPreferences("LocoGuard", MODE_PRIVATE);
        selectedLanguage = prefs.getString("language", "en");
        Log.i("IntroActivity", "Language from shared preference " + selectedLanguage);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            selectedLanguage = Locale.getDefault().getLanguage();
            mPosition = selectedLanguage.equals("de") ? 1 : 0;
            Log.i("IntroActivity", "Android N: Language changed to " + selectedLanguage);
        }

//        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("EXIT", false)) {
//            finishAndRemoveTask();
//        }
        Button singIn = (Button)findViewById(R.id.mainSignIn);
        singIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(IntroActivity.this, SignInActivity.class));
            }
        });

        final LinearLayout signUp = (LinearLayout)findViewById(R.id.signUp);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(IntroActivity.this, SignUpActivity.class);
                startActivity(signUpIntent);
            }
        });

        dropdown = findViewById(R.id.languageSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.layout_language_spinner, languages);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);
        if(selectedLanguage.equals("de")) {
            Log.i("IntroActivity", "Updating spinner to show " + selectedLanguage);
            dropdown.setTag("1");
            dropdown.setSelection(1);
        }
        else {
            Log.i("IntroActivity", "Updating spinner to show " + selectedLanguage);
            dropdown.setTag("0");
            dropdown.setSelection(0);
        }

        ((Spinner)findViewById(R.id.languageSpinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("IntroActivity", "OnItemSelected called for selecting " + selectedLanguage);
                Log.i("IntroActivity", "Tag: " + dropdown.getTag().toString());
                Log.i("IntroActivity", "position: " + position);
                Log.i("IntroActivity", "mPosition: " + mPosition);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    if ((!dropdown.getTag().toString().equals(Integer.toString(position))) && (mPosition == position)) {
                        if (!(Locale.getDefault().getLanguage().equals(languages[position].toLowerCase()))) {
                            Log.i("IntroActivity", "OnItemSelected called: API24 user wants to change language");
                            Toast.makeText(IntroActivity.this, getString(R.string.toast_noLanguageSwitchingSupport), Toast.LENGTH_SHORT).show();
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
                } else {
                    if (position == 0) {
                        //English
                        Log.i("IntroActivity", "User selected " + selectedLanguage);
                        selectedLanguage = "en";
                        setLocale(IntroActivity.this, "en");
                    } else {
                        //German
                        Log.i("IntroActivity", "User selected " + selectedLanguage);
                        selectedLanguage = "de";
                        setLocale(IntroActivity.this, "de");
                    }
                }
                Log.i("IntroActivity", "=======");
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

    @Override
    protected void onResume() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            selectedLanguage = Locale.getDefault().getLanguage();
            if (selectedLanguage.equals("en")){
                dropdown.setTag("0");
            }
            else {
                dropdown.setTag("1");
            }
        }
        super.onResume();
    }

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
}
