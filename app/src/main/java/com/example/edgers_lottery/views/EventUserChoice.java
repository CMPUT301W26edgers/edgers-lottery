package com.example.edgers_lottery.views;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.CurrentUser;
import com.example.edgers_lottery.models.Event;
import com.example.edgers_lottery.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Activity that allows a user to accept or decline an event invitation.
 * Loads the event from Firestore using the {@code eventId} intent extra,
 * displays event details, and updates the entrants and invited users lists in Firestore
 * based on the user's choice.
 */
public class EventUserChoice extends AppCompatActivity {

    private ImageView btnBack;
    private Button btnAcceptInvite;
    private Button btnRejectInvite;
    private TextView tvDescriptionTitle;
    private TextView tvLocationName;
    private TextView tvEventDate;
    private TextView tvDescriptionBody;

    private User currentUser;
    private Event currentEvent;
    private String currentEventId;

    private FirebaseFirestore db;

    /**
     * Initializes the activity, loads event data from Firestore, populates the UI,
     * and redirects the user if they have already accepted the invitation.
     *
     * @param savedInstanceState saved state from a previous instance, or null if first creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_acceptance_choice);

        db = FirebaseFirestore.getInstance();
        currentUser = CurrentUser.get();

        btnBack = findViewById(R.id.btn_back);
        btnAcceptInvite = findViewById(R.id.btn_accept_invite);
        btnRejectInvite = findViewById(R.id.btn_decline_invite);
        tvDescriptionTitle = findViewById(R.id.tv_description_title);
        tvLocationName = findViewById(R.id.tv_location_name);
        tvEventDate = findViewById(R.id.tv_event_date);
        tvDescriptionBody = findViewById(R.id.tv_description_body);

        currentEventId = getIntent().getStringExtra("eventId");

        if (currentEventId != null) {
            db.collection("events").document(currentEventId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            currentEvent = documentSnapshot.toObject(Event.class);
                            currentEvent.setId(documentSnapshot.getId());

                            if (isUserInList(currentUser.getId(), currentEvent.getEntrants())) {
                                Toast.makeText(EventUserChoice.this, "You have already accepted this invitation!", Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }

                            tvDescriptionTitle.setText(currentEvent.getName());

                            if (currentEvent.getDescription() != null) {
                                tvDescriptionBody.setText(currentEvent.getDescription());
                            }
                            if (currentEvent.getLocation() != null) {
                                tvLocationName.setText(currentEvent.getLocation());
                            }
                            if (currentEvent.getDate() != null && currentEvent.getTime() != null) {
                                tvEventDate.setText(currentEvent.getDate() + " at " + currentEvent.getTime());
                            } else if (currentEvent.getDate() != null) {
                                tvEventDate.setText(currentEvent.getDate());
                            }
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

        setupClickListeners();
    }

    /**
     * Attaches click listeners to the back, accept, and decline buttons.
     */
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnAcceptInvite.setOnClickListener(v -> processAccept());
        btnRejectInvite.setOnClickListener(v -> showDeclineConfirmationDialog());
    }

    /**
     * Shows a confirmation dialog before processing the user's decline of the invitation.
     */
    private void showDeclineConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Decline Invitation")
                .setMessage("Are you sure you want to decline this invitation? \n \nThis action cannot be undone and your spot will be given to another person on the waitlist.")
                .setPositiveButton("Yes, Decline", (dialog, which) -> processDecline())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Removes the current user from the event's invited list in Firestore.
     * Disables the decline button while the update is in progress.
     */
    private void processDecline() {
        btnRejectInvite.setEnabled(false);
        btnRejectInvite.setText("Processing...");

        if (currentEventId == null && currentEvent != null) {
            currentEventId = currentEvent.getId();
        }

        if (currentUser == null) {
            Toast.makeText(this, "Error: Current user not found!", Toast.LENGTH_LONG).show();
            btnRejectInvite.setEnabled(true);
            btnRejectInvite.setText("Reject Invite");
            return;
        }

        if (currentEvent.getInvitedUsers() != null) {
            removeUserFromListSafely(currentUser.getId(), currentEvent.getInvitedUsers());
        }

        db.collection("events").document(currentEventId)
                .update("invitedUsers", currentEvent.getInvitedUsers())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EventUserChoice.this, "Invitation declined successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EventUserChoice.this, "Error declining invite: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnRejectInvite.setEnabled(true);
                    btnRejectInvite.setText("Reject Invite");
                });
    }

    /**
     * Adds the current user to the event's entrants list and removes them from the invited list in Firestore.
     * Disables both buttons while the update is in progress.
     */
    private void processAccept() {
        btnAcceptInvite.setEnabled(false);
        btnRejectInvite.setEnabled(false);
        btnAcceptInvite.setText("Processing...");

        if (currentEventId == null && currentEvent != null) {
            currentEventId = currentEvent.getId();
        }

        if (currentUser == null) {
            Toast.makeText(this, "Error: Current user not found!", Toast.LENGTH_LONG).show();
            btnAcceptInvite.setEnabled(true);
            btnRejectInvite.setEnabled(true);
            btnAcceptInvite.setText("Accept Invite");
            return;
        }

        if (currentEvent.getEntrants() == null) {
            currentEvent.setEntrants(new ArrayList<>());
        }
        if (!isUserInList(currentUser.getId(), currentEvent.getEntrants())) {
            currentEvent.getEntrants().add(currentUser);
        }

        if (currentEvent.getInvitedUsers() != null) {
            removeUserFromListSafely(currentUser.getId(), currentEvent.getInvitedUsers());
        }

        db.collection("events").document(currentEventId)
                .update(
                        "entrants", currentEvent.getEntrants(),
                        "invitedUsers", currentEvent.getInvitedUsers()
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

    /**
     * Checks whether a user with the given ID exists in the provided list.
     *
     * @param targetUserId the ID of the user to search for
     * @param userList     the list of {@link User} objects to search
     * @return true if the user is found, false otherwise
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
     * Removes a user from the list by their ID, iterating in reverse to avoid index issues.
     *
     * @param targetUserId the ID of the user to remove
     * @param userList     the list to remove the user from
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