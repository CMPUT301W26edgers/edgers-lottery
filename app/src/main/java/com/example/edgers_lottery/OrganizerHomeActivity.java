package com.example.edgers_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OrganizerHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_home);

        TextView tvUsername = findViewById(R.id.tvUsername);
        TextView tvUserID = findViewById(R.id.tvUserID);

        // Set your user data here
        tvUsername.setText("@username");
        tvUserID.setText("@userID");

        // ✅ Navigate to CreateEditEventActivity
        TextView btnCreateEvent = findViewById(R.id.btnCreateEvent);
        btnCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateEditEventActivity.class);
            startActivity(intent);
            finish();
        });

        TextView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        TextView btnEventsList = findViewById(R.id.btnEventsList);
        btnEventsList.setOnClickListener(v -> {
            // navigate to your events list activity
        });
    }
}