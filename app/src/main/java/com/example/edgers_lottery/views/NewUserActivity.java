package com.example.edgers_lottery.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edgers_lottery.models.CurrentUser;
import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity that handles new user account creation via Firebase Authentication.
 * Collects first name, last name, email, and password, then registers the user
 * as an entrant and saves their profile to Firestore.
 */
public class NewUserActivity extends AppCompatActivity {

    private TextInputEditText firstNameEditText, lastNameEditText, emailEditText, passwordEditText;

    /**
     * Initializes the activity, binds input fields, and attaches listeners
     * to the register and login buttons.
     *
     * @param savedInstanceState saved state from a previous instance, or null if first creation
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountcreation);

        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        findViewById(R.id.continueUserButton).setOnClickListener(v -> registerUser());
        findViewById(R.id.logInButton).setOnClickListener(v -> loginAsExistingUser());
    }

    /**
     * Validates all input fields and creates a new Firebase Authentication account with the ENTRANT role.
     * Calls {@link #saveUserToFirestore} on success.
     */
    private void registerUser() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    saveUserToFirestore(uid, firstName, lastName, email, "ENTRANT");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Validates all input fields and creates a new Firebase Authentication account with the ORGANIZER role.
     * Calls {@link #saveUserToFirestore} on success.
     *
     * @deprecated This method is decommissioned and no longer called.
     */
    private void registerOrganizer() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    saveUserToFirestore(uid, firstName, lastName, email, "ORGANIZER");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Navigates to {@link LoginActivity} for users who already have an account.
     */
    private void loginAsExistingUser() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Creates a {@link User} object and saves it to Firestore, then navigates to the
     * appropriate home screen based on the user's role.
     *
     * @param uid       the Firebase Authentication UID for the new user
     * @param firstName the user's first name
     * @param lastName  the user's last name
     * @param email     the user's email address
     * @param role      the user's role, either {@code "ENTRANT"} or {@code "ORGANIZER"}
     */
    private void saveUserToFirestore(String uid, String firstName, String lastName, String email, String role) {
        User user = new User(uid, firstName + " " + lastName, email, role);
        FirebaseFirestore.getInstance().collection("users").document(uid).set(user)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "User created successfully", Toast.LENGTH_SHORT).show();
                    CurrentUser.set(user);
                    if ("ORGANIZER".equals(role)) {
                        Intent intent = new Intent(this, OrganizerHomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
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