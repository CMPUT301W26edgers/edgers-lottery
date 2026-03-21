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
 * Displays the current organizer's info on launch and provides navigation
 * to event creation, the events list, and the organizer's profile.
 * Accessible by organizer accounts only.
 */
public class OrgHomeActivity extends AppCompatActivity {
    private static final String TAG = "OrgHomeActivity";
    protected static User user;

    /**
     * Displays an alert dialog showing the given user's name and email.
     *
     * @param user the {@link User} whose info is displayed
     */
    private void showUserInfoDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("User Info")
                .setMessage("Name: " + user.getName() + "\nEmail: " + user.getEmail())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Initializes the activity, loads the current organizer, displays their info,
     * and sets up navigation buttons for event creation, events list, and profile.
     *
     * @param savedInstanceState saved state from a previous instance, or null if first creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orghome);
        user = CurrentUser.get();

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