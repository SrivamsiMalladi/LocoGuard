package com.bug_apk.locoguard;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShowGuardiansFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowGuardiansFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    journey journey;
    user userDetails;

    RecyclerView recyclerView;
    ShowGuardiansRecyclerviewAdapter adapter;
    String email;
    ArrayList<String> guardiansList;
    TextView textView;
    CharSequence nullGuardians = "Journey Ended";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ShowGuardiansFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ShowGuardiansFragment.
     */
    // TODO: Rename and change types and number of parameters
    public ShowGuardiansFragment(String email) {
        this.email = email;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_guardians, container, false);
        Log.i("User_Email",email);
        // Inflate the layout for this fragment

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("journeys");
        guardiansList = new ArrayList<>();
        textView = getView().findViewById(R.id.guardiansListTitle);
        reference.child(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                journey = dataSnapshot.getValue(com.bug_apk.locoguard.journey.class);
                recyclerView = (RecyclerView) view.findViewById(R.id.show_guardians_recycler_view);
                if(journey.getGuardiansList()!=null || journey.getContactNames()!=null) {
                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("users");
                    reference1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot snapshot:dataSnapshot.getChildren()) {
                                user user = snapshot.getValue(user.class);
                                if(journey.getGuardiansList()!=null)
                                if(journey.getGuardiansList().contains(user.encodedEmail()))
                                    guardiansList.add(user.getName());
                            }
                            if(journey.getContactNames()!=null)
                                guardiansList.addAll(journey.getContactNames());

                            adapter = new ShowGuardiansRecyclerviewAdapter(getContext(),guardiansList,email);
                            recyclerView.setLayoutManager( new LinearLayoutManager(getActivity()));
                            recyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
                else
                {
                    textView.setText(nullGuardians);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Log.i("ShowGuardians","Entered ShowGuardians");
//        Log.i("ShowingGuardians","Showing " + size + " Guardians: ");
//        for(int i = 0; i<guardiansList.size();i++) {
//            Log.i("ShowList", guardiansList.get(i));
//        }
//        recyclerView = (RecyclerView) view.findViewById(R.id.show_guardians_recycler_view);
//        adapter = new ShowGuardiansRecyclerviewAdapter(getContext(),guardiansList,email);
//        recyclerView.setLayoutManager( new LinearLayoutManager(getActivity()));
//        recyclerView.setAdapter(adapter);
//        adapter.notifyDataSetChanged();
    }
}