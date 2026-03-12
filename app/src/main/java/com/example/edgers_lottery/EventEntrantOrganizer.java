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

public class EventEntrantOrganizer extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrants_organizer);

        initViews();
        setupListeners();
    }

    private void initViews() {

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        //findViewById(R.id.btnRunLottery).setOnClickListener(v -> runLottery());
        //findViewById(R.id.btnNotifyWaitlisters).setOnClickListener(v -> notifyWaitlisters());
    }

    private void setupListeners() {
        findViewById(R.id.editEventBtn).setOnClickListener(v -> {
            finish();
            Intent intent = new Intent(this, CreateEditEventActivity.class);
            //intent.putExtra("event_id", eventId); // <-- passes event_id to the next screen
            startActivity(intent);
        });
        findViewById(R.id.detailBtn).setOnClickListener(v -> {
            finish();
            Intent intent = new Intent(this, EventDetailsOrganizer.class);
            //intent.putExtra("event_id", eventId); // <-- passes event_id to the next screen
            startActivity(intent);
        });
        findViewById(R.id.waitListBtn).setOnClickListener(v -> {
            finish();
            Intent intent = new Intent(this, EventWaitlistTab.class);
            //intent.putExtra("event_id", eventId); // <-- passes event_id to the next screen
            startActivity(intent);
        });
    }
}