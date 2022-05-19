package com.bug_apk.locoguard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class    ResetPasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        mAuth = FirebaseAuth.getInstance();

        LoadingDialog loadingDialog = new LoadingDialog(ResetPasswordActivity.this);

        ((ImageButton)findViewById(R.id.backarrow)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent welcomeActivity = new Intent(ResetPasswordActivity.this, SignInActivity.class);
                startActivity(welcomeActivity);
            }
        });

        ((Button) findViewById(R.id.resetButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getRootView().getWindowToken(), 0);
                loadingDialog.startLoadingDialog();
                email = ((EditText) findViewById(R.id.email)).getText().toString().toLowerCase();
                if(TextUtils.isEmpty(email)) {
                    loadingDialog.dismissDialog();
                    Toast.makeText(ResetPasswordActivity.this, getString(R.string.resetPassword_validEmail),Toast.LENGTH_SHORT).show();
                }
                else {
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                loadingDialog.dismissDialog();
                                Toast.makeText(ResetPasswordActivity.this,getString(R.string.resetPassword_resetLinkSent),Toast.LENGTH_LONG).show();
                                Intent loginIntent = new Intent(ResetPasswordActivity.this,SignInActivity.class);
                                loginIntent.putExtra("ResetPassword", true);
                                startActivity(loginIntent);
                            }
                            else {
                                loadingDialog.dismissDialog();
                                Toast.makeText(ResetPasswordActivity.this,getString(R.string.resetPassword_somethingWentWrong),Toast.LENGTH_SHORT);
                            }
                        }
                    });
                }

            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(new NoInternetHelper(this).noInternetBroadcastCallback,
                new IntentFilter("InternetStatusBroadcast"));
        new NoInternetHelper(this).checkInternet();
    }
}
