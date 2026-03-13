package com.example.edgers_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Home screen activity for organizers.
 * Displays the organizer's username and user ID, and provides navigation
 * to event creation via {@link CreateEditEventActivity} and the organizer's events list.
 */
public class OrganizerHomeActivity extends AppCompatActivity {

    /**
     * Initializes the activity, populates the username and user ID fields,
     * and sets up navigation listeners for the create event and events list buttons.
     *
     * @param savedInstanceState saved state from a previous instance, or null if first creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_home);

        TextView tvUsername = findViewById(R.id.tvUsername);
        TextView tvUserID = findViewById(R.id.tvUserID);

        tvUsername.setText("@username");
        tvUserID.setText("@userID");

        TextView btnCreateEvent = findViewById(R.id.btnCreateEvent);
        btnCreateEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateEditEventActivity.class);
            startActivity(intent);
            finish();
        });

        TextView btnEventsList = findViewById(R.id.btnEventsList);
        btnEventsList.setOnClickListener(v -> {
            // navigate to your events list activity
        });
    }
}