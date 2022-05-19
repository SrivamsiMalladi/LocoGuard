package com.bug_apk.locoguard;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AutocompleteRecyclerViewAdapter extends RecyclerView.Adapter<AutocompleteRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "AutocompleteRecyclerVie";

    private ArrayList<String> mLocationPrimaryName = new ArrayList<>();
    private ArrayList<String> mLocationSecondaryName = new ArrayList<>();
    private ArrayList<String> mLocationID = new ArrayList<>();
    private Context mContext;


    public AutocompleteRecyclerViewAdapter(ArrayList<String> mLocationPrimaryName, ArrayList<String> mLocationSecondaryName, ArrayList<String> mLocationID, Context mContext) {
        this.mLocationPrimaryName = mLocationPrimaryName;
        this.mLocationSecondaryName = mLocationSecondaryName;
        this.mLocationID = mLocationID;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_autocomplete, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.i(TAG,"onBindVieHolder: called.");

        holder.locationPrimaryName.setText(mLocationPrimaryName.get(position));
        holder.locationSecondaryName.setText(mLocationSecondaryName.get(position));

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "OnClick: clicked on: "+mLocationPrimaryName.get(position)+ ": "+ mLocationSecondaryName.get(position)+", id:" + mLocationID.get(position));
                Intent intent = new Intent("locationIDBroadcast");
                //            intent.putExtra("quantity",Integer.parseInt(quantity.getText().toString()));
                intent.putExtra("locationID", mLocationID.get(position));
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

                int mLocationLength = mLocationPrimaryName.size();
                mLocationPrimaryName.clear();
                mLocationSecondaryName.clear();
                mLocationID.clear();
                notifyItemRangeRemoved(0,mLocationLength);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLocationPrimaryName.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView locationPrimaryName;
        TextView locationSecondaryName;
        ConstraintLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            locationPrimaryName = itemView.findViewById(R.id.locationPrimaryName);
            locationSecondaryName = itemView.findViewById(R.id.locationSecondaryName);
            parentLayout = itemView.findViewById(R.id.parentLayout);
        }
    }
}
