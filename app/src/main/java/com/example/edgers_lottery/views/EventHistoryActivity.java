package com.example.edgers_lottery.views;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edgers_lottery.models.CurrentUser;
import com.example.edgers_lottery.models.Event;
import com.example.edgers_lottery.models.EventHistoryAdapter;
import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays the event history for the currently logged-in user.
 * Fetches all events from Firestore and filters them locally to show only events
 * where the user appears in the waiting list or invited users list.
 */
public class EventHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventHistoryAdapter adapter;
    private List<Event> eventList;
    private FirebaseFirestore db;
    private String currentUserId;

    /**
     * Initializes the activity, sets up the RecyclerView and adapter,
     * retrieves the current user's ID, and triggers the initial data fetch.
     *
     * @param savedInstanceState saved state from a previous instance, or null if first creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_history);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.historyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        currentUserId = CurrentUser.get().getId();

        eventList = new ArrayList<>();
        adapter = new EventHistoryAdapter(this, eventList, currentUserId);
        recyclerView.setAdapter(adapter);

        fetchUserEventHistory();

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * Refreshes event history data every time the user returns to this screen.
     */
    @Override
    protected void onResume() {
        super.onResume();
        fetchUserEventHistory();
    }

    /**
     * Fetches all events from Firestore and filters them locally to find events
     * the current user is involved in. Updates the RecyclerView adapter on completion.
     */
    private void fetchUserEventHistory() {
        db.collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                eventList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Event event = document.toObject(Event.class);
                    event.setId(document.getId());
                    if (isUserInEvent(event)) {
                        eventList.add(event);
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.e("EventHistory", "Error getting documents: ", task.getException());
            }
        });
    }

    /**
     * Checks whether the current user appears in any of the event's participant lists.
     *
     * @param event the {@link Event} to check
     * @return true if the user is in the waiting list or invited users list
     */
    private boolean isUserInEvent(Event event) {
        return checkListForUser(event.getWaitingList()) ||
                checkListForUser(event.getInvitedUsers()) ||
                checkListForUser(event.getEntrants());
    }

    /**
     * Checks whether the current user's ID exists in the given list.
     *
     * @param list the list of {@link User} objects to search
     * @return true if the current user is found in the list, false otherwise
     */
    private boolean checkListForUser(ArrayList<User> list) {
        if (list == null) return false;
        for (User u : list) {
            if (u.getId() != null && u.getId().equals(currentUserId)) return true;
        }
        return false;
    }
}