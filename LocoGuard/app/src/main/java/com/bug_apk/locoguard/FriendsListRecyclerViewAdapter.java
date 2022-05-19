package com.bug_apk.locoguard;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsListRecyclerViewAdapter extends RecyclerView.Adapter<FriendsListRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "FriendsListRecyclerViewAdapter";

    //   ArrayList<user> mImages = new ArrayList<>();
//    private ArrayList<user> mNames = new ArrayList<>();
//    private Context mContext;


//    public ContactslistRecyclerViewAdapter(ArrayList<String> mImages, ArrayList<String> mNames, Context mContext) {
//        this.mImages = mImages;
//        this.mNames = mNames;
//        this.mContext = mContext;
//    }

    Context context;
    ArrayList<String> friends;
    user user;
    String email;
    // ArrayList<user> contacts;


    public FriendsListRecyclerViewAdapter(Context context, ArrayList<String> friends, String email)
    {
        this.context = context;
        this.friends = friends;
        this.email = email;
        //  this.contacts = contacts;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_friends_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }



    @Override
    public void onBindViewHolder(@NonNull FriendsListRecyclerViewAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance().getReference().child("users");
        Log.i("FRVActivity", email);
        reference.child(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user dbUserDetails = dataSnapshot.getValue(user.class);
                String friendEmail;
                assert dbUserDetails.getFriends() != null;
                if (!(dbUserDetails.getFriends().isEmpty())) {
                    friendEmail = dbUserDetails.getFriends().get(position);
                    Log.i("FLRVAdActivity", friendEmail);
                    DatabaseReference reference2 = reference.child(friendEmail.replace("#E2", "%2E"));
                    reference2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            user friendDetails = dataSnapshot.getValue(user.class);
                            String friendName = friendDetails.getName();
                            holder.name.setText(friendName);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

                    Picasso.get().load(dbUserDetails.getDp()).into(holder.image);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView image;
        TextView name;
        RelativeLayout parentlayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            parentlayout = itemView.findViewById(R.id.parent_layout);

        }
    }

}
