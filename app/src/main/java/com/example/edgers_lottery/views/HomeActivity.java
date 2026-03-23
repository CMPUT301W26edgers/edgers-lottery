package com.example.edgers_lottery.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.edgers_lottery.models.CurrentUser;
import com.example.edgers_lottery.models.Event;
import com.example.edgers_lottery.models.EventArrayAdapter;
import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

/**
 * Main home screen activity that displays a browsable list of events.
 * Supports filtering by interest and date range, and provides navigation to profile,
 * event history, event details, and organizer mode.
 * Implements {@link EditProfileFragment.EditProfileDialogListener} and
 * {@link FilterEventsFragment.EditFilterDialogListener}.
 */

public class HomeActivity extends AppCompatActivity implements EditProfileFragment.EditProfileDialogListener, FilterEventsFragment.EditFilterDialogListener {
    private static final String TAG = "HomeActivity";
    protected static User user;
    FirebaseFirestore db;
    ArrayList<Event> eventsArray = new ArrayList<>();
    ArrayList<Event> allEventsArray = new ArrayList<>();
    ListView eventsList;
    EventArrayAdapter adapter;
    String keyword = "";
    String availabilityStart = "";
    String availabilityEnd = "";

    /**
     * Displays an alert dialog showing the given user's name and email.
     *
     * @param user the {@link User} whose info is displayed
     */
    private void showUserInfoDialog(User user) {
        new AlertDialog.Builder(this)
                .setTitle("User Info")
                .setMessage("Name: " + user.getName() + "\nEmail: " + user.getEmail())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

//    /**
//     * Updates the active filter criteria and shows a confirmation toast.
//     *
//     * @param interests         keyword to match against event name or description
//     * @param registrationStart earliest event date to include, in {@code yyyy-MM-dd} format
//     * @param registrationEnd   latest event date to include, in {@code yyyy-MM-dd} format
//     */
//    public void editFilter(String interests, String registrationStart, String registrationEnd) {
//        this.keyword = interests;
//        this.availabilityStart = registrationStart;
//        this.availabilityEnd = registrationEnd;
//        Toast.makeText(this, "Filters updated: " + interests + ", " + registrationStart + ", " + registrationEnd, Toast.LENGTH_SHORT).show();
//    }

    private void filterEvents(String query) {
        eventsArray.clear();

        if (query.isEmpty()) {
            eventsArray.addAll(allEventsArray); // restore full list
        } else {
            String lowerQuery = query.toLowerCase();
            for (Event event : allEventsArray) {
                boolean matchesName = event.getName() != null
                        && event.getName().toLowerCase().contains(lowerQuery);
                boolean matchesDescription = event.getDescription() != null
                        && event.getDescription().toLowerCase().contains(lowerQuery);

                if (matchesName || matchesDescription) {
                    eventsArray.add(event);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
    /**
     * Applies the given filter criteria and reloads the events list from Firestore.
     * Only events matching the interest keyword and date range are added to the list.
     *
     * @param filterKeyword         keyword to match against event name or description
     * @param availabilityStart earliest event date to include, in {@code yyyy-MM-dd} format
     * @param availabilityEnd   latest event date to include, in {@code yyyy-MM-dd} format
     */
    @Override
    public void onFilterApplied(String filterKeyword, String availabilityStart, String availabilityEnd) {
        this.keyword = filterKeyword;
        this.availabilityStart = availabilityStart;
        this.availabilityEnd = availabilityEnd;

        android.util.Log.d(TAG, "Interest:" + filterKeyword + ", Start:" + availabilityStart + ", End:" + availabilityEnd);

        eventsArray.clear();
        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        event.setId(document.getId());
                        boolean matchesKeyword = keyword == null || keyword.isEmpty()
                                || (event.getName() != null && event.getName().toLowerCase().contains(keyword.toLowerCase()))
                                || (event.getDescription() != null && event.getDescription().toLowerCase().contains(keyword.toLowerCase()));
                        boolean matchesStart = availabilityStart == null || availabilityStart.isEmpty()
                                || (event.getDate() != null && event.getDate().compareTo(availabilityStart) >= 0);
                        boolean matchesEnd = availabilityEnd == null || availabilityEnd.isEmpty()
                                || (event.getDate() != null && event.getDate().compareTo(availabilityEnd) <= 0);

                        if (matchesKeyword && matchesStart && matchesEnd) {
                            eventsArray.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e(TAG, "Failed to fetch events: " + e.getMessage());
                });
    }

    /**
     * Initializes the activity, loads the current user, fetches events from Firestore,
     * and sets up navigation and filter button listeners.
     *
     * @param savedInstanceState saved state from a previous instance, or null if first creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        user = CurrentUser.get();

        if (user != null) {
            showUserInfoDialog(user);
        } else {
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        ImageButton profileButton = findViewById(R.id.ProfileButton);
        Button historyButton = findViewById(R.id.btnHistory);
        ImageButton qrButton = findViewById(R.id.qrButton);
        ImageButton checkoutButton = findViewById(R.id.checkoutButton);
//        Button favoritesButton = findViewById(R.id.btnFavorites);
        Button filterButton = findViewById(R.id.btnFilter);
        Button organizerButton = findViewById(R.id.btnOrganizerMode);
        Button adminButton = findViewById(R.id.btnAdminMode);
        SearchView searchView = findViewById(R.id.searchView);

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

        qrButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, QrScannerActivity.class);
            startActivity(intent);
        });

        checkoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CheckoutActivity.class);
            startActivity(intent);
        });

        // view events here!
        eventsList = findViewById(R.id.eventListView);
        adapter = new EventArrayAdapter(this, eventsArray);
        eventsList.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();

        // REMOVE if onResume() works and loads list changes instantly
        //
//        db.collection("events").get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    for (DocumentSnapshot document : queryDocumentSnapshots) {
//                        Event event = document.toObject(Event.class);
//                        adapter.add(event);
//                    }
//                    adapter.notifyDataSetChanged();
//                })
//                .addOnFailureListener(e -> {
//                    android.util.Log.e(TAG, "Failed to fetch events: " + e.getMessage());
//                });

        eventsList.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = eventsArray.get(position);
            Intent intent = new Intent(this, EventDetailsActivity.class);
            intent.putExtra("eventId", selectedEvent.getId());
            startActivity(intent);
        });

        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, EventHistoryActivity.class);
            startActivity(intent);
        });

        filterButton.setOnClickListener(v -> {
            FilterEventsFragment filterEventsFragment = new FilterEventsFragment();
            filterEventsFragment.show(getSupportFragmentManager(), "filter_events");
            android.util.Log.d(TAG, "Keyword:" + keyword + ", Start:" + availabilityStart + ", End:" + availabilityEnd);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // not needed since we filter on every keystroke
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterEvents(newText);
                return true;
            }
        });

        organizerButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Switch to Organizer")
                    .setMessage("Are you sure you want to switch to the organizer view?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        user.setRole("ORGANIZER");
                        Intent intent = new Intent(this, OrganizerHomeActivity.class);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        if ("ADMIN".equals(user.getRole())) {
            adminButton.setVisibility(View.VISIBLE);
        } else {
            adminButton.setVisibility(View.INVISIBLE);
        }

        adminButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Switch to Admin")
                    .setMessage("Are you sure you want to switch to Admin view?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Intent intent = new Intent(this, AdminHomeActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If the database is initialized, fetch the freshest data
        if (db != null && adapter != null) {
            loadEvents();
        }
    }

    private void loadEvents() {
        adapter.clear(); // Clear the old list before grabbing the new one
        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        // Make sure to attach the ID so it works when clicked!
                        if (event != null) {
                            event.setId(document.getId());
                            adapter.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e(TAG, "Failed to fetch events: " + e.getMessage());
                });
        allEventsArray.addAll(eventsArray);
    }
}