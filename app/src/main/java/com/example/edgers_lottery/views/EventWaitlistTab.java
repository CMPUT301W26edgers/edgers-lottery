package com.example.edgers_lottery.views;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.WaitlistUser;
import com.example.edgers_lottery.models.WaitlistAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays the waitlist management screen for an organizer.
 * Shows the list of users on the waitlist for a specific event and provides
 * options to run the lottery, notify waitlisters, and navigate to other event management screens.
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
        findViewById(R.id.btnRunLottery).setOnClickListener(v -> runLottery());
        findViewById(R.id.btnNotifyWaitlisters).setOnClickListener(v -> notifyWaitlisters());
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
     * Fetches the waitlist subcollection for the current event from Firestore
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
                                if (item instanceof java.util.Map) {
                                    java.util.Map<String, Object> userMap = (java.util.Map<String, Object>) item;
                                    String userId = (String) userMap.get("id");
                                    String name = (String) userMap.get("name");
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
     * Removes a user from the waitlist subcollection in Firestore and updates the RecyclerView.
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
                            if (item instanceof java.util.Map) {
                                if (user.getUserId().equals(((java.util.Map<?, ?>) item).get("id"))) {
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
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to remove user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

    /**
     * Placeholder method for running the lottery to select entrants from the waitlist.
     */
    private void runLottery() {
        Toast.makeText(this, "Running lottery...", Toast.LENGTH_SHORT).show();
    }

    /**
     * Placeholder method for sending notifications to all users on the waitlist.
     */
    private void notifyWaitlisters() {
        Toast.makeText(this, "Notifying waitlisters...", Toast.LENGTH_SHORT).show();
    }
}