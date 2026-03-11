package com.example.edgers_lottery;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


// can only be accessed by admins
public class OrganizerListActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizerlist);

        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            finish();
        });

        organizerList = findViewById(R.id.organizerList);
        adapter = new OrganizerListAdapter(this, organizers);
        organizerList.setAdapter(adapter);
        loadOrganizers();
    }

    private ListView organizerList;
    private ArrayList<User> organizers = new ArrayList<>();
    private OrganizerListAdapter adapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public void removeOrganizer(String organizerId) {
        // delete all events tied to organizerId
        db.collection("events")
                .whereEqualTo("organizerId", organizerId)
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        doc.getReference().delete();
                    }
                    // delete the user with organizerId and reloads list
                    db.collection("users")
                            .document(organizerId)
                            .delete()
                            .addOnSuccessListener(aVoid -> loadOrganizers());
                });
    }

    private void loadOrganizers() {
        // loads users with role "ORGANIZER" from the database
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
