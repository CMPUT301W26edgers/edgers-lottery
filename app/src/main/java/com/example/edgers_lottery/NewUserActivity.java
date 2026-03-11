package com.example.edgers_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class NewUserActivity extends AppCompatActivity {

    private TextInputEditText firstNameEditText, lastNameEditText, emailEditText, passwordEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountcreation);

        // Hook up the fields from your XML
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        // include switch for signing up as a entrant or an organizer


        findViewById(R.id.continueUserButton).setOnClickListener(v -> registerUser());
        findViewById(R.id.continueOrgButton).setOnClickListener(v -> registerOrganizer());
    }

    private void registerUser() {
        String firstName = firstNameEditText.getText().toString().trim(); // remove whitespace
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // user messed up the form, don't let them proceed yet
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create account now
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    saveUserToFirestore(uid, firstName, lastName, email, "ENTRANT");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void registerOrganizer() {
        String firstName = firstNameEditText.getText().toString().trim(); // remove whitespace
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // user messed up the form, don't let them proceed yet
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create account now
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    saveUserToFirestore(uid, firstName, lastName, email, "ORGANIZER");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserToFirestore(String uid, String firstName, String lastName, String email, String role) {
        User user = new User(uid, firstName + " " + lastName, email, role); // user doesnt need to enter phone number until they update person information
        // save this user to the firebase
        FirebaseFirestore.getInstance().collection("users").document(uid).set(user)
                .addOnSuccessListener(unused -> {
                    // go to HomeActivity now, user created
                    Toast.makeText(this, "User created successfully", Toast.LENGTH_SHORT).show();

                    CurrentUser.set(user);

                    if ("ORGANIZER".equals(role)) {
                        Intent intent = new Intent(this, OrgHomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent); // this is how you swap screens!
                        finish();
                    }
                    else {
                        Intent intent = new Intent(this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
