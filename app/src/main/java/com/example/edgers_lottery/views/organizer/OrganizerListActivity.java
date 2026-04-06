package com.example.edgers_lottery.views.organizer;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edgers_lottery.models.adapters.OrganizerListAdapter;
import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.core.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Admin-only activity that displays a list of all organizer accounts.
 * Supports removing an organizer and all their associated events from Firestore.
 */
public class OrganizerListActivity extends AppCompatActivity {

    private ListView organizerList;
    private ArrayList<User> organizers = new ArrayList<>();
    private OrganizerListAdapter adapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Initializes the activity, sets up the back button, and loads the organizer list.
     *
     * @param savedInstanceState saved state from a previous instance, or null if first creation
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizerlist);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        organizerList = findViewById(R.id.organizerList);
        adapter = new OrganizerListAdapter(this, organizers);
        organizerList.setAdapter(adapter);
        loadOrganizers();
    }

    /**
     * Deletes the organizer with the given ID from Firestore,
     * removes all their associated events, then reloads the organizer list.
     *
     * @param organizerId the Firestore document ID of the organizer to remove
     */
    public void removeOrganizer(String organizerId) {
        db.collection("events")
                .whereEqualTo("organizerId", organizerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        db.collection("events").document(document.getId()).delete();
                    }
                    db.collection("users")
                            .document(organizerId)
                            .delete()
                            .addOnSuccessListener(aVoid -> loadOrganizers());
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("OrganizerListActivity", "Failed to delete events for organizer: " + organizerId, e);
                });
    }

    /**
     * Fetches all users with the ORGANIZER role from Firestore and refreshes the list.
     */
    private void loadOrganizers() {
        db.collection("users")
                .whereEqualTo("organizer", true)
                .get()
                .addOnSuccessListener(query -> {
                    organizers.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setId(doc.getId());
                            organizers.add(user);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}