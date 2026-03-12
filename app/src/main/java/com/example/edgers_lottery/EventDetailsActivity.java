package com.example.edgers_lottery;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import kotlin.text.UStringsKt;


public class EventDetailsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private TextView eventNameText;

    private TextView eventDescriptionText;
    private TextView eventDateText;
    private TextView eventTimeText;
    private TextView eventLocationText;
    private TextView eventCapacityText;
    private Button joinButton;
    private Button waitlistButton;
    private int capacity;
    private int entrantCount;
    private ArrayList<User> waitingList;
    private static final String TAG = "EventDetailsActivity";
    protected User user;
    protected User user2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        db = FirebaseFirestore.getInstance();
        eventNameText = findViewById(R.id.event_name);
        eventDescriptionText = findViewById(R.id.event_description);
        eventDateText = findViewById(R.id.date_text);
        eventTimeText = findViewById(R.id.event_time);
        eventLocationText = findViewById(R.id.location_name);
        eventCapacityText = findViewById(R.id.event_capacity);
        joinButton = findViewById(R.id.join_button);
        waitlistButton = findViewById(R.id.view_waitlist);
        user = new User();
        user.setName("Tamu");
        waitingList = new ArrayList<>();
        db.collection("events").document("Basketball").get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Event event = document.toObject(Event.class);
                        if (event != null) {
                            waitingList = event.getWaitingList() != null
                                    ? event.getWaitingList()
                                    : new ArrayList<>();
                            showEvent(event);
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show()
                );
    }
    private void showEvent(Event event) {
        eventNameText.setText(event.getName());
        eventDescriptionText.setText(event.getDescription());
        eventDateText.setText(event.getDate());
        eventTimeText.setText(event.getTime());
        eventLocationText.setText(event.getLocation());
        capacity = event.getCapacity();
        entrantCount = (event.getEntrants() == null) ? 0 : event.getEntrants().size();
        eventCapacityText.setText(String.format("Capacity: %d", capacity));
        if (waitingList.contains(user)) {
            joinButton.setText("Leave Waitlist");
            joinButton.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        } else {
            joinButton.setText("Join Waitlist");
            joinButton.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
        }
        joinButton.setOnClickListener(v -> {
            if (waitingList.contains(user)) {
                waitingList.remove(user);
                joinButton.setText("Join Waitlist");
                joinButton.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                Toast.makeText(this, "Removed from Waitlist", Toast.LENGTH_SHORT).show();
            } else {
                waitingList.add(user);
                joinButton.setText("Leave Waitlist");
                joinButton.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                Toast.makeText(this, "Added to Waitlist", Toast.LENGTH_SHORT).show();
            }
        });
        waitlistButton.setOnClickListener(v->{
            StringBuilder list = new StringBuilder();

            for (User u : waitingList) {
                list.append(u.getName()).append("\n");
            }
            new AlertDialog.Builder(this)
                    .setTitle("Waitlist")
                    .setMessage(list.toString())
                    .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                    .show();
        });




    }


}
