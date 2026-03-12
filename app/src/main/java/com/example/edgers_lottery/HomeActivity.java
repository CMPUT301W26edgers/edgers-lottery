package com.example.edgers_lottery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
;


public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    protected static User user;
    private void showUserInfoDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("User Info")
                .setMessage("Name: " + user.getName() + "\nEmail: " + user.getEmail())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        user = CurrentUser.get(); // already loaded in StartActivity
        
        if (user != null) {
            showUserInfoDialog(user);
        }
        ImageButton profileButton = findViewById(R.id.ProfileButton);
        Button historyButton = findViewById(R.id.btnHistory);

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, EventHistoryActivity.class);
            startActivity(intent);
        });
    }

}
