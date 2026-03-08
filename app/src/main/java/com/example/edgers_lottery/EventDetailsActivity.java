package com.example.edgers_lottery;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


public class EventDetailsActivity extends AppCompatActivity {
    private TextView eventNameText;
    private TextView eventDescriptionText;
    private TextView eventDateText;
    private TextView eventTimeText;
    private TextView eventLocationText;
    private TextView eventCapacityText;
    private Button joinButton;
    private int capacity;
    private int entrantCount;
    private static final String TAG = "EventDetailsActivity";
    protected User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        eventNameText = findViewById(R.id.event_name);
        eventDescriptionText = findViewById(R.id.event_description);
        eventDateText = findViewById(R.id.date_text);
        eventTimeText = findViewById(R.id.event_time);
        eventLocationText = findViewById(R.id.location_name);
        eventCapacityText = findViewById(R.id.event_capacity);
        joinButton = findViewById(R.id.join_button);
        Event sampleEvent = new Event();
        sampleEvent.setName("Basketball Tournament");
        sampleEvent.setDescription("A 3v3 campus tournament.");
        sampleEvent.setDate("March 20, 2026");
        sampleEvent.setTime("6:00 PM");
        sampleEvent.setLocation("Main Gym");
        sampleEvent.setCapacity(2);

        ArrayList<Entrant> entrants = new ArrayList<>();
        entrants.add(new Entrant());
        entrants.add(new Entrant());
        sampleEvent.setEntrants(entrants);

        showEvent(sampleEvent);
    }
    private void showEvent(Event event) {
        eventNameText.setText(event.getName());
        eventDescriptionText.setText(event.getDescription());
        eventDateText.setText(event.getDate());
        eventTimeText.setText(event.getTime());
        eventLocationText.setText(event.getLocation());
        capacity = event.getCapacity();
        entrantCount = (event.getEntrants() == null) ? 0 : event.getEntrants().size();
        eventCapacityText.setText(String.format("Capacity: %d / %d", entrantCount, capacity));
        if (entrantCount >= capacity) {
            joinButton.setText("Join Waitlist");
        }
        else {
            joinButton.setText("Register");
        }



    }


}
