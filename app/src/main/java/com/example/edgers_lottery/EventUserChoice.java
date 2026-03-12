package com.example.edgers_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class EventUserChoice extends AppCompatActivity {

    // 1. UI Elements
    private ImageView btnBack;
    private Button btnAcceptInvite;
    private Button btnRejectInvite;
    private TextView tvDescriptionTitle;
    private TextView tvEventPrice;

    // 2. Data Objects
    private User currentUser;
    private Event currentEvent;
    private String currentEventId; // We need the document ID to update Firestore!

    // 3. Firebase Database
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_acceptance_choice);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // get the current user details
        currentUser = CurrentUser.get();

        // 4. Link UI Elements to XML IDs
        btnBack = findViewById(R.id.btn_back);
        btnAcceptInvite = findViewById(R.id.btn_accept_invite);
        btnRejectInvite = findViewById(R.id.btn_decline_invite);
        tvDescriptionTitle = findViewById(R.id.tv_description_title);
        // Add more fields here as needed based on your XML (e.g., location, date)

        currentEventId = getIntent().getStringExtra("eventId");

        if (currentEventId != null) {
            // Fetch the real, up-to-date event from Firestore
            db.collection("events").document(currentEventId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Convert the Firebase document back into an Event object
                            currentEvent = documentSnapshot.toObject(Event.class);
                            currentEvent.setId(documentSnapshot.getId()); // ensure ID is set

                            // if the user previously accepted to the event, we kick them out!
                            if (isUserInList(currentUser.getId(), currentEvent.getEntrants())) {
                                Toast.makeText(EventUserChoice.this, "You have already accepted this invitation!", Toast.LENGTH_SHORT).show();
                                finish(); // Kick them out immediately
                                return;   // Stop running the rest of the code
                            }

                            // Now that we have the data, update the UI!
                            tvDescriptionTitle.setText(currentEvent.getName());
                        } else {
                            Toast.makeText(this, "Event no longer exists.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load event data.", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            Toast.makeText(this, "Error: No Event ID provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 6. Set up Click Listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Back Button: close this screen
        btnBack.setOnClickListener(v -> finish());

        // Accept Button: Add user to event's entrants list
        btnAcceptInvite.setOnClickListener(v -> processAccept());

        // Reject Button: get decline confirmation from user
        btnRejectInvite.setOnClickListener(v -> showDeclineConfirmationDialog());
    }

    // Displays the popup asking "Are you sure?"
    private void showDeclineConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Decline Invitation")
                .setMessage("Are you sure you want to decline this invitation? \n \nThis action cannot be undone and your spot will be given to another person on the waitlist.")
                .setPositiveButton("Yes, Decline", (dialog, which) -> {
                    // User confirmed! Execute the database update.
                    processDecline();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User changed their mind. Dismiss the dialog.
                    dialog.dismiss();
                })
                .show();
    }

    private void processDecline() {
        // Disable the button so they can't click it twice while it loads
        btnRejectInvite.setEnabled(false);
        btnRejectInvite.setText("Processing...");

        // Safety Check 1: Ensure we have the Event ID
        if (currentEventId == null && currentEvent != null) {
            currentEventId = currentEvent.getId();
        }

        // Safety Check 2: Ensure we actually have a logged-in user!
        if (currentUser == null) {
            Toast.makeText(this, "Error: Current user not found!", Toast.LENGTH_LONG).show();
            btnRejectInvite.setEnabled(true);
            btnRejectInvite.setText("Reject Invite");
            return;
        }

        // 1. Remove the user from the local invited list
        if (currentEvent.getInvitedUsers() != null) {
            // NOTE: For this to work perfectly, your User class MUST have overridden the .equals() method!
            currentEvent.getInvitedUsers().remove(currentUser);
        }

        // 2. Push the updated list to Firebase Firestore
        db.collection("events").document(currentEventId)
                .update("invitedUsers", currentEvent.getInvitedUsers())
                .addOnSuccessListener(aVoid -> {
                    // Success! The database was updated.
                    Toast.makeText(EventUserChoice.this, "Invitation declined successfully.", Toast.LENGTH_SHORT).show();

                    // Close the screen and send them back
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Uh oh, something went wrong
                    Toast.makeText(EventUserChoice.this, "Error declining invite: " + e.getMessage(), Toast.LENGTH_LONG).show();

                    // Re-enable the button so they can try again
                    btnRejectInvite.setEnabled(true);
                    btnRejectInvite.setText("Reject Invite");
                });
    }


    // Handles the Firestore Database update for Accepting
    private void processAccept() {
        // Disable buttons so they can't double-click while it loads
        btnAcceptInvite.setEnabled(false);
        btnRejectInvite.setEnabled(false);
        btnAcceptInvite.setText("Processing...");

        // Safety Check 1: Ensure we have the Event ID
        if (currentEventId == null && currentEvent != null) {
            currentEventId = currentEvent.getId();
        }

        // Safety Check 2: Ensure we actually have a logged-in user!
        if (currentUser == null) {
            Toast.makeText(this, "Error: Current user not found!", Toast.LENGTH_LONG).show();
            // Re-enable buttons
            btnAcceptInvite.setEnabled(true);
            btnRejectInvite.setEnabled(true);
            btnAcceptInvite.setText("Accept Invite");
            return;
        }

        // 1. Add to entrants list
        if (currentEvent.getEntrants() == null) {
            currentEvent.setEntrants(new ArrayList<>());
        }
        if (!isUserInList(currentUser.getId(), currentEvent.getEntrants())) {
            currentEvent.getEntrants().add(currentUser);
        }

        // 2. Remove from invited list
        if (currentEvent.getInvitedUsers() != null) {
            // NOTE: For this to work perfectly, your User class MUST have overridden the .equals() method!
            currentEvent.getInvitedUsers().remove(currentUser);
        }

        // 3. Push BOTH updated lists to Firebase Firestore
        db.collection("events").document(currentEventId)
                .update(
                        "entrants", currentEvent.getEntrants(),
                        "invitedUsers", currentEvent.getInvitedUsers() // 🟢 We must update this field too!
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EventUserChoice.this, "Invitation accepted! You're on the list.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EventUserChoice.this, "Error accepting invite: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnAcceptInvite.setEnabled(true);
                    btnRejectInvite.setEnabled(true);
                    btnAcceptInvite.setText("Accept Invite");
                });
    }

    private boolean isUserInList(String targetUserId, ArrayList<User> userList) {
        if (userList == null || targetUserId == null) return false;
        for (User user : userList) {
            if (user.getId() != null && user.getId().equals(targetUserId)) {
                return true;
            }
        }
        return false;
    }
}