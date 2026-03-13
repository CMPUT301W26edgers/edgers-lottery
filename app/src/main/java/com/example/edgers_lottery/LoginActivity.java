package com.example.edgers_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity that handles user login via Firebase Authentication.
 * Provides fields for email and password, and navigates to {@link NewUserActivity}
 * for new account creation or back to {@link StartActivity} on successful login.
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    protected User user;
    TextInputEditText emailEditText, passwordEditText;

    /**
     * Initializes the activity, sets up the email and password fields,
     * and attaches listeners to the login and create account buttons.
     *
     * @param savedInstanceState saved state from a previous instance, or null if first creation
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        findViewById(R.id.continueUserButton).setOnClickListener(v -> login());
        findViewById(R.id.createAccountButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, NewUserActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Validates the email and password fields and attempts Firebase Authentication sign-in.
     * Navigates to {@link StartActivity} on success, or shows an error toast on failure.
     */
    private void login() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, StartActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}