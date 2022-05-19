package com.bug_apk.locoguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

import static android.content.Context.MODE_PRIVATE;

public class CustomEditText extends androidx.appcompat.widget.AppCompatEditText {

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == EditorInfo.IME_ACTION_DONE) {
            // User has pressed Back key. So hide the keyboard
            Log.i("CustomeEditText", "Some key pressed");
            InputMethodManager mgr = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(this.getWindowToken(), 0);

            SharedPreferences.Editor editor = getContext().getSharedPreferences("LocoGuard", MODE_PRIVATE).edit();
            editor.putString("locationUpdateFrequency", ((CustomEditText)findViewById(R.id.locationUpdateFrequencyValue)).getText().toString());
            editor.apply();

            clearFocus();

            Toast.makeText(getContext(), R.string.profile_permissions_locationUpdateFrequency, Toast.LENGTH_LONG).show();
        }
        return true;
    }
}
