package com.example.edgers_lottery.views;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.WaitlistUser;
import com.example.edgers_lottery.models.WaitlistAdapter;
import com.example.edgers_lottery.services.NotificationService;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * Activity that displays the entrant management screen for an organizer.
 * Provides navigation to the event details, waitlist, and edit event screens.
 * Requires an {@code event_id} intent extra to identify the current event.
 *
 * Long-pressing a user row lets the organizer assign them as a co-organizer
 * (US 02.09.01), which adds them to the event's coOrganizers list and removes
 * them from the waitlist pool.
 */
public class EventEntrantOrganizer extends AppCompatActivity {

    private RecyclerView rvEntrants;
    private TextView tvEntrantCount;
    private WaitlistAdapter adapter;
    private List<WaitlistUser> entrantUsers;
    private FirebaseFirestore db;
    private String eventId;
    private List<Map<String, Object>> invitedUsers = new ArrayList<>();
    private List<Map<String, Object>> AllInvitedUsers = new ArrayList<>();
    private List<Map<String, Object>> declinedUsers = new ArrayList<>();
    private List<Map<String, Object>> acceptedUsers = new ArrayList<>();
    private String currentView = "allInvited";


    /**
     * Initializes the activity, reads the event ID from the intent,
     * and sets up views and navigation listeners.
     *
     * @param savedInstanceState saved state from a previous instance, or null if first creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrants_organizer);
        eventId = getIntent().getStringExtra("event_id");

        db = FirebaseFirestore.getInstance();
        initViews();
        setupListeners();
        setupRecyclerView();
        if (eventId != null) {
            loadEntrants();
        }
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<Map<String, Object>> rawAllInvitedUsers =
                                (List<Map<String, Object>>) doc.get("AllInvitedUsers");
                        List<Map<String, Object>> rawDeclinedUsers =
                                (List<Map<String, Object>>) doc.get("declinedUsers");
                        List<Map<String, Object>> rawAcceptedUsers =
                                (List<Map<String, Object>>) doc.get("entrants");
                        AllInvitedUsers  = rawAllInvitedUsers != null ? rawAllInvitedUsers : new ArrayList<>();
                        declinedUsers   = rawDeclinedUsers   != null ? rawDeclinedUsers   : new ArrayList<>();
                        acceptedUsers   = rawAcceptedUsers   != null ? rawAcceptedUsers   : new ArrayList<>();
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Binds views and attaches a click listener to the back button.
     */
    private void initViews() {
        rvEntrants     = findViewById(R.id.listEntrants);
        tvEntrantCount = findViewById(R.id.entrantCount);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    /**
     * Attaches navigation click listeners to the edit event, event details, and waitlist buttons.
     * Passes the current {@code eventId} to each destination activity via intent extra.
     */
    private void setupListeners() {
        findViewById(R.id.editEventBtn).setOnClickListener(v -> {
            finish();
            Intent intent = new Intent(this, CreateEditEventActivity.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });
        findViewById(R.id.detailBtn).setOnClickListener(v -> {
            finish();
            Intent intent = new Intent(this, EventDetailsOrganizer.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });
        findViewById(R.id.waitListBtn).setOnClickListener(v -> {
            finish();
            Intent intent = new Intent(this, EventWaitlistTab.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });

        Button mapBtn = findViewById(R.id.mapBtn);
        if (mapBtn != null) {
            mapBtn.setOnClickListener(v -> {
                finish();
                Intent intent = new Intent(this, OrganizerWaitlistMapActivity.class);
                intent.putExtra("event_id", eventId);
                startActivity(intent);
            });
        }

        Button commentsBtn = findViewById(R.id.commentsBtn);
        commentsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventCommentsOrganizer.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });

        Button BtnChosen    = findViewById(R.id.BtnChosen);
        Button BtnCancelled = findViewById(R.id.BtnCancelled);
        Button BtnTotal = findViewById(R.id.BtnTotal);
        BtnTotal.setOnClickListener(v-> {
            currentView = "accepted";
            entrantUsers.clear();

            for (Map<String, Object> userMap : acceptedUsers) {
                tvEntrantCount.setText("Chosen Entrants: " + entrantUsers.size());
            }
        });

        BtnTotal.setOnClickListener(v -> {
            entrantUsers.clear();
            for (Map<String, Object> userMap : acceptedUsers) {
                entrantUsers.add(new WaitlistUser(
                        (String) userMap.get("id"),
                        (String) userMap.get("name"),
                        (String) userMap.get("profileImage")));
            }
            adapter.notifyDataSetChanged();
            tvEntrantCount.setText("Chosen Entrants: " + entrantUsers.size());
        });
        BtnChosen.setOnClickListener(v-> {
            currentView = "allInvited";
            entrantUsers.clear();

            for (Map<String, Object> userMap : AllInvitedUsers) {
                String userId = (String) userMap.get("id");
                String name = (String) userMap.get("name");
                String imageUrl = (String) userMap.get("profileImage");
                entrantUsers.add(new WaitlistUser(userId, name, imageUrl));
            }
            adapter.notifyDataSetChanged();
            tvEntrantCount.setText("Chosen Entrants: " + entrantUsers.size());
        });
        BtnCancelled.setOnClickListener(v->{
            currentView = "declined";
            entrantUsers.clear();
            for (Map<String, Object> userMap : declinedUsers) {
                entrantUsers.add(new WaitlistUser(
                        (String) userMap.get("id"),
                        (String) userMap.get("name"),
                        (String) userMap.get("profileImage")));
            }
            adapter.notifyDataSetChanged();
            tvEntrantCount.setText("Chosen Entrants: " + entrantUsers.size());
        });
    }


    private void setupRecyclerView() {
        entrantUsers = new ArrayList<>();
        adapter = new WaitlistAdapter(entrantUsers, (user, position) -> {
            if ("accepted".equals(currentView)) {
                removeFromEntrants(user, position);
            } else {
                Toast.makeText(this, "You can only move users from the accepted list.", Toast.LENGTH_SHORT).show();
            }
        });
        rvEntrants.setLayoutManager(new LinearLayoutManager(this));
        rvEntrants.setAdapter(adapter);
    }

    /**
     * Loads confirmed entrants from the "entrants" array in Firestore.
     * Each entry is a full user object; we read id, name, and profileImage.
     */
    private void loadEntrants() {
        db.collection("events")
                .document(eventId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Failed to load entrants: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        List<Object> entrantsRaw = (List<Object>) documentSnapshot.get("AllInvitedUsers");
                        entrantUsers.clear();
                        if (entrantsRaw != null) {
                            for (Object item : entrantsRaw) {
                                if (item instanceof Map) {
                                    Map<String, Object> userMap = (Map<String, Object>) item;
                                    entrantUsers.add(new WaitlistUser(
                                            (String) userMap.get("id"),
                                            (String) userMap.get("name"),
                                            (String) userMap.get("profileImage")));
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        tvEntrantCount.setText("Entrants for Event: " + entrantUsers.size());
                    }
                });
    }

    /**
     * Removes a user from the "entrants" array in Firestore and updates the RecyclerView.
     *
     * @param user     the {@link WaitlistUser} to remove
     * @param position the position of the user in the adapter list
     */
    private void removeFromEntrants(WaitlistUser user, int position) {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<Map<String, Object>> entrants =
                            (List<Map<String, Object>>) documentSnapshot.get("entrants");
                    List<Map<String, Object>> declined =
                            (List<Map<String, Object>>) documentSnapshot.get("declinedUsers");

                    if (entrants == null) entrants = new ArrayList<>();
                    if (declined == null) declined = new ArrayList<>();
                    final List<Map<String, Object>> finalEntrants = entrants;
                    final List<Map<String, Object>> finalDeclined = declined;

                    Map<String, Object> movedUser = null;

                    for (int i = 0; i < entrants.size(); i++) {
                        Map<String, Object> userMap = entrants.get(i);
                        if (user.getUserId().equals(userMap.get("id"))) {
                            movedUser = userMap;
                            entrants.remove(i);
                            break;
                        }
                    }

                    if (movedUser == null) {
                        Toast.makeText(this, "User not found in accepted list", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean alreadyDeclined = false;
                    for (Map<String, Object> declinedUser : declined) {
                        if (user.getUserId().equals(declinedUser.get("id"))) {
                            alreadyDeclined = true;
                            break;
                        }
                    }

                    if (!alreadyDeclined) {
                        declined.add(movedUser);
                        //declined.add(movedUser);
                    }

                    db.collection("events")
                            .document(eventId)
                            .update(
                                    "entrants", finalEntrants,
                                    "declinedUsers", finalDeclined
                            )
                            .addOnSuccessListener(aVoid -> {
                                acceptedUsers.clear();
                                acceptedUsers.addAll(finalEntrants);

                                declinedUsers.clear();
                                declinedUsers.addAll(finalDeclined);

                                entrantUsers.remove(position);
                                adapter.notifyItemRemoved(position);

                                tvEntrantCount.setText("Chosen Entrants: " + entrantUsers.size());
                                Toast.makeText(this, "User moved to declined", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(err -> {
                                Toast.makeText(this, "Failed to update user: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(err -> {
                    Toast.makeText(this, "Failed to load event: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // US 02.09.01 — Assign an entrant as co-organizer
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called when a row is long-pressed. Shows a confirmation dialog before
     * assigning the user as a co-organizer for this event.
     *
     * @param user the long-pressed {@link WaitlistUser}
     */
    private void onEntrantLongPressed(WaitlistUser user) {
        new AlertDialog.Builder(this)
                .setTitle("Assign Co-Organizer?")
                .setMessage(user.getName() + " will be made a co-organizer for this event "
                        + "and removed from the entrant pool.")
                .setPositiveButton("Confirm", (dialog, which) -> assignCoOrganizer(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Assigns the given user as a co-organizer by:
     *  1. Adding their ID to the event's {@code coOrganizers} array.
     *  2. Setting {@code isOrganizer = true} on their user document.
     *  3. Removing them from the event's {@code waitingList}.
     *  4. Sending a CO_ORGANIZER_INVITE notification to the user.
     *
     * @param user the {@link WaitlistUser} to promote
     */
    private void assignCoOrganizer(WaitlistUser user) {
        String userId = user.getUserId();

        // Step 1 — guard: check not already a co-organizer for this event
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    List<String> coOrgs = (List<String>) eventDoc.get("coOrganizers");
                    if (coOrgs != null && coOrgs.contains(userId)) {
                        Toast.makeText(this, user.getName() + " is already a co-organizer", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Step 2 — add to coOrganizers on the event
                    db.collection("events").document(eventId)
                            .update("coOrganizers", FieldValue.arrayUnion(userId))
                            .addOnSuccessListener(unused1 -> {

                                // Step 3 — set isOrganizer = true on the user document
                                db.collection("users").document(userId)
                                        .update("isOrganizer", true)
                                        .addOnSuccessListener(unused2 -> {

                                            // Step 4 — send co-organizer invite notification to the user
                                            db.collection("events").document(eventId).get()
                                                    .addOnSuccessListener(doc -> {
                                                        String eventName = doc.getString("name");
                                                        NotificationService.sendCoOrganizerInviteNotification(
                                                                userId,
                                                                eventId,
                                                                eventName
                                                        );
                                                    });

                                            // Step 5 — remove from waitingList
                                            removeCoOrgFromWaitlist(user);
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this,
                                                        "Assigned co-organizer but could not update user record: " + e.getMessage(),
                                                        Toast.LENGTH_SHORT).show());
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to assign co-organizer: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Could not load event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Removes the newly assigned co-organizer from the event's {@code waitingList}.
     *
     * @param user the user to remove from the waiting list
     */
    private void removeCoOrgFromWaitlist(WaitlistUser user) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    List<Object> waitingList = (List<Object>) doc.get("waitingList");
                    if (waitingList == null || waitingList.isEmpty()) {
                        Toast.makeText(this, user.getName() + " is now a co-organizer", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Object entryToRemove = null;
                    for (Object item : waitingList) {
                        if (item instanceof Map && user.getUserId().equals(((Map<?, ?>) item).get("id"))) {
                            entryToRemove = item;
                            break;
                        }
                    }

                    if (entryToRemove == null) {
                        Toast.makeText(this, user.getName() + " is now a co-organizer", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("events").document(eventId)
                            .update("waitingList", FieldValue.arrayRemove(entryToRemove))
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(this,
                                            user.getName() + " is now a co-organizer and has been removed from the waitlist",
                                            Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this,
                                            "Co-organizer assigned but waitlist removal failed: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show());
                });
    }
}