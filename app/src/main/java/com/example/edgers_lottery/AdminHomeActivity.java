package com.example.edgers_lottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminHomeActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminhome);

        TextView organizer = findViewById(R.id.organizerListMenu).findViewById(R.id.menuTitle);
        TextView events = findViewById(R.id.eventListMenu).findViewById(R.id.menuTitle);
        TextView images = findViewById(R.id.imagesViewMenu).findViewById(R.id.menuTitle);
        TextView users = findViewById(R.id.userListMenu).findViewById(R.id.menuTitle);
        TextView export = findViewById(R.id.exportNotificationsMenu).findViewById(R.id.menuTitle);

        organizer.setText("Organizer List");
        events.setText("Events List");
        images.setText("Images View");
        users.setText("User List");
        export.setText("Export Notifications");

        organizer.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrganizerListActivity.class);
            startActivity(intent);
        });

        events.setOnClickListener(v -> {
            Intent intent = new Intent(this, EventsListActivity.class);
            startActivity(intent);
        });

        images.setOnClickListener(v -> {
            Intent intent = new Intent(this, ImagesViewActivity.class);
            startActivity(intent);
        });

        users.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserListActivity.class);
            startActivity(intent);
        });

        export.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExportNotificationsActivity.class);
            startActivity(intent);
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
