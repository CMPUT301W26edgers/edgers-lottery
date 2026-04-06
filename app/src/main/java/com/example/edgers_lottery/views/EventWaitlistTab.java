package com.example.edgers_lottery.views;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Activity that displays the waitlist management screen for an organizer.
 * Shows the list of users on the waitlist for a specific event and provides
 * options to run the lottery, notify waitlisters, invite specific users, and
 * navigate to other event management screens.
 * Requires an {@code event_id} intent extra to identify the current event.
 */
public class EventWaitlistTab extends AppCompatActivity {

    private RecyclerView rvWaitlist;
    private TextView tvWaitlistCount;
    private WaitlistAdapter adapter;
    private List<WaitlistUser> waitlistUsers;
    private FirebaseFirestore db;
    private String eventId;

    /**
     * Initializes the activity, reads the event ID from the intent,
     * and sets up views and navigation listeners.
     *
     * @param savedInstanceState saved state from a previous instance, or null if first creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waitlist_organizer);
        eventId = getIntent().getStringExtra("event_id");

        db = FirebaseFirestore.getInstance();
        initViews();
        setupListeners();
        setupRecyclerView();

        if (eventId != null) {
            loadWaitlist();
        }
    }

    private void initViews() {
        rvWaitlist = findViewById(R.id.rvWaitlist);
        tvWaitlistCount = findViewById(R.id.tvWaitlistCount);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnNotifyWaitlisters).setOnClickListener(v -> notifyWaitlisters());
        // US 02.01.03 — invite button (add this button to activity_waitlist_organizer.xml)
        findViewById(R.id.btnInviteUser).setOnClickListener(v -> showInviteSearchDialog());
    }

    /**
     * Attaches navigation click listeners to the edit event, event details, and entrants buttons.
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
        findViewById(R.id.entrantBtn).setOnClickListener(v -> {
            finish();
            Intent intent = new Intent(this, EventEntrantOrganizer.class);
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
        findViewById(R.id.commentsBtn).setOnClickListener(v -> {
            finish();
            Intent intent = new Intent(this, EventCommentsOrganizer.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });
    }

    /**
     * Initializes the RecyclerView with a {@link WaitlistAdapter} and a linear layout manager.
     */
    private void setupRecyclerView() {
        waitlistUsers = new ArrayList<>();
        adapter = new WaitlistAdapter(waitlistUsers, this::removeFromWaitlist);
        rvWaitlist.setLayoutManager(new LinearLayoutManager(this));
        rvWaitlist.setAdapter(adapter);
    }

    /**
     * Fetches the waitlist for the current event from Firestore
     * and populates the RecyclerView adapter.
     */
    private void loadWaitlist() {
        db.collection("events")
                .document(eventId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Failed to load waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        List<Object> waitingListRaw = (List<Object>) documentSnapshot.get("waitingList");
                        waitlistUsers.clear();
                        if (waitingListRaw != null) {
                            for (Object item : waitingListRaw) {
                                if (item instanceof Map) {
                                    Map<String, Object> userMap = (Map<String, Object>) item;
                                    String userId   = (String) userMap.get("id");
                                    String name     = (String) userMap.get("name");
                                    String imageUrl = (String) userMap.get("profileImage");
                                    waitlistUsers.add(new WaitlistUser(userId, name, imageUrl));
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        tvWaitlistCount.setText("Waitlisters for Event: " + waitlistUsers.size());
                    }
                });
    }

    /**
     * Removes a user from the waitlist in Firestore and updates the RecyclerView.
     *
     * @param user     the {@link WaitlistUser} to remove
     * @param position the position of the user in the adapter list
     */
    private void removeFromWaitlist(WaitlistUser user, int position) {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Object> waitingListRaw = new ArrayList<>((List<Object>) documentSnapshot.get("waitingList"));
                    if (waitingListRaw != null) {
                        for (int i = 0; i < waitingListRaw.size(); i++) {
                            Object item = waitingListRaw.get(i);
                            if (item instanceof Map) {
                                if (user.getUserId().equals(((Map<?, ?>) item).get("id"))) {
                                    waitingListRaw.remove(i);
                                    break;
                                }
                            }
                        }
                        db.collection("events")
                                .document(eventId)
                                .update("waitingList", waitingListRaw)
                                .addOnSuccessListener(aVoid -> {
                                    waitlistUsers.remove(position);
                                    adapter.notifyItemRemoved(position);
                                    tvWaitlistCount.setText("Waitlisters for Event: " + waitlistUsers.size());
                                    Toast.makeText(this, "User removed from waitlist", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed to remove user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // US 02.01.03 — Invite specific entrants to a private event's waiting list
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Shows a search dialog where the organizer can type a name, email, or phone
     * number to find a user and invite them to the waitlist.
     */
    private void showInviteSearchDialog() {
        EditText searchInput = new EditText(this);
        searchInput.setHint("Search by name, email, or phone");

        new AlertDialog.Builder(this)
                .setTitle("Invite User to Waitlist")
                .setView(searchInput)
                .setPositiveButton("Search", (dialog, which) -> {
                    String query = searchInput.getText().toString().trim();
                    if (query.isEmpty()) {
                        Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    searchAndShowResults(query);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Searches the users collection for users matching {@code query} by name, email,
     * or phone (case-insensitive). Excludes users already on the waitlist.
     * Shows a picker dialog with the results.
     *
     * @param query the search string entered by the organizer
     */
    private void searchAndShowResults(String query) {
        String lowerQuery = query.toLowerCase();

        // First load the current waitlist so we can exclude those users from results
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    // Build a set of IDs already on the waitlist
                    Set<String> alreadyAdded = new HashSet<>();
                    List<Object> currentList = (List<Object>) eventDoc.get("waitingList");
                    if (currentList != null) {
                        for (Object item : currentList) {
                            if (item instanceof Map) {
                                Object idObj = ((Map<?, ?>) item).get("id");
                                if (idObj instanceof String) alreadyAdded.add((String) idObj);
                            }
                        }
                    }

                    // Now search all users and filter client-side
                    db.collection("users").get()
                            .addOnSuccessListener(querySnapshot -> {
                                List<Map<String, Object>> results = new ArrayList<>();

                                for (QueryDocumentSnapshot doc : querySnapshot) {
                                    String userId = doc.getId();
                                    if (alreadyAdded.contains(userId)) continue;

                                    String name  = doc.getString("name");
                                    String email = doc.getString("email");
                                    String phone = doc.getString("phone");

                                    boolean matches =
                                            (name  != null && name.toLowerCase().contains(lowerQuery))  ||
                                                    (email != null && email.toLowerCase().contains(lowerQuery)) ||
                                                    (phone != null && phone.toLowerCase().contains(lowerQuery));

                                    if (matches) {
                                        Map<String, Object> info = new HashMap<>();
                                        info.put("id",           userId);
                                        info.put("name",         name  != null ? name  : "");
                                        info.put("email",        email != null ? email : "");
                                        info.put("phone",        phone != null ? phone : "");
                                        info.put("profileImage", doc.getString("profileImage"));
                                        results.add(info);
                                    }
                                }

                                if (results.isEmpty()) {
                                    Toast.makeText(this, "No users found matching \"" + query + "\"", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                showResultsPickerDialog(results, eventDoc);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Could not load event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Displays a list of matched users and lets the organizer pick one to invite.
     *
     * @param results   the list of matched user maps
     * @param eventDoc  the current event snapshot (used for capacity check)
     */
    private void showResultsPickerDialog(List<Map<String, Object>> results,
                                         com.google.firebase.firestore.DocumentSnapshot eventDoc) {
        // Check capacity before even showing the list
        Long capacity    = eventDoc.getLong("capacity");
        List<Object> currentList = (List<Object>) eventDoc.get("waitingList");
        int currentSize  = currentList != null ? currentList.size() : 0;
        if (capacity != null && currentSize >= capacity) {
            Toast.makeText(this, "Waitlist is already at capacity", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build display labels: "Name (email)"
        String[] labels = new String[results.size()];
        for (int i = 0; i < results.size(); i++) {
            String name  = (String) results.get(i).get("name");
            String email = (String) results.get(i).get("email");
            labels[i] = name + " (" + email + ")";
        }

        new AlertDialog.Builder(this)
                .setTitle("Select a user to invite")
                .setItems(labels, (dialog, index) ->
                        inviteUserToWaitlist(results.get(index)))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Adds the selected user to the event's waitingList in Firestore.
     *
     * @param userInfo map containing "id", "name", "profileImage"
     */
    private void inviteUserToWaitlist(Map<String, Object> userInfo) {
        // Check if the event is private before inviting
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    Boolean isPublic = doc.getBoolean("ispublic");
                    if (Boolean.TRUE.equals(isPublic)) {
                        Toast.makeText(this,
                                "This event is public — users can join via the app directly",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Build entry matching existing waitingList schema
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("id",           userInfo.get("id"));
                    entry.put("name",         userInfo.get("name"));
                    entry.put("profileImage", userInfo.get("profileImage"));

                    db.collection("events").document(eventId)
                            .update("waitingList", FieldValue.arrayUnion(entry))
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(this,
                                            userInfo.get("name") + " has been invited to the waitlist",
                                            Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Invite failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Could not load event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Placeholder method for running the lottery to select entrants from the waitlist.
     */

    /**
     * Sends a WAITLIST_UPDATE notification to all users currently on the waitlist.
     */
    private void notifyWaitlisters() {
        if (waitlistUsers.isEmpty()) {
            Toast.makeText(this, "No users on the waitlist to notify", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    String eventName = doc.getString("name");
                    NotificationService.sendWaitlistUpdateNotifications(waitlistUsers, eventId, eventName);
                    Toast.makeText(this, "Waitlisters notified!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to notify: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}