package com.example.edgers_lottery.views.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.core.Event;
import com.example.edgers_lottery.models.adapters.EventArrayAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class OrganizerEventsListActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private ArrayList<Event> eventsArray = new ArrayList<>();
    private ArrayList<Event> allEventsArray = new ArrayList<>();
    private ListView eventsList;
    private EventArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_my_events);

        ImageButton backButton = findViewById(R.id.btnBack);
        SearchView searchView = findViewById(R.id.searchView);
        eventsList = findViewById(R.id.eventListView);

        adapter = new EventArrayAdapter(this, eventsArray);
        eventsList.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();
        backButton.setOnClickListener(v -> finish());
        loadMyEvents();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterEvents(newText);
                return true;
            }
        });

        eventsList.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = eventsArray.get(position);
            Intent intent = new Intent(this, EventDetailsOrganizer.class);
            intent.putExtra("event_id", selectedEvent.getId());
            startActivity(intent);
        });
    }

    private void loadMyEvents() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();

        eventsArray.clear();
        allEventsArray.clear();
        adapter.clear();

        // ── Query 1: events the user created (organizerId == uid) ──────────
        db.collection("events")
                .whereEqualTo("organizerId", uid)
                .get()
                .addOnSuccessListener(ownedSnapshots -> {
                    for (DocumentSnapshot document : ownedSnapshots) {
                        Event event = document.toObject(Event.class);
                        if (event != null) {
                            event.setId(document.getId());
                            // Avoid duplicates in case both queries return the same doc
                            if (!containsEvent(allEventsArray, event.getId())) {
                                eventsArray.add(event);
                                allEventsArray.add(event);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();

                    // ── Query 2: events where user is a co-organizer ────────
                    db.collection("events")
                            .whereArrayContains("coOrganizers", uid)
                            .get()
                            .addOnSuccessListener(coOrgSnapshots -> {
                                for (DocumentSnapshot document : coOrgSnapshots) {
                                    Event event = document.toObject(Event.class);
                                    if (event != null) {
                                        event.setId(document.getId());
                                        // Only add if not already in the list from query 1
                                        if (!containsEvent(allEventsArray, event.getId())) {
                                            eventsArray.add(event);
                                            allEventsArray.add(event);
                                        }
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                // Co-org query failed — owned events still show, non-critical
                                android.widget.Toast.makeText(this,
                                        "Could not load co-organized events: " + e.getMessage(),
                                        android.widget.Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    android.widget.Toast.makeText(this,
                            "Failed to load events: " + e.getMessage(),
                            android.widget.Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Returns true if the list already contains an event with the given id.
     * Used to prevent duplicates when merging the two Firestore queries.
     */
    private boolean containsEvent(ArrayList<Event> list, String eventId) {
        for (Event e : list) {
            if (eventId.equals(e.getId())) return true;
        }
        return false;
    }

    private void filterEvents(String query) {
        eventsArray.clear();

        if (query == null || query.isEmpty()) {
            eventsArray.addAll(allEventsArray);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Event event : allEventsArray) {
                boolean matchesName = event.getName() != null &&
                        event.getName().toLowerCase().contains(lowerQuery);
                boolean matchesDescription = event.getDescription() != null &&
                        event.getDescription().toLowerCase().contains(lowerQuery);
                if (matchesName || matchesDescription) {
                    eventsArray.add(event);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }
}