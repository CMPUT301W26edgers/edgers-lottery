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

        // 4. Link UI Elements to XML IDs
        btnBack = findViewById(R.id.btn_back);
        btnAcceptInvite = findViewById(R.id.btn_accept_invite);
        btnRejectInvite = findViewById(R.id.btn_decline_invite);
        tvDescriptionTitle = findViewById(R.id.tv_description_title);
        // Add more fields here as needed based on your XML (e.g., location, date)

        // 5. Populate the screen with data
        tvDescriptionTitle.setText(currentEvent.getName());

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

    // Handles the Firestore Database update
//    private void processDecline() {
//        // Disable the button so they can't click it twice while it loads
//        btnRejectInvite.setEnabled(false);
//        btnRejectInvite.setText("Processing...");
//
//        // 1. Remove the user from the local list
//        if (currentEvent.getEntrants() != null) {
//            currentEvent.getEntrants().remove(currentUser);
//        }
//
//        // 2. Push the updated list to Firebase Firestore
//        // We look for the "events" collection, find the specific event document, and update the "entrants" field
//        db.collection("events").document(currentEventId)
//                .update("entrants", currentEvent.getEntrants())
//                .addOnSuccessListener(aVoid -> {
//                    // Success! The database was updated.
//                    Toast.makeText(EventUserChoice.this, "Invitation declined successfully.", Toast.LENGTH_SHORT).show();
//
//                    // Close the screen and send them back
//                    finish();
//                })
//                .addOnFailureListener(e -> {
//                    // Uh oh, something went wrong (e.g., no internet)
//                    Toast.makeText(EventUserChoice.this, "Error declining invite: " + e.getMessage(), Toast.LENGTH_LONG).show();
//
//                    // Re-enable the button so they can try again
//                    btnRejectInvite.setEnabled(true);
//                    btnRejectInvite.setText("Reject Invite");
//                });
//    }

    private void processDecline() {
        btnRejectInvite.setEnabled(false);
        btnRejectInvite.setText("Processing...");

        // 🛠️ CHANGED: We DO NOT touch the "entrants" list here anymore.
        // Because they aren't in the entrants list, we have nothing to remove from it!
        // Instead, this is where you will update the list they *were* on (like an invited list).

        /* TODO: Once your teammate updates the Event class, add this logic:
        1. currentEvent.getInvitedUsers().remove(currentUser);
        2. db.collection("events").document(currentEventId).update("invitedUsers", currentEvent.getInvitedUsers())
        */

        // For now, since Issue #12 is just rejecting, we will just show success and close!
        Toast.makeText(EventUserChoice.this, "Invitation declined successfully.", Toast.LENGTH_SHORT).show();
        finish();
    }


    // Handles the Firestore Database update for Accepting
    private void processAccept() {
        // Disable buttons so they can't double-click while it loads
        btnAcceptInvite.setEnabled(false);
        btnRejectInvite.setEnabled(false);
        btnAcceptInvite.setText("Processing...");

        // 1. Safety check: If the entrants list is completely empty/null, create it first
        if (currentEvent.getEntrants() == null) {
            currentEvent.setEntrants(new ArrayList<>());
        }

        // 2. Add the user to the local list (only if they aren't somehow already in it)
        if (!currentEvent.getEntrants().contains(currentUser)) {
            currentEvent.getEntrants().add(currentUser);
        }

        // 3. Push the updated list to Firebase Firestore
        db.collection("events").document(currentEventId)
                .update("entrants", currentEvent.getEntrants())
                .addOnSuccessListener(aVoid -> {
                    // Success! The database was updated.
                    Toast.makeText(EventUserChoice.this, "Invitation accepted! You're on the list.", Toast.LENGTH_SHORT).show();

                    // Close the screen
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Uh oh, something went wrong
                    Toast.makeText(EventUserChoice.this, "Error accepting invite: " + e.getMessage(), Toast.LENGTH_LONG).show();

                    // Re-enable the buttons so they can try again
                    btnAcceptInvite.setEnabled(true);
                    btnRejectInvite.setEnabled(true);
                    btnAcceptInvite.setText("Accept Invite");
                });
    }
}