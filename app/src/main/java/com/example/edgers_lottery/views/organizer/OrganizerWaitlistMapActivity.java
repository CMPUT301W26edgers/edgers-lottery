package com.example.edgers_lottery.views.organizer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.edgers_lottery.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

/**
 * Activity for Event Organizers to view the geographical distribution of their waitlisted entrants.
 * This activity integrates with Google Maps and Firebase Firestore to display real-time
 * location markers for users who have joined the event's waitlist with location permissions enabled.
 */
public class OrganizerWaitlistMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    /** The Google Map instance used to display user locations. */
    private GoogleMap mMap;

    /** The unique Firestore document ID of the event being viewed. */
    private String eventId;

    /** Instance of FirebaseFirestore used for real-time database operations. */
    private FirebaseFirestore db;

    /**
     * Initializes the activity, retrieves the event ID from the incoming intent,
     * initializes the Firestore instance, sets up the MapFragment, and configures navigation.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     * being shut down, this Bundle contains the data it most
     * recently supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waitlist_map_organizer);

        // 1. Get Event ID from Intent
        eventId = getIntent().getStringExtra("event_id");
        db = FirebaseFirestore.getInstance();

        // 2. Initialize Map Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            // Asynchronously sets up the map. onMapReady will be called when it's finished loading.
            mapFragment.getMapAsync(this);
        }

        // 3. Setup Navigation (Matching teammate's style)
        setupNavigation();
    }

    /**
     * Callback triggered when the Google Map is ready to be used.
     * Enables basic map UI controls and triggers the fetching of entrant locations.
     *
     * @param googleMap A non-null instance of a GoogleMap associated with the MapFragment.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Optional: Style the map to be clean
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (eventId != null) {
            loadEntrantLocations();
        }
    }

    /**
     * Attaches a real-time snapshot listener to the specific event document in Firestore.
     * Parses the "waitingList" array to extract user coordinates and places markers on the map.
     * The map's camera automatically adjusts its bounds to ensure all markers are visible.
     * This method runs continuously; if a user joins or leaves, the map updates automatically.
     */
    @SuppressWarnings("unchecked")
    private void loadEntrantLocations() {
        db.collection("events").document(eventId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null || documentSnapshot == null || !documentSnapshot.exists()) return;

                    // Clear existing markers before redrawing to prevent duplicates on real-time updates
                    mMap.clear();

                    // Get the waitingList array from Firestore
                    List<Map<String, Object>> waitingList = (List<Map<String, Object>>) documentSnapshot.get("waitingList");

                    if (waitingList == null || waitingList.isEmpty()) {
                        Toast.makeText(this, "No entrants on the waitlist yet.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    boolean hasMarkers = false;

                    // Iterate through the waitlist and drop pins for users with valid coordinates
                    for (Map<String, Object> userMap : waitingList) {
                        Double lat = (Double) userMap.get("latitude");
                        Double lon = (Double) userMap.get("longitude");
                        String name = (String) userMap.get("name");

                        if (lat != null && lon != null) {
                            LatLng position = new LatLng(lat, lon);
                            mMap.addMarker(new MarkerOptions()
                                            .position(position)
                                            .title(name))
                                    .showInfoWindow();

                            builder.include(position);
                            hasMarkers = true;
                        }
                    }

                    // Auto-zoom the camera to fit all participants on the screen
                    if (hasMarkers) {
                        LatLngBounds bounds = builder.build();
                        int padding = 200; // Offset from edges of the map in pixels
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                    }
                });
    }

    /**
     * Binds click listeners to the navigation bar/buttons.
     * Allows the organizer to easily transition between the map, event details,
     * the standard waitlist view, the selected entrants view, and the event editor.
     */
    private void setupNavigation() {
        // Back Button
        findViewById(R.id.btnBackEventDetails).setOnClickListener(v -> finish());

        // Tab Navigation
        findViewById(R.id.detailBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, EventDetailsOrganizer.class).putExtra("event_id", eventId));
            finish();
        });

        findViewById(R.id.waitListBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, EventWaitlistTab.class).putExtra("event_id", eventId));
            finish();
        });

        findViewById(R.id.entrantBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, EventEntrantOrganizer.class).putExtra("event_id", eventId));
            finish();
        });

        findViewById(R.id.editEventBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, CreateEditEventActivity.class).putExtra("event_id", eventId));
            finish();
        });
    }
}