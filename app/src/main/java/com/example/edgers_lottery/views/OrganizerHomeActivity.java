package com.example.edgers_lottery.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.edgers_lottery.models.CurrentUser;
import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.User;

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
        User currentUser = CurrentUser.get();

        TextView tvUsername = findViewById(R.id.tvUsername);
        TextView tvUserID = findViewById(R.id.tvUserID);
        ImageButton backButton = findViewById(R.id.backButton);


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
            Intent intent = new Intent(this, OrganizerEventsListActivity.class);
            startActivity(intent);
        });
        backButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Go Back")
                    .setMessage("Are you sure you want to go back to User Home?")
                    .setPositiveButton("Yes", (dialog, which) -> {
//                        currentUser.setRole("ENTRANT");
                        Intent intent = new Intent(this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}