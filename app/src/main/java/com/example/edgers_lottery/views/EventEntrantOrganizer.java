package com.example.edgers_lottery.views;

import android.os.Bundle;
import android.widget.Button;
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
 * Activity that displays the entrant management screen for an organizer.
 * Provides navigation to the event details, waitlist, and edit event screens.
 * Requires an {@code event_id} intent extra to identify the current event.
 */
public class EventEntrantOrganizer extends AppCompatActivity {

    private RecyclerView rvEntrants;
    private TextView tvEntrantCount;
    private WaitlistAdapter adapter;
    private List<WaitlistUser> entrantUsers;
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
        setContentView(R.layout.activity_entrants_organizer);
        eventId = getIntent().getStringExtra("event_id");

        db = FirebaseFirestore.getInstance();
        initViews();
        setupListeners();
        setupRecyclerView();

        if (eventId != null) {
            loadEntrants();
        }
    }

    /**
     * Binds views and attaches a click listener to the back button.
     */
    private void initViews() {
        rvEntrants = findViewById(R.id.listEntrants);
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
        Button commentsBtn = findViewById(R.id.commentsBtn);
        commentsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventCommentsOrganizer.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        entrantUsers = new ArrayList<>();
        adapter = new WaitlistAdapter(entrantUsers, this::removeFromEntrants);
        rvEntrants.setLayoutManager(new LinearLayoutManager(this));
        rvEntrants.setAdapter(adapter);
    }

    private void loadEntrants() {
        db.collection("events")
                .document(eventId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Failed to load entrants: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        List<Object> entrantsRaw = (List<Object>) documentSnapshot.get("waitingList");
                        entrantUsers.clear();
                        if (entrantsRaw != null) {
                            for (Object item : entrantsRaw) {
                                if (item instanceof java.util.Map) {
                                    java.util.Map<String, Object> userMap = (java.util.Map<String, Object>) item;
                                    String userId = (String) userMap.get("id");
                                    String name = (String) userMap.get("name");
                                    String imageUrl = (String) userMap.get("profileImage");
                                    entrantUsers.add(new WaitlistUser(userId, name, imageUrl));
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        tvEntrantCount.setText("Entrants for Event: " + entrantUsers.size());
                    }
                });
    }

    private void removeFromEntrants(WaitlistUser user, int position) {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Object> entrantsRaw = new ArrayList<>((List<Object>) documentSnapshot.get("waitingList"));
                    if (entrantsRaw != null) {
                        for (int i = 0; i < entrantsRaw.size(); i++) {
                            Object item = entrantsRaw.get(i);
                            if (item instanceof java.util.Map) {
                                if (user.getUserId().equals(((java.util.Map<?, ?>) item).get("id"))) {
                                    entrantsRaw.remove(i);
                                    break;
                                }
                            }
                        }

                        db.collection("events")
                                .document(eventId)
                                .update("waitingList", entrantsRaw)
                                .addOnSuccessListener(aVoid -> {
                                    entrantUsers.remove(position);
                                    adapter.notifyItemRemoved(position);
                                    tvEntrantCount.setText("Entrants for Event: " + entrantUsers.size());
                                    Toast.makeText(this, "User removed from entrants", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(err -> {
                                    Toast.makeText(this, "Failed to remove user: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }
}