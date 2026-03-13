package com.example.edgers_lottery;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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

/**
 * Activity that displays the details of a single event to an entrant.
 * Allows the current user to join or leave the event waitlist.
 * Loads event data from Firestore using the {@code eventId} passed via intent extra.
 */
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
    ArrayList<User> waitingList;
    private static final String TAG = "EventDetailsActivity";
    protected User user;
    private String eventId;

    /**
     * Initializes the activity, loads event data from Firestore using the provided event ID,
     * and sets up the back button.
     *
     * @param savedInstanceState saved state from a previous instance, or null if first creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());
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
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        if (eventId != null) {
            db.collection("events").document(eventId).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            Event event = document.toObject(Event.class);
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
            finish();
        }
    }

    /**
     * Populates the UI with event details and configures the join/leave waitlist button
     * based on the current user's waitlist status and available capacity.
     * Also sets up the waitlist viewer dialog button.
     *
     * @param event the {@link Event} object to display
     */
    private void showEvent(Event event) {
        eventNameText.setText(event.getName());
        eventDescriptionText.setText(event.getDescription());
        eventDateText.setText(event.getDate());
        eventTimeText.setText(event.getTime());
        eventLocationText.setText(event.getLocation());
        capacity = event.getCapacity();
        entrantCount = (event.getEntrants() == null) ? 0 : event.getEntrants().size();
        eventCapacityText.setText(String.format("Capacity: %d", capacity));

        if (isUserInList(user.getId(), waitingList)) {
            joinButton.setText("Leave Waitlist");
            joinButton.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        } else if (capacity > 0 && waitingList.size() >= capacity) {
            joinButton.setEnabled(false);
            joinButton.setText("Waitlist Full");
            joinButton.setBackgroundTintList(ColorStateList.valueOf(Color.YELLOW));
        } else {
            joinButton.setText("Join Waitlist");
            joinButton.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
        }

        joinButton.setOnClickListener(v -> {
            joinButton.setEnabled(false);

            if (isUserInList(user.getId(), waitingList)) {
                removeUserFromListSafely(user.getId(), waitingList);
                joinButton.setText("Join Waitlist");
                joinButton.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            } else {
                addUserToList(user, waitingList);
                joinButton.setText("Leave Waitlist");
                joinButton.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            }

            db.collection("events").document(eventId)
                    .update("waitingList", waitingList)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Waitlist updated successfully!", Toast.LENGTH_SHORT).show();
                        joinButton.setEnabled(true);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update waitlist.", Toast.LENGTH_SHORT).show();
                        joinButton.setEnabled(true);
                    });
        });

        waitlistButton.setOnClickListener(v -> {
            StringBuilder list = new StringBuilder();
            for (User u : waitingList) {
                list.append(u.getName()).append("\n");
            }
            new AlertDialog.Builder(this)
                    .setTitle("Waitlist (" + waitingList.size() + " users)")
                    .setMessage(list.toString())
                    .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    /**
     * Checks whether a user with the given ID exists in the provided list.
     *
     * @param targetUserId the ID of the user to search for
     * @param userList     the list of users to search
     * @return true if the user is found, false otherwise
     */
    public static boolean isUserInList(String targetUserId, ArrayList<User> userList) {
        if (userList == null || targetUserId == null) return false;
        for (User user : userList) {
            if (user.getId() != null && user.getId().equals(targetUserId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a user to the waiting list if they are not already in it.
     *
     * @param user        the {@link User} to add
     * @param waitingList the list to add the user to
     */
    public static void addUserToList(User user, ArrayList<User> waitingList) {
        if (user == null || waitingList == null) return;
        if (!isUserInList(user.getId(), waitingList)) {
            waitingList.add(user);
        }
    }

    /**
     * Removes a user from the list by their ID, iterating in reverse to avoid index issues.
     *
     * @param targetUserId the ID of the user to remove
     * @param userList     the list to remove the user from
     */
    public static void removeUserFromListSafely(String targetUserId, ArrayList<User> userList) {
        if (userList == null || targetUserId == null) return;
        for (int i = userList.size() - 1; i >= 0; i--) {
            if (userList.get(i).getId() != null && userList.get(i).getId().equals(targetUserId)) {
                userList.remove(i);
                break;
            }
        }
    }
}