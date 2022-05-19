package com.bug_apk.locoguard;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

class LoadingDialog {

    private Activity activity;
    private AlertDialog dialog;

    LoadingDialog(Activity myactivity){
        activity = myactivity;
    }

    public void startLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.layout_progress_alert_dialog, null));
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.show();

        dialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent
        );
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    public boolean isDialogShowing() {
        return dialog.isShowing();
    }
}
