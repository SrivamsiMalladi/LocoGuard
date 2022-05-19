package com.bug_apk.locoguard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.widget.Toast;

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
import io.opencensus.resource.Resource;


public class ContactslistRecyclerViewAdapter extends RecyclerView.Adapter<ContactslistRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "ContactslistRecyclerViewAdapter";

 //   ArrayList<user> mImages = new ArrayList<>();
//    private ArrayList<user> mNames = new ArrayList<>();
//    private Context mContext;


//    public ContactslistRecyclerViewAdapter(ArrayList<String> mImages, ArrayList<String> mNames, Context mContext) {
//        this.mImages = mImages;
//        this.mNames = mNames;
//        this.mContext = mContext;
//    }

    Context context;
    ArrayList<user> users;
    String email;
    CheckBox friendCheck;


    int duration = Toast.LENGTH_SHORT;
   // ArrayList<user> contacts;


    public ContactslistRecyclerViewAdapter(Context context , ArrayList<user> users,String email)
    {
        this.context = context;
        this.users = users;
        this.email = email;
      //  this.contacts = contacts;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_contactslist, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        holder.name.setText(users.get(position).getName());

        Picasso.get().load(users.get(position).getDp()).into(holder.image);

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference;
                reference = FirebaseDatabase.getInstance().getReference("users");
                user addedUser = users.get(position);
                Log.i("CRVActivity", email);
                Log.i("CRVActivity", addedUser.getEmail());
                reference.child(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        user dbUserDetails =  dataSnapshot.getValue(user.class);
                        ArrayList<String> newFriendsList = dbUserDetails.getFriends();
                        if(newFriendsList==null) {
                            newFriendsList = new ArrayList<String>();
                            newFriendsList.add(addedUser.encodedEmail());
                            dbUserDetails.setFriends(newFriendsList);
                            reference.child(email).setValue(dbUserDetails);
                            Toast toast = Toast.makeText(context, R.string.contactsListRecyclerView_friendAdded, duration);
                            toast.show();
                        }
                        else if(newFriendsList.contains(users.get(position).encodedEmail())) {
                            Toast toast = Toast.makeText(context, R.string.contactsListRecyclerVIew_alreadyAdded, duration);
                            toast.show();

                        }
                        else {
                            newFriendsList.add(addedUser.encodedEmail());
                            dbUserDetails.setFriends(newFriendsList);
                            reference.child(email).setValue(dbUserDetails);
                            Toast toast = Toast.makeText(context, R.string.contactsListRecyclerView_friendAdded, duration);
                            toast.show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }
        });



    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView image;
        TextView name;
        RelativeLayout parentLayout;

//        onNoteListener onNoteListener = new onNoteListener();
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            parentLayout = itemView.findViewById(R.id.parent_layout);

        }

    }

}

