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

public class EventWaitlistTab extends AppCompatActivity {

    private RecyclerView rvWaitlist;
    private TextView tvWaitlistCount;
    private WaitlistAdapter adapter;
    private List<WaitlistUser> waitlistUsers;
    private FirebaseFirestore db;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waitlist_organizer);
        eventId = getIntent().getStringExtra("event_id");
        //db = FirebaseFirestore.getInstance();
        //eventId = getIntent().getStringExtra("event_id"); // <-- gets the event_id passed from the previous screen

        initViews();
        setupListeners();
        //setupRecyclerView();
        //loadWaitlist();
    }

    private void initViews() {
        rvWaitlist = findViewById(R.id.rvWaitlist);
        tvWaitlistCount = findViewById(R.id.tvWaitlistCount);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnRunLottery).setOnClickListener(v -> runLottery());
        findViewById(R.id.btnNotifyWaitlisters).setOnClickListener(v -> notifyWaitlisters());
    }

    private void setupListeners() {
        findViewById(R.id.editEventBtn).setOnClickListener(v -> {
            finish();
            Intent intent = new Intent(this, CreateEditEventActivity.class);
            intent.putExtra("event_id", eventId); // <-- passes event_id to the next screen
            startActivity(intent);
        });

        findViewById(R.id.detailBtn).setOnClickListener(v -> {
            finish();
            Intent intent = new Intent(this, EventDetailsOrganizer.class);
            intent.putExtra("event_id", eventId); // <-- passes event_id to the next screen
            startActivity(intent);
        });

        findViewById(R.id.entrantBtn).setOnClickListener(v -> {
            finish();
            Intent intent = new Intent(this, EventEntrantOrganizer.class);
            intent.putExtra("event_id", eventId); // <-- passes event_id to the next screen
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        waitlistUsers = new ArrayList<>();
        adapter = new WaitlistAdapter(waitlistUsers, this::removeFromWaitlist);
        rvWaitlist.setLayoutManager(new LinearLayoutManager(this));
        rvWaitlist.setAdapter(adapter);
    }

    private void loadWaitlist() {
        db.collection("events")
                .document(eventId)
                .collection("waitlist")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    waitlistUsers.clear();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        String userId = doc.getId();
                        String name = doc.getString("name");
                        String imageUrl = doc.getString("profileImage");
                        waitlistUsers.add(new WaitlistUser(userId, name, imageUrl));
                    }
                    adapter.notifyDataSetChanged();
                    tvWaitlistCount.setText("Waitlisters for Event: " + waitlistUsers.size());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void removeFromWaitlist(WaitlistUser user, int position) {
        db.collection("events")
                .document(eventId)
                .collection("waitlist")
                .document(user.getUserId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    waitlistUsers.remove(position);
                    adapter.notifyItemRemoved(position);
                    tvWaitlistCount.setText("Waitlisters for Event: " + waitlistUsers.size());
                    Toast.makeText(this, "User removed from waitlist", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to remove user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void runLottery() {
        Toast.makeText(this, "Running lottery...", Toast.LENGTH_SHORT).show();
    }

    private void notifyWaitlisters() {
        Toast.makeText(this, "Notifying waitlisters...", Toast.LENGTH_SHORT).show();
    }
}