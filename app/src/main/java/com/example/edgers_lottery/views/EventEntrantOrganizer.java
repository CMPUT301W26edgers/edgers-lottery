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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Date;

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
                        if (rawAllInvitedUsers != null) {
                            AllInvitedUsers = rawAllInvitedUsers;
                        } else {
                            AllInvitedUsers = new ArrayList<>();
                        }
                        if (rawDeclinedUsers != null) {
                            declinedUsers = rawDeclinedUsers;
                        } else {
                            declinedUsers = new ArrayList<>();
                        }
                        if (rawAcceptedUsers != null) {
                            acceptedUsers = rawAcceptedUsers;
                        } else {
                            acceptedUsers = new ArrayList<>();
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                });
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

        // Add this to EventDetailsOrganizer, EventWaitlistTab, and EventEntrantOrganizer
        Button mapBtn = findViewById(R.id.mapBtn);
        if (mapBtn != null) {
            mapBtn.setOnClickListener(v -> {
                finish();
                Intent intent = new Intent(this, OrganizerWaitlistMapActivity.class);
                intent.putExtra("event_id", eventId); // Make sure the variable name matches their intent key
                startActivity(intent);
            });
        }
        Button commentsBtn = findViewById(R.id.commentsBtn);
        commentsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventCommentsOrganizer.class);
            intent.putExtra("event_id", eventId);
            startActivity(intent);
        });
        Button BtnChosen = findViewById(R.id.BtnChosen);
        Button BtnCancelled = findViewById(R.id.BtnCancelled);
        Button BtnTotal = findViewById(R.id.BtnTotal);
        BtnTotal.setOnClickListener(v-> {
            currentView = "accepted";
            entrantUsers.clear();

            for (Map<String, Object> userMap : acceptedUsers) {
                String userId = (String) userMap.get("id");
                String name = (String) userMap.get("name");
                String imageUrl = (String) userMap.get("profileImage");
                entrantUsers.add(new WaitlistUser(userId, name, imageUrl));
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
                String userId = (String) userMap.get("id");
                String name = (String) userMap.get("name");
                String imageUrl = (String) userMap.get("profileImage");
                entrantUsers.add(new WaitlistUser(userId, name, imageUrl));
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
}