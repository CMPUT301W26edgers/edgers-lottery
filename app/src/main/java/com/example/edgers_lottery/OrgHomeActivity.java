package com.example.edgers_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

// can only be accessed by organizers
public class OrgHomeActivity extends AppCompatActivity {
    private static final String TAG = "OrgHomeActivity";
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
        setContentView(R.layout.activity_orghome);
        user = CurrentUser.get(); // already loaded in StartActivity

        if (user != null) {
            showUserInfoDialog(user);
        }

        TextView createEvent = findViewById(R.id.createEventButton).findViewById(R.id.menuTitle);
        TextView eventsList = findViewById(R.id.eventListMenu).findViewById(R.id.menuTitle);

        createEvent.setText("Create Event");
        eventsList.setText("Events List");

        ImageButton profileButton = findViewById(R.id.ProfileButton);
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });
    }

}