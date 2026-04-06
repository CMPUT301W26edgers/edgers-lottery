package com.example.edgers_lottery.views;

import static android.view.View.GONE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.edgers_lottery.models.CurrentUser;
import com.example.edgers_lottery.models.Event;
import com.example.edgers_lottery.models.EventArrayAdapter;
import com.example.edgers_lottery.R;
import com.example.edgers_lottery.models.EventCarouselAdapter;
import com.example.edgers_lottery.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeableMethod;

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
    ArrayList<Event> filteredEventsArray = new ArrayList<>();
    ListView eventsList;
    EventArrayAdapter adapter;
    EventCarouselAdapter carouselAdapter;
    CardStackLayoutManager cardStackLayoutManager;
    CardStackView cardStackView;
//    String keyword = "";
    boolean capacity_bool = false;
    boolean carousel_bool = false;
    String availabilityStart = "";
    String availabilityEnd = "";
    ImageButton profileButton ;
    Button historyButton;
    ImageButton qrButton;
    ImageButton checkoutButton;
    ImageButton notificationsButton;
    Button carouselButton;
    Button filterButton ;
    Button organizerButton;
    Button adminButton;
    SearchView searchView;

    LinearLayout linearLayout13; // holds the search bar

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


    /**
     * Filters the displayed events list based on a search query.
     * Matches against event name and description (case-insensitive).
     * If the query is empty, the full unfiltered list is restored.
     *
     * @param query the search string to filter events by
     */
    private void filterEvents(String query) {
//        eventsArray.clear();
        if (filteredEventsArray.isEmpty())
            filteredEventsArray.addAll(eventsArray);
        if (query.isEmpty()) {
            adapter.addAll(eventsArray); // restore full list
            adapter.notifyDataSetChanged();
            return;
        } else {
            String lowerQuery = query.toLowerCase();
            ArrayList<Event> searchArray = new ArrayList<>();
            for (Event event : filteredEventsArray) { // loop through all events (in filter)
                boolean matchesName = event.getName() != null
                        && event.getName().toLowerCase().contains(lowerQuery);
                boolean matchesDescription = event.getDescription() != null
                        && event.getDescription().toLowerCase().contains(lowerQuery);
                boolean isInFilter = filteredEventsArray.contains(event);
                if ((matchesName || matchesDescription) && isInFilter) {
                    searchArray.add(event);
                }
            }
            adapter.clear();
            adapter.addAll(searchArray);
            adapter.notifyDataSetChanged();
        }
    }
    /**
     * Applies the given filter criteria and reloads the events list from Firestore.
     * Only events matching the interest keyword and date range are added to the list.
     *
     * @param isCapacity         keyword to match against event name or description
     * @param availabilityStart earliest event date to include, in {@code yyyy-MM-dd} format
     * @param availabilityEnd   latest event date to include, in {@code yyyy-MM-dd} format
     */
    @Override
    public void onFilterApplied(boolean isCapacity, String availabilityStart, String availabilityEnd) {
        this.capacity_bool = isCapacity;
        this.availabilityStart = availabilityStart;
        this.availabilityEnd = availabilityEnd;

        android.util.Log.d(TAG, "At Capacity:" + isCapacity + ", Start:" + availabilityStart + ", End:" + availabilityEnd);
        filteredEventsArray.clear();
        eventsArray.clear();
        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        event.setId(document.getId());

                        boolean matchesStart = availabilityStart == null || availabilityStart.isEmpty()
                                || (event.getDate() != null && event.getDate().compareTo(availabilityStart) >= 0);

                        boolean matchesEnd = availabilityEnd == null || availabilityEnd.isEmpty()
                                || (event.getDate() != null && event.getDate().compareTo(availabilityEnd) <= 0);

                        boolean matchesCapacity = !isCapacity
                                || (event.getEntrants() == null || event.getEntrants().size() < event.getCapacity());
                        if (matchesStart && matchesEnd && matchesCapacity) {
                            eventsArray.add(event);
                            filteredEventsArray.add(event);
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
//            showUserInfoDialog(user);
            Toast.makeText(this, "Welcome back, " + user.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
            finish();
            return;
        }


        findViews();
        // set these visibilities by default
        cardStackView.setVisibility(GONE);
        filterButton.setVisibility(View.VISIBLE);
        eventsList.setVisibility(View.VISIBLE);
        linearLayout13.setVisibility(View.VISIBLE);
        if (user.isOrganizer()) {
            organizerButton.setText("\uD83D\uDCC4 Organizer Mode");
        } else {
            organizerButton.setText("\uD83D\uDCC4 Create Event");
        }
        if ("ADMIN".equals(user.getRole())) {
            adminButton.setVisibility(View.VISIBLE);
        } else {
            adminButton.setVisibility(View.INVISIBLE);
        }
        setListeners();
        setupCardStack(); // setup carousel view
        // view events here!
        adapter = new EventArrayAdapter(this, eventsArray);
        eventsList.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();
        loadEvents();
        // carousel view
        carouselAdapter = new EventCarouselAdapter(this, eventsArray);
        cardStackView.setAdapter(carouselAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // not needed since we filter on every keystroke
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    // reset to filtered array (or all events if no filter applied)
                    adapter.clear();
                    adapter.addAll(filteredEventsArray.isEmpty() ? eventsArray : filteredEventsArray);
                    adapter.notifyDataSetChanged();

                } else {
                    filterEvents(newText);
                }
                return true;
            }
        });
    }
// this causes a bug where it reloads all events after a user clicks back to the home screen after searching
    // we want the search not to end because it makes the app more usable
    /**
     * Refreshes the current user reference when the activity resumes.
     */
    @Override
    protected void onResume() {
        super.onResume();
        user = CurrentUser.get(); // update current user
    }
    /**
     * Loads all events from Firestore and populates the adapter.
     * Clears the existing list before fetching. Logs an error on failure.
     */
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
                            allEventsArray.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    carouselAdapter.setEvents(allEventsArray);
                    carouselAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e(TAG, "Failed to fetch events: " + e.getMessage());
                });
    }
    private void findViews(){
        profileButton = findViewById(R.id.ProfileButton);
        historyButton = findViewById(R.id.btnHistory);
        qrButton = findViewById(R.id.qrButton);
        checkoutButton = findViewById(R.id.checkoutButton);
        notificationsButton = findViewById(R.id.ProfileNotification);
        filterButton = findViewById(R.id.btnFilter);
        organizerButton = findViewById(R.id.btnOrganizerMode);
        adminButton = findViewById(R.id.btnAdminMode);
        searchView = findViewById(R.id.searchView);
        carouselButton = findViewById(R.id.btnCarousel);
        cardStackView = findViewById(R.id.card_stack_view);
        eventsList = findViewById(R.id.eventListView);
        linearLayout13 = findViewById(R.id.linearLayout13);
    }
    private void setListeners(){
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
        notificationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExportNotificationsActivity.class);
            startActivity(intent);
        });
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
            android.util.Log.d(TAG, "At Capacity:" + capacity_bool + ", Start:" + availabilityStart + ", End:" + availabilityEnd);
        });
        organizerButton.setOnClickListener(v -> {
            if (user.isOrganizer()){
                new AlertDialog.Builder(this)
                        .setTitle("Switch to Organizer")
                        .setMessage("Are you sure you want to switch to the organizer view?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            user.setRole("ORGANIZER");
                            Intent intent = new Intent(this, OrganizerHomeActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else{ // create first event, if they dont create one then they shouldnt be an organizer
                Intent intent = new Intent(this, CreateEditEventActivity.class);
                startActivity(intent);
                finish();
            }
        });
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
        carouselButton.setOnClickListener(v -> {
            if (!carousel_bool) { // entering carousel mode
                carouselButton.setText("♡ List Mode");
                cardStackView.setVisibility(View.VISIBLE);
                eventsList.setVisibility(GONE);
                filterButton.setVisibility(GONE);
                linearLayout13.setVisibility(GONE);

                carouselAdapter.setEvents(allEventsArray);
                carouselAdapter.notifyDataSetChanged();
                cardStackLayoutManager.scrollToPosition(0);

                new AlertDialog.Builder(this)
                        .setTitle("Carousel Instructions")
                        .setMessage("Swipe card to the left to skip, right to view details")
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).show();
            } else{ // entering list mode
                carouselButton.setText("♡ Carousel Mode");
                cardStackView.setVisibility(GONE);
                filterButton.setVisibility(View.VISIBLE);
                eventsList.setVisibility(View.VISIBLE);
                linearLayout13.setVisibility(View.VISIBLE);
            }
            carousel_bool = !carousel_bool;
        });
    }
    /**
     * Sets up the CardStackLayoutManager with swipe settings and listener.
     * https://www.geeksforgeeks.org/android/tinder-swipe-view-with-example-in-android/
     */
    private void setupCardStack() {
        cardStackLayoutManager = new CardStackLayoutManager(this, new CardStackListener() {
            @Override
            public void onCardDragging(Direction direction, float ratio) {

            }
            @Override
            public void onCardSwiped(Direction direction) {
                int position = cardStackLayoutManager.getTopPosition();

                // Handle the swipe action
                if (direction == Direction.Right && position > 0) {
                    Event event = allEventsArray.get(position - 1);
                    Intent intent = new Intent(HomeActivity.this, EventDetailsActivity.class);
                    intent.putExtra("eventId", event.getId());
                    startActivity(intent);
                } else if (direction == Direction.Left) {
                    Toast.makeText(HomeActivity.this, "Skipped", Toast.LENGTH_SHORT).show();
                }

                // Check end of deck AFTER handling the swipe, not before
                if (position >= allEventsArray.size()) {
                    new AlertDialog.Builder(HomeActivity.this)
                            .setTitle("You've seen all events!")
                            .setMessage("Nothing left to see here!")
                            .setPositiveButton("Start Over", (dialog, which) -> {
                                carouselAdapter.setEvents(allEventsArray);
                                carouselAdapter.notifyDataSetChanged();
                                cardStackLayoutManager.scrollToPosition(0);
                            })
                            .setNegativeButton("Switch to List", (dialog, which) -> {
                                carousel_bool = true;
                                carouselButton.performClick();
                            })
                            .show();
                }
            }
            // dont do anything with these at all
            @Override
            public void onCardRewound() {}

            @Override
            public void onCardCanceled() {}

            @Override
            public void onCardAppeared(View view, int position) {}

            @Override
            public void onCardDisappeared(View view, int position) {}
        });

        // Stack configuration
        cardStackLayoutManager.setStackFrom(StackFrom.None);
        cardStackLayoutManager.setVisibleCount(3);       // how many cards peek behind
        cardStackLayoutManager.setTranslationInterval(8.0f);
        cardStackLayoutManager.setScaleInterval(0.95f);
        cardStackLayoutManager.setSwipeThreshold(0.3f);
        cardStackLayoutManager.setMaxDegree(20.0f);
        cardStackLayoutManager.setDirections(Direction.HORIZONTAL); // left and right only
        cardStackLayoutManager.setCanScrollHorizontal(true);
        cardStackLayoutManager.setCanScrollVertical(false);
        cardStackLayoutManager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual);
        cardStackView.setLayoutManager(cardStackLayoutManager);
    }
}