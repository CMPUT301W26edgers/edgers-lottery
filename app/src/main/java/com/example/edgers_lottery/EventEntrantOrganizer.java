package com.example.edgers_lottery;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays the entrant management screen for an organizer.
 * Provides navigation to the event details, waitlist, and edit event screens.
 * Requires an {@code event_id} intent extra to identify the current event.
 */
public class EventEntrantOrganizer extends AppCompatActivity {

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
        initViews();
        setupListeners();
    }

    /**
     * Binds views and attaches a click listener to the back button.
     */
    private void initViews() {
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
    }
}