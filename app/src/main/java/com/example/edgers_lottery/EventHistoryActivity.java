package com.example.edgers_lottery; // Update if your package name is different!

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class EventHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventHistoryAdapter adapter;
    private List<Event> eventList;
    private FirebaseFirestore db;

    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_history);


        // 1. Initialize Firebase and UI
        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.historyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 2. Get the current user's ID (Assuming you use Device ID for testing)
        currentUserId = CurrentUser.get().getId();


        // 3. Setup the Adapter
        eventList = new ArrayList<>();
        adapter = new EventHistoryAdapter(this, eventList, currentUserId);
        recyclerView.setAdapter(adapter);

        // 4. Load the data!
        fetchUserEventHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 4. Load the data! (This now runs every time you return to this screen)
        fetchUserEventHistory();
    }

    private void fetchUserEventHistory() {
        // Fetch ALL events for now to filter locally (since Firestore doesn't allow OR queries across multiple array fields easily)
        db.collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                eventList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Event event = document.toObject(Event.class);
                    event.setId(document.getId());
                    // Filter: Only add to history if the user is in ANY of the lists
                    if (isUserInEvent(event)) {
                        eventList.add(event);
                    }
                }
                adapter.notifyDataSetChanged(); // Tell the adapter the data is ready!
            } else {
                Log.e("EventHistory", "Error getting documents: ", task.getException());
            }
        });
    }

    private boolean isUserInEvent(Event event) {
        // Re-use the same logic to check if they are in the waitlist or invited list
        return checkListForUser(event.getWaitingList()) || checkListForUser(event.getInvitedUsers());
    }

    private boolean checkListForUser(ArrayList<User> list) {
        if (list == null) return false;
        for (User u : list) {
            if (u.getId() != null && u.getId().equals(currentUserId)) return true;
        }
        return false;
    }
}