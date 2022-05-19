package com.bug_apk.locoguard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class JourneyHistoryRecyclerViewAdapter extends RecyclerView.Adapter<JourneyHistoryRecyclerViewAdapter.ViewHolder> {
    Context context;
    ArrayList<String> previousJourneys;
    String email;
    ArrayList<String> deletedJourneys = new ArrayList<>();
    int count = 0;

    public  JourneyHistoryRecyclerViewAdapter(Context context, ArrayList<String> previousJourneys, String email) {
        this.context = context;
        this.previousJourneys = previousJourneys;
        this.email = email;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_journey_history, parent, false);
        JourneyHistoryRecyclerViewAdapter.ViewHolder viewHolder = new JourneyHistoryRecyclerViewAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.date.setText(previousJourneys.get(position));
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("journeys").child(email).child("previousJourneys").child(previousJourneys.get(position));
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                journey journey = dataSnapshot.getValue(com.bug_apk.locoguard.journey.class);
                holder.source.setText(journey.getSourceName());
                holder.destination.setText(journey.getDestinationName());
                holder.linLayout.setBackgroundResource(R.color.colorAccent);
                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public boolean onLongClick(View v) {
                count++;
                if (count % 2 == 0) {
                    holder.linLayout.setBackgroundColor(R.color.colorPrimary);
                    deletedJourneys.add(previousJourneys.get(position));
                }
                else {
                    holder.linLayout.setBackgroundResource(R.color.colorAccent);
                    deletedJourneys.remove(previousJourneys.get(position));
                }
                reference.getParent().getParent().child("deletedJourneys").setValue(deletedJourneys);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return previousJourneys.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView date, source, destination;
        LinearLayout linLayout;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.tv_date);
            source = itemView.findViewById(R.id.tv_source);
            destination = itemView.findViewById(R.id.tv_destination);
            cardView = itemView.findViewById(R.id.cardView);
            linLayout = itemView.findViewById(R.id.lin_layout);
            }
    }
}
