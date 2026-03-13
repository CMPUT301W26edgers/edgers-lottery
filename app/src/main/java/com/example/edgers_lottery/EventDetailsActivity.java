package com.example.edgers_lottery;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;

import java.util.ArrayList;

import kotlin.text.UStringsKt;


public class EventDetailsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private ImageView backButton;
    private TextView eventNameText;

    private TextView eventDescriptionText;
    private TextView eventDateText;
    private TextView eventTimeText;
    private TextView eventLocationText;
    private TextView eventCapacityText;
    private Button joinButton;
    private Button waitlistButton;
    private int capacity;
    private int entrantCount;
    private ArrayList<User> waitingList;
    private static final String TAG = "EventDetailsActivity";

    protected User user;

    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        backButton = findViewById(R.id.btn_back);

        backButton.setOnClickListener(v -> {
            finish();
        });
        db = FirebaseFirestore.getInstance();
        eventNameText = findViewById(R.id.event_name);
        eventDescriptionText = findViewById(R.id.event_description);
        eventDateText = findViewById(R.id.date_text);
        eventTimeText = findViewById(R.id.event_time);
        eventLocationText = findViewById(R.id.location_name);
        eventCapacityText = findViewById(R.id.event_capacity);
        joinButton = findViewById(R.id.join_button);
        waitlistButton = findViewById(R.id.view_waitlist);
        user = CurrentUser.get();

        eventId = getIntent().getStringExtra("eventId");

        if (eventId != null) {
            db.collection("events").document(eventId).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            Event event = document.toObject(Event.class);
                            // Set the ID here too, just in case!
                            if (event != null) {
                                event.setId(document.getId());
                                waitingList = event.getWaitingList() != null
                                        ? event.getWaitingList()
                                        : new ArrayList<>();
                                showEvent(event);
                            }
                        } else {
                            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show()
                    );
        } else {
            Toast.makeText(this, "Error: No Event ID provided.", Toast.LENGTH_SHORT).show();
            finish(); // Go back if there's no ID
        }
    }


    private void showEvent(Event event) {
        eventNameText.setText(event.getName());
        eventDescriptionText.setText(event.getDescription());
        eventDateText.setText(event.getDate());
        eventTimeText.setText(event.getTime());
        eventLocationText.setText(event.getLocation());
        capacity = event.getCapacity();
        entrantCount = (event.getEntrants() == null) ? 0 : event.getEntrants().size();
        eventCapacityText.setText(String.format("Capacity: %d", capacity));

        // Bulletproof check for initial button state
        if (isUserInList(user.getId(), waitingList)) {
            joinButton.setText("Leave Waitlist");
            joinButton.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        } else {
            joinButton.setText("Join Waitlist");
            joinButton.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
        }

        joinButton.setOnClickListener(v -> {
            // Disable button temporarily to prevent spam clicking while Firebase updates
            joinButton.setEnabled(false);

            if (isUserInList(user.getId(), waitingList)) {
                // Safely remove them
                removeUserFromListSafely(user.getId(), waitingList);
                joinButton.setText("Join Waitlist");
                joinButton.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            } else {
                // Add them
                waitingList.add(user);
                joinButton.setText("Leave Waitlist");
                joinButton.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            }

            // ☁️ FIREBASE UPDATE: Actually save this new list to the cloud!
            db.collection("events").document(eventId)
                    .update("waitingList", waitingList)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Waitlist updated successfully!", Toast.LENGTH_SHORT).show();
                        joinButton.setEnabled(true); // Re-enable button
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update waitlist.", Toast.LENGTH_SHORT).show();
                        joinButton.setEnabled(true); // Re-enable button even if it fails
                    });
        });

        waitlistButton.setOnClickListener(v -> {
            StringBuilder list = new StringBuilder();

            for (User u : waitingList) {
                list.append(u.getName()).append("\n");
            }
            int totalUsers = waitingList.size();
            new AlertDialog.Builder(this)
                    .setTitle("Waitlist (" + totalUsers + " users)")
                    .setMessage(list.toString())
                    .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    /**
     * Helper method to safely check if our user's ID is already in the list.
     */
    private boolean isUserInList(String targetUserId, ArrayList<User> userList) {
        if (userList == null || targetUserId == null) return false;
        for (User user : userList) {
            if (user.getId() != null && user.getId().equals(targetUserId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper method to safely remove a user from a list using their ID.
     */
    private void removeUserFromListSafely(String targetUserId, ArrayList<User> userList) {
        if (userList == null || targetUserId == null) return;
        for (int i = userList.size() - 1; i >= 0; i--) {
            if (userList.get(i).getId() != null && userList.get(i).getId().equals(targetUserId)) {
                userList.remove(i);
                break;
            }
        }
    }
}