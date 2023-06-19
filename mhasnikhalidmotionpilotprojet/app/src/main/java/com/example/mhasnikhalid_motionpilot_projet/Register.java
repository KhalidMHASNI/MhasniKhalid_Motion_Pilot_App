package com.example.mhasnikhalid_motionpilot_projet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;
    private TextInputEditText firstNameEditText;
    private TextInputEditText lastNameEditText;
    private TextInputEditText telephoneEditText;
    private RadioGroup genderRadioGroup;
    private DatabaseReference mDatabase;


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            Intent intent = new Intent(getApplicationContext(), Profile.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        this.getSupportActionBar().hide();


        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonReg = findViewById(R.id.btn_register);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progress_circular);
        textView = findViewById(R.id.loginNow);
        firstNameEditText = findViewById(R.id.firstName);
        lastNameEditText = findViewById(R.id.lastName);
        telephoneEditText = findViewById(R.id.telephone);
        genderRadioGroup = findViewById(R.id.radioGroupGender);

        mDatabase = FirebaseDatabase.getInstance("https://mhasni-khalid-motionpilot-default-rtdb.europe-west1.firebasedatabase.app/").getReference();


        textView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Signin.class);
                startActivity(intent);
            }
        });
        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password, firstName, lastName, telephone, gender;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                firstName = String.valueOf(firstNameEditText.getText());
                lastName = String.valueOf(lastNameEditText.getText());
                telephone = String.valueOf(telephoneEditText.getText());

                int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
                if (selectedGenderId == R.id.radioButtonMale) {
                    gender = "Male";
                } else if (selectedGenderId == R.id.radioButtonFemale) {
                    gender = "Female";
                } else {
                    // Handle the case when no gender is selected
                    gender = "";
                }

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Register.this, "Enter email", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (!isValidEmail(email)) {
                    Toast.makeText(Register.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Register.this, "Enter password", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(firstName)) {
                    Toast.makeText(Register.this, "Enter First Name", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(lastName)) {
                    Toast.makeText(Register.this, "Enter Last Name", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(telephone)) {
                    Toast.makeText(Register.this, "Enter phone number", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (!isValidPhoneNumber(telephone)) {
                    Toast.makeText(Register.this, "Invalid phone number format, it should be like 05(06 | 07)20304050", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser currentUser = mAuth.getCurrentUser();
                                    if (currentUser != null) {
                                        String userId = currentUser.getUid(); // Get the authenticated user's ID

                                        // Store user data in the Realtime Database using the same userId
                                        writeNewUser(firstName, lastName, email, password, telephone, gender, "", userId, "");

                                        Toast.makeText(Register.this, "Compte créé avec succès.",
                                                Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), Profile.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Handle the case when currentUser is null
                                        Toast.makeText(Register.this, "Failed to retrieve current user",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // Registration failed
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(Register.this, errorMessage, Toast.LENGTH_SHORT).show();
                                }
                                progressBar.setVisibility(View.GONE);
                            }
                        });
            }
        });

    }
    public void writeNewUser(String fname, String lname, String email, String password, String telephone, String sexe, String filiere, String userId,String picpath) {
        User user = new User(fname, lname, email, password, telephone, sexe, filiere, userId, picpath);
        mDatabase.child("users").child(userId).setValue(user);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        String phoneRegex = "^(05|06|07)[0-9]{8}$";
        return phoneNumber.matches(phoneRegex);
    }

}
