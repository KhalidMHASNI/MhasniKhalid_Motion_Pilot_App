package com.example.mhasnikhalid_motionpilot_projet;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.*;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Settings extends AppCompatActivity {

    private EditText firstNameEditText, lastNameEditText, emailEditText, passwordEditText, telephoneEditText, filiereEditText;
    private RadioGroup radioGroupGender;
    private ImageView profileImageView;
    private RadioButton radioButtonMale, radioButtonFemale;
    private Button updateButton,changeProfileButton;
    private SharedPreferences sharedPreferences;
    private static final String AUTH_TOKEN_KEY = "auth_token";

    DrawerLayout drawerLayout;
    FirebaseAuth auth;

    private Uri picUri;
    private static final int REQUEST_STORAGE_PERMISSION = 1;


    final private DatabaseReference userRef = FirebaseDatabase.getInstance("https://mhasni-khalid-motionpilot-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("users");
    final private StorageReference storageRef = FirebaseStorage.getInstance("gs://mhasni-khalid-motionpilot.appspot.com/").getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        this.getSupportActionBar().hide();

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        String authToken = sharedPreferences.getString(AUTH_TOKEN_KEY, null);

        auth = FirebaseAuth.getInstance();
        // Initialize your views
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        telephoneEditText = findViewById(R.id.telephoneEditText);
        filiereEditText = findViewById(R.id.filiereEditText);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        radioButtonMale = findViewById(R.id.radioButtonMale);
        radioButtonFemale = findViewById(R.id.radioButtonFemale);
        updateButton = findViewById(R.id.updateButton);
        changeProfileButton = findViewById(R.id.changeProfileButton);
        profileImageView = findViewById(R.id.profileImageView);
        drawerLayout = findViewById(R.id.drawerLayout);

        // Retrieve the user information from the Realtime Database
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userReff = FirebaseDatabase.getInstance("https://mhasni-khalid-motionpilot-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("users").child(userId);
            userReff.addListenerForSingleValueEvent(new ValueEventListener() {
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
                        Glide.with(Settings.this)
                                .load(uri)
                                .apply(requestOptions)
                                .into(profileImageView);

                        // Get the user data from the snapshot
                        String firstName = snapshot.child("fname").getValue(String.class);
                        String lastName = snapshot.child("lname").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String password = snapshot.child("password").getValue(String.class);
                        String telephone = snapshot.child("telephone").getValue(String.class);
                        String filiere = snapshot.child("filiere").getValue(String.class);
                        String gender = snapshot.child("gender").getValue(String.class);

                        // Set the user data as placeholders in the EditText fields
                        firstNameEditText.setText(firstName);
                        lastNameEditText.setText(lastName);
                        emailEditText.setText(email);
                        passwordEditText.setText(password);
                        telephoneEditText.setText(telephone);
                        filiereEditText.setText(filiere);

                        // Set the gender radio button
                        if (gender != null) {
                            if (gender.equals("Male")) {
                                radioButtonMale.setChecked(true);
                            } else if (gender.equals("Female")) {
                                radioButtonFemale.setChecked(true);
                            }
                        }
                    }
                }


                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle the error appropriately
                }
            });
        }



        changeProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(Settings.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted, request it from the user
                    ActivityCompat.requestPermissions(Settings.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
                } else {
                    // Permission is already granted, proceed with picking an image from the gallery
                    pickImageFromGalery();
                }
            }
        });


        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String userId = currentUser.getUid();
                    updateUserInfo(userId);
                    if (picUri != null) {
                        uploadToFirebase(picUri, currentUser);
                    } else {
                        //Toast.makeText(Settings.this, "Please select an image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        findViewById(R.id.imageView2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the navigation drawer
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation item clicks here
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // Open Profile activity
                    Intent intent = new Intent(Settings.this, Profile.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.nav_settings) {
                    // Open Settings activity
                    Intent intent = new Intent(Settings.this, Settings.class);
                    startActivity(intent);
                } else if (id == R.id.nav_share) {
                    // Open Signin activity
                    Intent intent = new Intent(Settings.this, MapActivity.class);
                    startActivity(intent);
                    finish();
                }else if (id == R.id.nav_activity) {
                    // Open Signin activity
                    Intent intent = new Intent(Settings.this, Activityhistory.class);
                    startActivity(intent);
                    finish();
                }  else if (id == R.id.nav_logout) {
                    // Log out the user
                    auth.signOut();
                    // Clear the shared preferences
                    sharedPreferences.edit().remove(AUTH_TOKEN_KEY).apply();
                    Intent intent = new Intent(Settings.this, Signin.class);
                    startActivity(intent);
                    finish();
                }
                drawerLayout.closeDrawer(GravityCompat.START); // Close the navigation drawer
                return true;
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, proceed with picking an image from the gallery
                pickImageFromGalery();
            } else {
                // Permission is denied, handle it accordingly
                Toast.makeText(Settings.this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult (
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode () == Activity .RESULT_OK){
                        Intent data = result.getData();
                        picUri = data.getData();
                        profileImageView.setImageURI(picUri);
                    } else {
                        Toast.makeText(Settings.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
    private void pickImageFromGalery() {
        Intent picpicker = new Intent();
        picpicker.setAction(Intent.ACTION_GET_CONTENT);
        picpicker.setType("image/*");
        activityResultLauncher.launch(picpicker);

    }
    private void uploadToFirebase(Uri uri, FirebaseUser currentUser){
        final StorageReference imageReference = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(uri));
        imageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String userId = currentUser.getUid();
                        DatabaseReference userRef = FirebaseDatabase.getInstance("https://mhasni-khalid-motionpilot-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("users").child(userId);
                        userRef.child("profilePicture").setValue(uri.toString());
                        Toast.makeText(Settings.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Settings.this, Profile.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
    }
    private String getFileExtension(Uri fileUri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(fileUri));
    }

    private void updateUserInfo(String userId) {
        // Get the updated user information from the EditText fields
        String updatedFirstName = firstNameEditText.getText().toString().trim();
        String updatedLastName = lastNameEditText.getText().toString().trim();
        String updatedEmail = emailEditText.getText().toString().trim();
        String updatedPassword = passwordEditText.getText().toString().trim();
        String updatedTelephone = telephoneEditText.getText().toString().trim();
        String updatedFiliere = filiereEditText.getText().toString().trim();
        String updatedGender = "";

        // Get the selected gender from the radio group
        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedGenderId == radioButtonMale.getId()) {
            updatedGender = "Male";
        } else if (selectedGenderId == radioButtonFemale.getId()) {
            updatedGender = "Female";
        }

        // Update the user information in the Realtime Database
        DatabaseReference userRef = FirebaseDatabase.getInstance("https://mhasni-khalid-motionpilot-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("users").child(userId);

        userRef.child("fname").setValue(updatedFirstName);
        userRef.child("lname").setValue(updatedLastName);
        userRef.child("email").setValue(updatedEmail);
        userRef.child("password").setValue(updatedPassword);
        userRef.child("telephone").setValue(updatedTelephone);
        userRef.child("filiere").setValue(updatedFiliere);
        userRef.child("gender").setValue(updatedGender);

        updateEmailAndPassword(updatedEmail, updatedPassword);

        Toast.makeText(Settings.this, "Mise è jour avec succès", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.this, Profile.class);
        startActivity(intent);

        finish();
    }
    private void updateEmailAndPassword(String newEmail, String newPassword) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.updateEmail(newEmail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> emailTask) {
                            if (emailTask.isSuccessful()) {
                                // Email updated successfully
                                currentUser.updatePassword(newPassword)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> passwordTask) {
                                                if (passwordTask.isSuccessful()) {
                                                    // Password updated successfully
                                                    Toast.makeText(Settings.this, "Email and password updated successfully", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    // Failed to update password, handle the error
                                                    Toast.makeText(Settings.this, "Failed to update password: " + passwordTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                // Failed to update email, handle the error
                                Toast.makeText(Settings.this, "Failed to update email: " + emailTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("TAG", emailTask.getException().getMessage());
                            }
                        }
                    });
        }
    }



}
