package com.bug_apk.locoguard;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowGuardiansRecyclerviewAdapter extends RecyclerView.Adapter<ShowGuardiansRecyclerviewAdapter.ViewHolder>{
    private static final String TAG = "ShowGuardiansRecyclerViewAdapter";

    String email;
    Context context;
    ArrayList<String> guardiansList;

    public ShowGuardiansRecyclerviewAdapter(Context context, ArrayList<String> guardiansList, String email){
        Log.i("ShowGuardiansRecycler","Called ShowGuardiansRecycler");
        this.email = email;
        this.context = context;
        this.guardiansList = guardiansList;
    }
    @NonNull
    @Override
    public ShowGuardiansRecyclerviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i("ShowGuardiansRecycler","Entered ShowGuardiansRecycler");
        View view = LayoutInflater.from(context).inflate(R.layout.layout_show_guardians, parent, false);
        ShowGuardiansRecyclerviewAdapter.ViewHolder viewHolder = new ShowGuardiansRecyclerviewAdapter.ViewHolder(view);

        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull ShowGuardiansRecyclerviewAdapter.ViewHolder holder, int position) {
       holder.name.setText(guardiansList.get(position));
    }

    @Override
    public int getItemCount() {
        return guardiansList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        LinearLayoutCompat parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.GuardianName);
            parentLayout = itemView.findViewById(R.id.showGuardiansLinearLayout);

        }
    }
}
