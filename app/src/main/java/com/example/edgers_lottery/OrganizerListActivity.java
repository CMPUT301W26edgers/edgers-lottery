package com.example.edgers_lottery;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class OrganizerListActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizerlist);


    }

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
                    // delete the user with organizerId
                    db.collection("users")
                            .document(organizerId)
                            .delete();
                });
    }
}
