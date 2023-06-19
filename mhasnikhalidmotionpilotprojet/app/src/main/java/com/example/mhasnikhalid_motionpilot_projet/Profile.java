package com.example.mhasnikhalid_motionpilot_projet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.mhasnikhalid_motionpilot_projet.Service.BackgroundDetectedActivitiesService;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Profile extends AppCompatActivity {

    FirebaseAuth auth;
    TextView userDetailsTextView, emailTxt;
    ImageView avatarImageView,activityIll;
    DatabaseReference userRef;
    DrawerLayout drawerLayout;
    TextView activityTypeTextView;
    Button activityRecognitionButtonOn;
    Button activityRecognitionButtonOff;
    private BroadcastReceiver broadcastReceiver;
    private TextView tvWalking;
    private TextView tvOnFoot;
    private TextView tvStill;
    private TextView tvRunning;
    private int totalConfidence = 100;
    private boolean isActivityOngoing = false;
    private int previousActivityType = -1;

    private FirebaseDatabase firestore;
    private SharedPreferences sharedPreferences;
    private static final String AUTH_TOKEN_KEY = "auth_token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        this.getSupportActionBar().hide();


        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        String authToken = sharedPreferences.getString(AUTH_TOKEN_KEY, null);
        if (authToken == null) {
            // User is not logged in, navigate to the Signin activity
            Intent intent = new Intent(getApplicationContext(), Signin.class);
            startActivity(intent);
            finish();
        }

        auth = FirebaseAuth.getInstance();
        userDetailsTextView = findViewById(R.id.user_details);
        avatarImageView = findViewById(R.id.avatarImageView);
        emailTxt = findViewById(R.id.emailTxt);
        drawerLayout = findViewById(R.id.drawerLayout);


        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(getApplicationContext(), Signin.class);
            startActivity(intent);
        } else {
            String userId = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance("https://mhasni-khalid-motionpilot-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("users").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String uri = snapshot.child("profilePicture").getValue(String.class);

                        // Using RequestOptions to customize Glide's behavior
                        RequestOptions requestOptions = new RequestOptions()
                                .placeholder(R.drawable.default_pfp) // Placeholder image while loading
                                .error(R.drawable.default_pfp) // Error image if loading fails
                                .diskCacheStrategy(DiskCacheStrategy.ALL) // Caching behavior
                                .transform(new CircleCrop()); // Apply circle crop transformation

                        // Load the image using Glide
                        Glide.with(Profile.this)
                                .load(uri)
                                .apply(requestOptions)
                                .into(avatarImageView);

                        String firstName = snapshot.child("fname").getValue(String.class);
                        String lastName = snapshot.child("lname").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);

                        // Update the TextView with the firstName and lastName values
                        String welcomeMessage = firstName + " " + lastName;
                        userDetailsTextView.setText(welcomeMessage);
                        emailTxt.setText(email);
                    } else {
                        userDetailsTextView.setText(snapshot.toString());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle the error appropriately
                }
            });
        }

        activityTypeTextView = findViewById(R.id.activityTypeTextView);
        activityRecognitionButtonOn = findViewById(R.id.activityRecognitionButtonOn);
        activityRecognitionButtonOff = findViewById(R.id.activityRecognitionButtonOff);
        tvWalking = findViewById(R.id.tvWalking);
        tvOnFoot = findViewById(R.id.tvOnFoot);
        tvStill = findViewById(R.id.tvStill);
        tvRunning = findViewById(R.id.tvRunning);

        requestActivityRecognitionPermission();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)) {
                    ArrayList<Integer> activityTypes = intent.getIntegerArrayListExtra("Activités");
                    ArrayList<Integer> confidences = intent.getIntegerArrayListExtra("Confidences");
                    userActivityOutput(activityTypes, confidences);
                }
            }
        };

        activityRecognitionButtonOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTracking();
            }
        });
        activityRecognitionButtonOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTracking();

                // Update the endTime of the last activity
                updateLastActivityEndTime();
            }
        });

        // Add click listener to the ConstraintLayout
        findViewById(R.id.imageView2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the navigation drawer
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Set up the navigation drawer
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation item clicks here
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // Open Profile activity
                    Intent intent = new Intent(Profile.this, Profile.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.nav_settings) {
                    // Open Settings activity
                    Intent intent = new Intent(Profile.this, Settings.class);
                    startActivity(intent);
                } else if (id == R.id.nav_share) {
                    Intent intent = new Intent(Profile.this, MapActivity.class);
                    startActivity(intent);
                    finish();
                }else if (id == R.id.nav_activity) {
                    // Open Signin activity
                    Intent intent = new Intent(Profile.this, Activityhistory.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.nav_logout) {
                    // Log out the user
                    auth.signOut();
                    // Clear the shared preferences
                    sharedPreferences.edit().remove(AUTH_TOKEN_KEY).apply();
                    Intent intent = new Intent(Profile.this, Signin.class);
                    startActivity(intent);
                    finish();
                }
                drawerLayout.closeDrawer(GravityCompat.START); // Close the navigation drawer
                return true;
            }
        });
        avatarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String uri = snapshot.child("profilePicture").getValue(String.class);
                            // Open the profile picture in a dialog or activity
                            showDialogWithProfilePicture(uri);
                        } else {
                            // Handle the case where the profile picture URI is not available
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle the database read error
                    }
                });
            }
        });

    }
    private void showDialogWithProfilePicture(String profilePictureUri) {
        // Create a dialog or start an activity to show the profile picture
        Dialog dialog = new Dialog(Profile.this);
        dialog.setContentView(R.layout.dialog_profile_picture);

        // Set the dialog background to be transparent
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Find the ImageView in the dialog layout
        ImageView profileImageView = dialog.findViewById(R.id.profileImageView);

        // Adjust the size of the profile picture
        int imageSize = getResources().getDimensionPixelSize(R.dimen.profile_image_size);
        profileImageView.getLayoutParams().width = imageSize;
        profileImageView.getLayoutParams().height = imageSize;

        // Load the profile picture using Glide or any other image loading library
        Glide.with(Profile.this)
                .load(profilePictureUri)
                .apply(RequestOptions.circleCropTransform())
                .into(profileImageView);

        // Show the dialog
        dialog.show();
    }

    private void startTracking() {
        Intent intent = new Intent(this, BackgroundDetectedActivitiesService.class);
        startService(intent);
    }

    private void stopTracking() {
        Intent intent = new Intent(this, BackgroundDetectedActivitiesService.class);
        stopService(intent);
    }
    private void userActivityOutput(ArrayList<Integer> activityTypes, ArrayList<Integer> confidences) {
        int sumConfidences = 0;
        int walkingConfidence = 0;
        int onFootConfidence = 0;
        int stillConfidence = 0;
        int runningConfidence = 0;
        int maxConfidence = 0;
        int maxConfidenceType = -1;
        int maxConfidenceIndex = -1;
        for (int i = 0; i < activityTypes.size(); i++) {
            int type = activityTypes.get(i);
            int confidence = confidences.get(i);
            switch (type) {
                case DetectedActivity.WALKING:
                    walkingConfidence = confidence;
                    break;
                case DetectedActivity.ON_FOOT:
                    onFootConfidence = confidence;
                    break;
                case DetectedActivity.STILL:
                    stillConfidence = confidence;
                    break;
                case DetectedActivity.RUNNING:
                    runningConfidence = confidence;
                    break;
            }
        }
        sumConfidences = walkingConfidence + onFootConfidence + stillConfidence + runningConfidence;

        if (sumConfidences != totalConfidence) {
            float factor = (float) totalConfidence / (sumConfidences + 1);
            walkingConfidence = Math.round(walkingConfidence * factor);
            onFootConfidence = Math.round(onFootConfidence * factor);
            stillConfidence = Math.round(stillConfidence * factor);
            runningConfidence = totalConfidence - walkingConfidence - onFootConfidence - stillConfidence;
        }
        tvWalking.setText("Marcher, Confidence: " + walkingConfidence);
        tvOnFoot.setText("Debout, Confidence: " + onFootConfidence);
        tvStill.setText("Still, Confidence: " + stillConfidence);
        tvRunning.setText("Courir, Confidence: " + runningConfidence);

        activityIll = findViewById(R.id.activityIll);
        // Find the activity type with the highest confidence
        if (walkingConfidence > maxConfidence) {
            maxConfidence = walkingConfidence;
            maxConfidenceType = DetectedActivity.WALKING;
            activityIll.setImageResource(R.drawable.walking);
        }
        if (onFootConfidence > maxConfidence) {
            maxConfidence = onFootConfidence;
            maxConfidenceType = DetectedActivity.ON_FOOT;
            activityIll.setImageResource(R.drawable.still);
        }
        if (stillConfidence > maxConfidence) {
            maxConfidence = stillConfidence;
            maxConfidenceType = DetectedActivity.STILL;
            activityIll.setImageResource(R.drawable.still);
        }
        if (runningConfidence > maxConfidence) {
            maxConfidence = runningConfidence;
            maxConfidenceType = DetectedActivity.RUNNING;
            activityIll.setImageResource(R.drawable.running);
        }

        if (maxConfidenceType != -1) {

            if (maxConfidenceType != previousActivityType) {
                // Detected activity has changed, end the previous activity
                if (isActivityOngoing) {
                    // Get the current date and time
                    Date currentDate = new Date();
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                    String endTimeString = timeFormat.format(currentDate);

                    // Update the Firestore document with the end time
                    //updateActivityEndTime(endTimeString);

                    isActivityOngoing = false;
                }
            }

            String activityName = getActivityName(maxConfidenceType);

            if (!isActivityOngoing) {
                // Get the current date and time
                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                String dateString = dateFormat.format(currentDate);
                String startTimeString = timeFormat.format(currentDate);

                // Save the activity data to Firestore
                saveActivityData(activityName, dateString, startTimeString);
                isActivityOngoing = true;
            }
            previousActivityType = maxConfidenceType;
        } else {
            // No activity with confidence above threshold detected
            if (isActivityOngoing) {
                // Get the current date and time
                Date currentDate = new Date();
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                String endTimeString = timeFormat.format(currentDate);

                // Update the Firestore document with the end time
                //updateActivityEndTime(endTimeString);

                isActivityOngoing = false;
            }
        }
    }

    private void saveActivityData(String activityName, String dateString, String startTimeString) {
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance("https://mhasni-khalid-motionpilot-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("activityData");

        // Create a data object to be stored in the Realtime Database
        String key = databaseRef.push().getKey();
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("activityName", activityName);
        data.put("date", dateString);
        data.put("startTime", startTimeString);
        data.put("endTime", "");

        // Save the data in the Realtime Database
        databaseRef.child(key).setValue(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Data successfully stored in the Realtime Database
                        updatePreviousActivityEndTime(key, startTimeString);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Profile.this, "Error occurred while storing Activity data in the Realtime Database", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void updatePreviousActivityEndTime(String currentActivityKey, String startTimeString) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance("https://mhasni-khalid-motionpilot-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("activityData");

        // Query the previous activity
        Query query = databaseRef.orderByKey().limitToLast(2);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() > 1) {
                    // Get the key of the previous activity
                    String previousActivityKey = snapshot.getChildren().iterator().next().getKey();

                    // Update the endTime of the previous activity with the startTime of the current activity
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("endTime", startTimeString);
                    databaseRef.child(previousActivityKey).updateChildren(updateData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Error occurred while updating previous activity endTime", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateLastActivityEndTime() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance("https://mhasni-khalid-motionpilot-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("activityData");

        // Query the last activity
        Query query = databaseRef.orderByKey().limitToLast(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() > 0) {
                    // Get the key of the last activity
                    String lastActivityKey = snapshot.getChildren().iterator().next().getKey();

                    // Update the endTime of the last activity with the current time
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                    Date currentTime = new Date();
                    String endTimeString = dateFormat.format(currentTime); // Replace with your own method to get the current time
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("endTime", endTimeString);
                    databaseRef.child(lastActivityKey).updateChildren(updateData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Error occurred while updating last activity endTime", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String getActivityName(int activityType) {
        switch (activityType) {
            case DetectedActivity.WALKING:
                return "Walking";
            case DetectedActivity.ON_FOOT:
                return "On Foot";
            case DetectedActivity.STILL:
                return "Still";
            case DetectedActivity.RUNNING:
                return "Running";
            default:
                return "Unknown";
        }
    }

    private void requestActivityRecognitionPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION}, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    String permission = permissions[i];
                    if (android.Manifest.permission.ACTIVITY_RECOGNITION.equalsIgnoreCase(permission)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            // you now have permission
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }


}
