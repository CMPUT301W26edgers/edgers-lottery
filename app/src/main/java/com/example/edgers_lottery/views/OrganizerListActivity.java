package com.example.edgers_lottery.views;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edgers_lottery.models.OrganizerListAdapter;
import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.User;
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
     * Deletes the organizer with the given ID and all their associated events from Firestore,
     * then reloads the organizer list.
     *
     * @param organizerId the Firestore document ID of the organizer to remove
     */
    public void removeOrganizer(String organizerId) {
        db.collection("events")
                .whereEqualTo("organizerId", organizerId)
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        doc.getReference().delete();
                    }
                    db.collection("users")
                            .document(organizerId)
                            .delete()
                            .addOnSuccessListener(aVoid -> loadOrganizers());
                });
    }

    /**
     * Fetches all users with the ORGANIZER role from Firestore and refreshes the list.
     */
    private void loadOrganizers() {
        db.collection("users")
                .whereEqualTo("role", "ORGANIZER")
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