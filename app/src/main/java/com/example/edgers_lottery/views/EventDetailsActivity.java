package com.example.edgers_lottery.views;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.edgers_lottery.models.CurrentUser;
import com.example.edgers_lottery.models.Event;
import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.User;
import com.example.edgers_lottery.services.CommentService;
import com.example.edgers_lottery.services.NotificationService;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;

import java.util.ArrayList;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

/**
 * Activity that displays the details of a single event to an entrant.
 * Allows the current user to join or leave the event waitlist.
 * Loads event data from Firestore using the {@code eventId} passed via intent extra.
 */
public class EventDetailsActivity extends AppCompatActivity {

    /** Firestore database instance used to read and update event data. */
    private FirebaseFirestore db;

    /** Button that navigates back to the previous screen. */
    private ImageView backButton;
    private ImageView eventposter;

    /** Displays the name of the event. */
    private TextView eventNameText;

    /** Displays the description of the event. */
    private TextView eventDescriptionText;

    /** Displays the date of the event. */
    private TextView eventDateText;

    /** Displays the time of the event. */
    private TextView eventTimeText;

    /** Displays the location of the event. */
    private TextView eventLocationText;

    /** Displays the maximum capacity of the event. */
    private TextView eventCapacityText;

    /** Button that allows the current user to join or leave the waitlist. */
    private Button joinButton;

    /** Button that opens a dialog showing all current waitlist members. */
    private Button waitlistButton;
    private String imageURL;
    private Button deleteButton;
    private Button viewCommentsButton;

    /** The maximum number of entrants allowed for this event. */

    private int capacity;

    /** The current number of confirmed entrants for this event. */
    private int entrantCount;

    /** The list of users currently on the waiting list for this event. */
    ArrayList<User> waitingList;

    /** Tag used for logging within this activity. */
    private static final String TAG = "EventDetailsActivity";

    /** The currently logged-in user. */
    protected User user;

    /** The Firestore document ID of the event being displayed. */
    private String eventId;

    /** Client used to grab device location */
    private FusedLocationProviderClient fusedLocationClient;

    /** Handles the system permission pop-up for location */
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {

                // Using standard .get() instead of .getOrDefault() to support API 23
                Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarseLocationGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                if ((fineLocationGranted != null && fineLocationGranted) ||
                        (coarseLocationGranted != null && coarseLocationGranted)) {
                    // Permission granted! Go get the location.
                    getLocationAndExecuteJoin();
                } else {
                    // Permission denied.
                    Toast.makeText(this, "Location permission is required to join.", Toast.LENGTH_SHORT).show();
                    joinButton.setEnabled(true);
                }
            });

    /**
     * Initializes the activity, binds UI components, retrieves the event ID
     * from the launching Intent, and loads the event data from Firestore.
     *
     * @param savedInstanceState the previously saved instance state, or {@code null}
     *                           if this is a fresh start
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            finish();
        });
        eventposter = findViewById(R.id.imageView2);
        db = FirebaseFirestore.getInstance();
        eventNameText = findViewById(R.id.event_name);
        eventDescriptionText = findViewById(R.id.event_description);
        eventDateText = findViewById(R.id.date_text);
        eventTimeText = findViewById(R.id.event_time);
        eventLocationText = findViewById(R.id.location_name);
        eventCapacityText = findViewById(R.id.event_capacity);
        joinButton = findViewById(R.id.join_button);
        waitlistButton = findViewById(R.id.view_waitlist);
        deleteButton = findViewById(R.id.delete_event_button);
        viewCommentsButton = findViewById(R.id.btnViewComments);

        viewCommentsButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailsActivity.this, EventCommentsActivity.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });


        // Initialize the user
        user = CurrentUser.get();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check if the current user is an Admin
        if (user != null && User.Role.ADMIN.name().equals(user.getRole())) {
            // Make the delete button visible
            deleteButton.setVisibility(View.VISIBLE);

            // Trigger the confirmation popup when clicked
            deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
        }


        eventId = getIntent().getStringExtra("eventId");
        //ImageButton backButton = findViewById(R.id.backButton);
        //backButton.setOnClickListener(v -> finish());

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
        imageURL = event.getPoster();
        if (imageURL != null && !imageURL.isEmpty()) {
            Glide.with(this)
                    .load(imageURL)
                    .placeholder(R.drawable.blankphoto)
                    .error(R.drawable.blankphoto)
                    .into(eventposter);
        } else {
            eventposter.setImageResource(R.drawable.blankphoto);
        }

        if (isUserInList(user.getId(), waitingList)) {
            joinButton.setText("Leave Waitlist");
            joinButton.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        } else if (capacity > 0 && waitingList.size() >= capacity) {
            joinButton.setEnabled(false);
            joinButton.setText("Waitlist Full");
            joinButton.setBackgroundTintList(ColorStateList.valueOf(Color.YELLOW));
            joinButton.setTextColor(Color.BLACK);
        } else {
            joinButton.setText("Join Waitlist");
            joinButton.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
        }

        joinButton.setOnClickListener(v -> {
            joinButton.setEnabled(false);

            if (isUserInList(user.getId(), waitingList)) {
                // LEAVING — no location needed, just remove and push
                removeUserFromListSafely(user.getId(), waitingList);
                joinButton.setText("Join Waitlist");
                joinButton.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                updateWaitlistInFirestore(null); // null = no notification
            } else {
                // JOINING — go through location check first
                checkLocationPermissionAndJoin();
                // Button stays disabled until location flow completes inside getLocationAndExecuteJoin()
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

    /**
     * Determines whether the waitlist has reached or exceeded the event capacity.
     *
     * @param capacity     the maximum number of users allowed on the waitlist
     * @param waitingList  the list of users currently on the waitlist
     * @return true if the waitlist size is greater than or equal to the capacity,
     *         false if the list is null, capacity is non-positive, or the waitlist
     *         has not yet reached capacity
     */
    public static boolean isWaitlistFull(int capacity, ArrayList<User> waitingList) {
        if (waitingList == null) return false;
        return capacity > 0 && waitingList.size() >= capacity;
    }

    /**
     * Displays an alert dialog confirming the admin wants to delete the event.
     * If confirmed, deletes the document from Firestore, deletes its comments,
     * and closes the activity.
     */
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {

                    // 1. Delete the event from the Firestore "events" collection
                    db.collection("events").document(eventId).delete()
                            .addOnSuccessListener(aVoid -> {

                                // 2. Cascade delete: Remove all comments associated with this event
                                CommentService.deleteCommentsOnEvent(eventId);

                                // 3. Show success message
                                Toast.makeText(EventDetailsActivity.this, "Event and associated comments deleted.", Toast.LENGTH_SHORT).show();

                                // 4. Close the details screen and return to the event list
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(EventDetailsActivity.this, "Failed to delete event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // User canceled, just dismiss the dialog
                    dialog.dismiss();
                })
                .show();
    }

    /**
     * Checks if we already have permission. If yes, grabs location. If no, asks user.
     */
    private void checkLocationPermissionAndJoin() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocationAndExecuteJoin();
        } else {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    /**
     * Grabs the GPS coordinates, updates the User object, and adds them to the list.
     */
    @SuppressWarnings("MissingPermission")
    private void getLocationAndExecuteJoin() {
        // First try the fast path: last known location
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // GPS cache was warm — use it directly
                        user.setLatitude(location.getLatitude());
                        user.setLongitude(location.getLongitude());
                        finalizeJoin();
                    } else {
                        // GPS cache is cold — request a fresh single update
                        requestFreshLocation();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Location unavailable. Joining without coordinates.", Toast.LENGTH_SHORT).show();
                    user.setLatitude(null);
                    user.setLongitude(null);
                    finalizeJoin();
                });
    }

    /**
     * Centralized method to push the waitingList array to Firestore.
     */
    private void updateWaitlistInFirestore(String newlyJoinedUserId) {
        db.collection("events").document(eventId)
                .update("waitingList", waitingList)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Waitlist updated successfully!", Toast.LENGTH_SHORT).show();
                    joinButton.setEnabled(true);

                    // If a userId was passed in, it means they just joined, so send the notification
                    if (newlyJoinedUserId != null) {
                        NotificationService.sendWaitlistJoinedNotification(newlyJoinedUserId, eventId, eventNameText.getText().toString());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update waitlist.", Toast.LENGTH_SHORT).show();
                    joinButton.setEnabled(true);
                });
    }

    @SuppressWarnings("MissingPermission")
    private void requestFreshLocation() {
        com.google.android.gms.location.LocationRequest locationRequest =
                com.google.android.gms.location.LocationRequest.create()
                        .setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setNumUpdates(1)           // only need one fix
                        .setInterval(0)
                        .setFastestInterval(0);

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                new com.google.android.gms.location.LocationCallback() {
                    @Override
                    public void onLocationResult(com.google.android.gms.location.LocationResult result) {
                        fusedLocationClient.removeLocationUpdates(this); // stop after one fix
                        android.location.Location loc = result.getLastLocation();
                        if (loc != null) {
                            user.setLatitude(loc.getLatitude());
                            user.setLongitude(loc.getLongitude());
                        } else {
                            user.setLatitude(null);
                            user.setLongitude(null);
                        }
                        finalizeJoin();
                    }
                },
                getMainLooper()
        );
    }

    private void finalizeJoin() {
        addUserToList(user, waitingList);
        joinButton.setText("Leave Waitlist");
        joinButton.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        updateWaitlistInFirestore(user.getId()); // your existing method handles notification + Firestore push
    }
}