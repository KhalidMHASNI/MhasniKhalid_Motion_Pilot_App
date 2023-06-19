package com.example.mhasnikhalid_motionpilot_projet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Activityhistory extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private ActivityDataAdapt adapter;
    private List<ActivityData> activityDataList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        this.getSupportActionBar().hide();

        // Initialize Firebase Auth and Realtime Database reference
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("activityData").child(userId);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(Activityhistory.this));

        activityDataList = new ArrayList<>();
        adapter = new ActivityDataAdapt(activityDataList);
        recyclerView.setAdapter(adapter);

        // Load activity logs from Realtime Database
        loadActivityLogs();

        androidx.constraintlayout.widget.ConstraintLayout constraintLayout = findViewById(R.id.constraint_layout);

        // Set click listener for the ConstraintLayout
        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Activityhistory.this, Profile.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void loadActivityLogs() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance("https://mhasni-khalid-motionpilot-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference().child("activityData");

        Query query = databaseRef.orderByChild("userId").equalTo(userId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                activityDataList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String activityName = snapshot.child("activityName").getValue(String.class);
                    String date = snapshot.child("date").getValue(String.class);
                    String startTime = snapshot.child("startTime").getValue(String.class);
                    String endTime = snapshot.child("endTime").getValue(String.class);

                    ActivityData activity = new ActivityData(activityName, date, startTime, endTime);
                    activityDataList.add(activity);
                }
                Collections.sort(activityDataList, new Comparator<ActivityData>() {
                    @Override
                    public int compare(ActivityData activity1, ActivityData activity2) {
                        // Implement your custom sorting logic based on date and time
                        // For example, assuming date is in the format "yyyy-MM-dd" and time is in the format "HH:mm",
                        // you can parse the strings into Date objects and compare them
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
                        try {
                            Date date1 = dateFormat.parse(activity1.getDate());
                            Date time1 = timeFormat.parse(activity1.getStartTime());
                            Date date2 = dateFormat.parse(activity2.getDate());
                            Date time2 = timeFormat.parse(activity2.getStartTime());
                            if (date1.compareTo(date2) == 0) {
                                return time1.compareTo(time2);
                            } else {
                                return date2.compareTo(date1);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return 0;
                    }
                });
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Activityhistory.this, "Error occurred while loading activity logs", Toast.LENGTH_LONG).show();
                // Handle the failure
            }
        });
    }



}
