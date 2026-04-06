package com.example.edgers_lottery.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.CurrentUser;
import com.example.edgers_lottery.models.Event;
import com.example.edgers_lottery.models.EventArrayAdapter;
import com.example.edgers_lottery.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Activity for Administrators to view a comprehensive list of all events in the system.
 * This class connects to Firebase Firestore to retrieve the events, displays them
 * using a {@link ListView} paired with an {@link EventArrayAdapter}, and handles
 * navigation to detailed event views.
 */
public class AdminEventListActivity extends AppCompatActivity {

    /** The ListView used to display the scrollable list of events. */
    private ListView eventsList;

    /** The custom adapter responsible for binding event data to the ListView rows. */
    private EventArrayAdapter adapter;

    /** The local list of Event objects currently displayed on the screen. */
    private ArrayList<Event> eventsArray;

    /** Instance of FirebaseFirestore used for database operations. */
    private FirebaseFirestore db;

    protected User user;

    /**
     * Initializes the activity, sets up the user interface components, and configures
     * click listeners for navigation.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     * being shut down, this Bundle contains the data it most
     * recently supplied. Otherwise, it is null.
     */
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

        user = CurrentUser.get();

        // 3. Handle Clicks on an Event
        // Navigates to EventDetailsActivity, passing the specific event's ID so the
        // details screen knows which event to query and display.
        eventsList.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = eventsArray.get(position);
            Intent intent = new Intent(AdminEventListActivity.this, EventDetailsActivity.class);
            intent.putExtra("eventId", selectedEvent.getId());
            startActivity(intent);
        });
    }

    /**
     * Called when the activity will start interacting with the user.
     * We trigger {@link #loadAllEvents()} here rather than just in onCreate() to ensure
     * the list automatically refreshes if the admin returns to this screen after
     * deleting or modifying an event in the EventDetailsActivity.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadAllEvents(); // Refresh the list every time the screen appears
    }

    /**
     * Fetches all documents from the "events" collection in Firebase Firestore.
     * Clears the current adapter data, maps each database document to an {@link Event}
     * object, sets the document ID, and notifies the adapter to refresh the UI.
     */
    private void loadAllEvents() {
        adapter.clear();
        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        if (event != null) {
                            // Ensure the local object has the correct Firestore document ID attached
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