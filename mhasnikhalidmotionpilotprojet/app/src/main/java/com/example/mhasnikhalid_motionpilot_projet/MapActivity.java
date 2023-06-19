package com.example.mhasnikhalid_motionpilot_projet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final float DEFAULT_ZOOM = 15f;
    private Marker startMarker;
    private Polyline polyline;
    private List<LatLng> polylinePoints = new ArrayList<>();
    private Location previousLocation;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getSupportActionBar().hide();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        androidx.constraintlayout.widget.ConstraintLayout constraintLayout = findViewById(R.id.constraint_layout);

        // Set click listener for the ConstraintLayout
        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapActivity.this, Profile.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private LatLng initialPosition; // Declare the initialPosition variable as a class member

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Load the map style from the JSON file in the raw directory
        try {
            boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
            if (!success) {
                // Failed to apply the map style
                // Handle error or display a message
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, get the current location
            getCurrentLocation();
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            initialPosition = new LatLng(location.getLatitude(), location.getLongitude());

                            // Add red marker for initial position
                            mMap.addMarker(new MarkerOptions()
                                    .position(initialPosition)
                                    .title("Start Point")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                            // Add blue marker for initial position
                            mMap.addMarker(new MarkerOptions()
                                    .position(initialPosition)
                                    .title("Blue Point")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                            // Move camera to the start point and zoom in
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, DEFAULT_ZOOM));

                            // Start location updates to track position changes
                            startLocationUpdates();
                        }
                    });
        }
    }



    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(5000); // 5 seconds
            locationRequest.setFastestInterval(3000); // 3 seconds
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }

                    for (Location location : locationResult.getLocations()) {
                        LatLng currentPoint = new LatLng(location.getLatitude(), location.getLongitude());

                        // Update the marker for current position
                        updateCurrentLocationMarker(location);

                        // Draw polyline between previous and current position
                        drawPolyline(location);
                    }
                }
            };

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void updateCurrentLocationMarker(Location location) {
        LatLng currentPoint = new LatLng(location.getLatitude(), location.getLongitude());

        // Remove the previous start marker
        if (startMarker != null) {
            startMarker.remove();
        }

        // Add marker for current position
        startMarker = mMap.addMarker(new MarkerOptions().position(currentPoint).title("Start Point"));
    }

    private void drawPolyline(Location location) {
        LatLng currentPoint = new LatLng(location.getLatitude(), location.getLongitude());
        polylinePoints.add(currentPoint);

        if (polyline != null) {
            polyline.remove();
        }

        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(polylinePoints)
                .width(5)
                .color(Color.RED);

        polyline = mMap.addPolyline(polylineOptions);

        // Calculate speed
        double speed = calculateSpeed(location);
        // Calculate distance
        double distance = calculateDistance();
        // Get Activity Detected
        String activityName=displayLatestActivity();

        // Update the information fields
        TextView speedText = findViewById(R.id.speed_text);
        TextView distanceText = findViewById(R.id.distance_text);
        TextView activityText = findViewById(R.id.activity_text);
        //activityText.setText("Activity Detected: "+activityName);
        speedText.setText(String.format(Locale.getDefault(), "Speed: %.2f km/h", speed));
        distanceText.setText(String.format(Locale.getDefault(), "Distance Traveled: %.2f meters", distance));
    }

    private String displayLatestActivity() {
        final String[] activityName = new String[1];
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance("https://mhasni-khalid-motionpilot-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference()
                .child("activityData");

        Query latestActivityQuery = databaseRef.orderByChild("userId").equalTo(userId)
                .limitToLast(1);

        latestActivityQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        activityName[0] = snapshot.child("activityName").getValue(String.class);
                        TextView activityText = findViewById(R.id.activity_text);
                        activityText.setText("Activity Detected: " + activityName[0]);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MapActivity.this, "Error occurred while retrieving activity data from the Realtime Database",
                        Toast.LENGTH_LONG).show();
                // Error occurred while retrieving data from the Realtime Database
            }
        });
        return activityName[0];
    }



    private double calculateSpeed(Location location) {
        if (previousLocation == null) {
            previousLocation = location;
            startTime = System.currentTimeMillis();
            return 0;
        }

        double distance = location.distanceTo(previousLocation);
        long deltaTime = System.currentTimeMillis() - startTime;
        double speed = (distance / deltaTime) * 1000 * 3.6; // Convert m/s to km/h

        previousLocation = location;
        startTime = System.currentTimeMillis();

        return speed;
    }

    private double calculateDistance() {
        double distance = 0;

        for (int i = 0; i < polylinePoints.size() - 1; i++) {
            LatLng startPoint = polylinePoints.get(i);
            LatLng endPoint = polylinePoints.get(i + 1);
            float[] results = new float[1];
            Location.distanceBetween(startPoint.latitude, startPoint.longitude, endPoint.latitude, endPoint.longitude, results);
            distance += results[0];
        }

        return distance;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted
                getCurrentLocation();
            } else {
                // Location permission denied
                // Handle error or display a message
            }
        }
    }
}