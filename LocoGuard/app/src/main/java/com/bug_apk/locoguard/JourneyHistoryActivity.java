package com.bug_apk.locoguard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Stack;

public class JourneyHistoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    JourneyHistoryRecyclerViewAdapter adapter;
    ArrayList<String> previousJourneys = new ArrayList<>();
    ArrayList<String> deletedJourneys = new ArrayList<>();
    CalendarView calendarView;
    int count = 0;
    int counter = 0;
    String fromString;
    String toString;
    private LinearLayout deletionlayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_history);

        TextView deleteFromBtn = (TextView) findViewById(R.id.deleteFrom_btn);
        TextView deleteToBtn = (TextView) findViewById(R.id.deleteTo_btn);
        user userDetails = getIntent().getParcelableExtra("userDetails");
        calendarView = (CalendarView) findViewById(R.id.calender_view);
        calendarView.setVisibility(View.INVISIBLE);

        ImageButton imageButton = (ImageButton)findViewById(R.id.recyclebin);
        Button button = (Button)findViewById(R.id.delete_btn);
        deletionlayout = findViewById(R.id.deletion);



        deletionlayout.setVisibility(deletionlayout.INVISIBLE);
        button.setVisibility(deletionlayout.INVISIBLE);

        imageButton.setOnClickListener(new View.OnClickListener() {
            boolean visible;
            @Override
            public void onClick(View v) {
                visible = !visible;
                deletionlayout.setVisibility(visible ? View.VISIBLE: View.GONE);
                button.setVisibility(visible ? View.VISIBLE: View.GONE);
                imageButton.setVisibility(View.GONE);
            }
        });

        ((ImageButton)findViewById(R.id.bck_arw_cin)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent = new Intent(JourneyHistoryActivity.this, ProfileActivity.class);
                backIntent.putExtra("userDetails",userDetails);
                startActivity(backIntent);
            }
        });

        deleteFromBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(calendarView.getVisibility() == View.INVISIBLE) {
                    calendarView.setVisibility(View.VISIBLE);
                    recyclerView.setAlpha(0);
                    deleteToBtn.setVisibility(View.INVISIBLE);
                    ((Button) findViewById(R.id.delete_btn)).setVisibility(View.INVISIBLE);
                    calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                        @Override
                        public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                            String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                            deleteFromBtn.setText(selectedDate);
                            calendarView.setVisibility(View.INVISIBLE);
                            recyclerView.setAlpha(1);
                            ((Button) findViewById(R.id.delete_btn)).setVisibility(View.VISIBLE);
                            deleteToBtn.setVisibility(View.VISIBLE);
                        }
                    });
                }
                else {
                    calendarView.setVisibility(View.INVISIBLE);
                    recyclerView.setAlpha(1);
                    ((Button) findViewById(R.id.delete_btn)).setVisibility(View.VISIBLE);
                    deleteToBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        deleteToBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!deleteFromBtn.getText().equals("")) {
                    if (calendarView.getVisibility() == View.INVISIBLE) {
                        calendarView.setVisibility(View.VISIBLE);
                        recyclerView.setAlpha(0);
                        deleteFromBtn.setVisibility(View.INVISIBLE);
                        ((Button) findViewById(R.id.delete_btn)).setVisibility(View.INVISIBLE);

                        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                            @Override
                            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                                String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                                deleteToBtn.setText(selectedDate);
                                calendarView.setVisibility(View.INVISIBLE);
                                recyclerView.setAlpha(1);
                                ((Button) findViewById(R.id.delete_btn)).setVisibility(View.VISIBLE);
                                deleteFromBtn.setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        calendarView.setVisibility(View.INVISIBLE);
                        recyclerView.setAlpha(1);
                        ((Button) findViewById(R.id.delete_btn)).setVisibility(View.VISIBLE);
                        deleteFromBtn.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(JourneyHistoryActivity.this, R.string.journeyHistory_selectFrom, Toast.LENGTH_SHORT).show();
                }
            }
        });


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("journeys").child(userDetails.encodedEmail()).child("previousJourneys");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String date = snapshot.getKey();
                    previousJourneys.add(date);
                }
                recyclerView = findViewById(R.id.recycler_view_journey);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(JourneyHistoryActivity.this));
                previousJourneys.sort(Comparator.reverseOrder());
                adapter = new JourneyHistoryRecyclerViewAdapter(JourneyHistoryActivity.this, previousJourneys, userDetails.encodedEmail());
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ((Button) findViewById(R.id.delete_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fromString = (String) deleteFromBtn.getText();
                toString = (String) deleteToBtn.getText();

                if(fromString.equals("") && toString.equals("")) {
                    reference.getParent().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.child("deletedJourneys").exists()) {
                                for(DataSnapshot snapshot : dataSnapshot.child("deletedJourneys").getChildren()) {
                                    String date = snapshot.getValue(String.class);
                                    deletedJourneys.add(date);
                                }
                                reference.getParent().addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        journey journey = dataSnapshot.getValue(com.bug_apk.locoguard.journey.class);
                                        HashMap<String, journey> journeys = journey.getPreviousJourneys();
                                        if(journeys!=null)
                                            for(int i = 0; i<deletedJourneys.size(); i++) {
                                                journeys.remove(deletedJourneys.get(i));
                                                previousJourneys.remove(deletedJourneys.get(i));
                                            }
                                        reference.setValue(journeys);
                                        reference.getParent().child("deletedJourneys").removeValue();
                                        adapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            else {
                                Toast.makeText(JourneyHistoryActivity.this, getString(R.string.journey_history_toast), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                else if(!fromString.equals("")) {
                    if(!toString.equals("")) {
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Calendar calendar = Calendar.getInstance();
                        Date currentTime = calendar.getTime();
                        String time = dateFormat.format(currentTime);
                        try {
                            Date fromDate = dateFormat.parse(fromString);
                            Date toDate = dateFormat.parse(toString);
                            Date currentDate = dateFormat.parse(time);
                            if(toDate.after(currentDate) || fromDate.after(currentDate)) {
                                Toast.makeText(JourneyHistoryActivity.this, getString(R.string.invalid_endDate), Toast.LENGTH_SHORT).show();
                            }
                            else if(fromDate.after(toDate)) {
                                Toast.makeText(JourneyHistoryActivity.this, getString(R.string.journeyHistory_invalidDate), Toast.LENGTH_SHORT).show();
                            }
                            else {
                                reference.getParent().addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for(DataSnapshot snapshot : dataSnapshot.child("previousJourneys").getChildren()) {
                                            try {
                                                String dateString = snapshot.getKey();
                                                Date date = dateFormat.parse(dateString);
                                                if((date.after(fromDate) && date.before(toDate)) || date.compareTo(fromDate) == 0 || date.compareTo(toDate) == 0) {
                                                    deletedJourneys.add(dateString);
                                                }
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        reference.getParent().addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                journey journey = dataSnapshot.getValue(com.bug_apk.locoguard.journey.class);
                                                HashMap<String, journey> journeys = journey.getPreviousJourneys();
                                                if(journeys!=null)
                                                    for(int i = 0; i<deletedJourneys.size(); i++) {
                                                        journeys.remove(deletedJourneys.get(i));
                                                        previousJourneys.remove(deletedJourneys.get(i));
                                                    }
                                                reference.setValue(journeys);
                                                reference.getParent().child("deletedJourneys").removeValue();
                                                adapter.notifyDataSetChanged();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    else {
                        Toast.makeText(JourneyHistoryActivity.this, getString(R.string.end_date_toast), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}