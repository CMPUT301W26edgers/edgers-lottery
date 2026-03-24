package com.example.edgers_lottery.views;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminUserProfileActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private TextView userprofile, name, username, role, description, email, phone, location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_profile);

        db = FirebaseFirestore.getInstance();

        userprofile = findViewById(R.id.userProfile);
        name = findViewById(R.id.profileName);
        username = findViewById(R.id.username);
        role = findViewById(R.id.userRole);
        description = findViewById(R.id.descriptionText);
        email = findViewById(R.id.profileEmail);
        phone = findViewById(R.id.profilePhone);
        location = findViewById(R.id.profileLocation);

        String userId = getIntent().getStringExtra("userId");

        if (userId != null) {
            loadUser(userId);
        }

        // navigate back to user list
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    // load user profile from Firestore
    private void loadUser(String userId) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        userprofile.setText(user.getName() + "'s Profile");
                        name.setText(user.getName());
                        username.setText(user.getUsername());
                        role.setText(user.getRole());
                        description.setText(user.getDescription());
                        email.setText("Email: " + user.getEmail());
                        phone.setText("Phone: " +user.getPhone());
                        location.setText("Location: " +user.getLocation());
                    }
                });
    }
}

