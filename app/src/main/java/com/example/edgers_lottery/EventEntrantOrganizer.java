package com.example.edgers_lottery;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class EventEntrantOrganizer extends AppCompatActivity {

    private RecyclerView rvEntrants;
    private TextView tvEntrantCount;
    private WaitlistAdapter adapter;
    private List<WaitlistUser> entrantUsers;
    private FirebaseFirestore db;
    private String eventId;

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

    private void initViews() {
        rvEntrants = findViewById(R.id.listEntrants);
        tvEntrantCount = findViewById(R.id.entrantCount);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

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