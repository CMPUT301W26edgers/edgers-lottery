package com.example.edgers_lottery.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.Event;
import com.example.edgers_lottery.models.EventArrayAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AdminEventListActivity extends AppCompatActivity {

    private ListView eventsList;
    private EventArrayAdapter adapter;
    private ArrayList<Event> eventsArray;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_list);

        // 1. Setup Back Button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // Returns to Admin Home

        // 2. Setup List and Adapter
        eventsList = findViewById(R.id.adminEventsList);
        eventsArray = new ArrayList<>();
        adapter = new EventArrayAdapter(this, eventsArray);
        eventsList.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // 3. Handle Clicks on an Event
        eventsList.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = eventsArray.get(position);
            Intent intent = new Intent(AdminEventListActivity.this, EventDetailsActivity.class);
            intent.putExtra("eventId", selectedEvent.getId());
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllEvents(); // Refresh the list every time the screen appears
    }

    private void loadAllEvents() {
        adapter.clear();
        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        if (event != null) {
                            event.setId(document.getId());
                            adapter.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("AdminEventList", "Failed to fetch events: " + e.getMessage());
                });
    }
}