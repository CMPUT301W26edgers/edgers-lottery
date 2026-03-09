package com.example.edgers_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
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
        setContentView(R.layout.activity_profile);
        user = CurrentUser.get(); // already loaded in StartActivity

        ImageButton homeButton = findViewById(R.id.HomeButton);
        Button deleteProfileButton = findViewById(R.id.deleteProfileButton);
        Button signoutButton = findViewById(R.id.signoutButton);
        // ImageButton checkoutButton = findViewById(R.id.checkoutButton);


        homeButton.setOnClickListener(v -> {
            String role = user.getRole();
            if (role.equals("ORGANIZER")){
                Intent intent = new Intent(this, OrgHomeActivity.class);
                startActivity(intent);
                finish();
            }
            else if (role.equals("ADMIN")) {
                Intent intent = new Intent(this, AdminHomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        deleteProfileButton.setOnClickListener(v -> {

        });

    }

}