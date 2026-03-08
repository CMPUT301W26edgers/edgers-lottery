package com.example.edgers_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
