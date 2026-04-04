package com.example.edgers_lottery;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.anything;
import static org.junit.Assert.assertTrue;

import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.edgers_lottery.models.CurrentUser;
import com.example.edgers_lottery.models.Event;
import com.example.edgers_lottery.models.User;
import com.example.edgers_lottery.views.AdminHomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;

import android.view.View;
import android.widget.Adapter;
import android.widget.ListView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class EventListAdminTest {

    private static final String TAG = "EventListAdminTest";

    @Before
    public void setupAdminSession() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        boolean[] setupSuccess = {false};

        // Your exact admin credentials
        String adminEmail = "sfaiyaz@ualberta.ca";
        String adminPassword = "Oj1_i11xx4YFzgJ";

        // 1. Force Firebase to log in
        FirebaseAuth.getInstance().signInWithEmailAndPassword(adminEmail, adminPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        String uid = task.getResult().getUser().getUid();

                        // 2. Fetch the user's data from Firestore using their UID
                        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                                .addOnSuccessListener(document -> {
                                    if (document.exists()) {
                                        User adminUser = document.toObject(User.class);
                                        if (adminUser != null) {
                                            adminUser.setId(document.getId());
                                            // 3. Inject the real user into your Singleton!
                                            CurrentUser.set(adminUser);
                                            setupSuccess[0] = true;
                                            android.util.Log.d(TAG, "Successfully logged in and populated CurrentUser.");
                                        }
                                    } else {
                                        android.util.Log.e(TAG, "User document does not exist in Firestore.");
                                    }
                                    latch.countDown();
                                })
                                .addOnFailureListener(e -> {
                                    android.util.Log.e(TAG, "Firestore fetch failed: " + e.getMessage());
                                    latch.countDown();
                                });
                    } else {
                        android.util.Log.e(TAG, "Firebase Auth Login failed.");
                        latch.countDown();
                    }
                });

        // Pause the test to let Auth AND Firestore finish (up to 10 seconds)
        latch.await(10, TimeUnit.SECONDS);

        // Verify everything worked before running UI tests
        assertTrue("Firebase login or Firestore fetch failed. Check your credentials and database.", setupSuccess[0]);

        // NOW launch the UI. CurrentUser is fully populated with the ADMIN role!
        ActivityScenario.launch(AdminHomeActivity.class);
    }

    @Test
    public void testAdminCanNavigateToEventList() throws InterruptedException {
        // Pause to let the Admin Home Screen load
        Thread.sleep(2000);

        // Click the Event List menu item (targeting the <include> ID from your XML)
        onView(withId(R.id.eventListMenu)).perform(click());

        // Pause to let the Event List screen load
        Thread.sleep(2000);

        // Assert that the ListView is successfully displayed on the screen
        onView(withId(R.id.adminEventsList)).check(matches(isDisplayed()));
    }

    @Test
    public void testAdminCanSelectEventAndSeeDeleteButton() throws InterruptedException {
        // Pause to let the Admin Home Screen load
        Thread.sleep(2000);

        // Navigate to the Event List
        onView(withId(R.id.eventListMenu)).perform(click());

        // Pause to let the events fetch from Firestore and populate the ListView
        Thread.sleep(3000);

        // Click on the first event (position 0) inside your ListView
        onData(anything())
                .inAdapterView(withId(R.id.adminEventsList))
                .atPosition(0)
                .perform(click());

        // Pause a moment for EventDetailsActivity to open and parse the Intent/Firestore
        Thread.sleep(3000);

        // Assert that the delete button is visible because the current user is an Admin!
        onView(withId(R.id.delete_event_button)).check(matches(isDisplayed()));
    }

    @Test
    public void testAdminCanSuccessfullyDeleteEvent() throws InterruptedException {
        // --- STEP 1: INJECT MOCK EVENT INTO FIRESTORE ---
        // (Injecting via DB is standard practice to set up the UI test state)
        CountDownLatch insertLatch = new CountDownLatch(1);
        boolean[] insertSuccess = {false};

        String uniqueMockEventName = "Espresso Delete Test " + System.currentTimeMillis();

        // Populate fields to match your Event class requirements safely
        java.util.Map<String, Object> mockEvent = new java.util.HashMap<>();
        mockEvent.put("name", uniqueMockEventName);
        mockEvent.put("description", "This is a temporary event created by an automated test.");
        mockEvent.put("capacity", 100);
        mockEvent.put("date", "2099-01-01");
        mockEvent.put("registrationEnd", "2099-01-01");

        FirebaseFirestore.getInstance().collection("events").add(mockEvent)
                .addOnSuccessListener(documentReference -> {
                    insertSuccess[0] = true;
                    insertLatch.countDown();
                })
                .addOnFailureListener(e -> insertLatch.countDown());

        insertLatch.await(10, java.util.concurrent.TimeUnit.SECONDS);
        assertTrue("Failed to set up UI test state (Mock Event Injection)", insertSuccess[0]);

        // --- STEP 2: NAVIGATE TO THE LIST ---
        Thread.sleep(2000);
        onView(withId(R.id.eventListMenu)).perform(click());
        Thread.sleep(3000);

        // --- STEP 3: FIND AND CLICK THE MOCK EVENT SAFELY ---
        // Scroll through the adapter to find our mock event and click it
        onData(withEventName(uniqueMockEventName))
                .inAdapterView(withId(R.id.adminEventsList))
                .perform(click());

        Thread.sleep(3000);

        // Click Delete Event
        onView(withId(R.id.delete_event_button)).perform(click());
        Thread.sleep(1000);

        // Confirm Delete on the Dialog
        onView(withText("Delete")).perform(click());

        // --- STEP 4: STRICT UI VERIFICATION ---
        // Wait a few seconds for EventDetailsActivity to close,
        // the Event List to resume, and the Adapter to refresh its data.
        Thread.sleep(4000);

        // Check the UI's ListView adapter to prove the item is completely gone from the screen's data
        onView(withId(R.id.adminEventsList)).check(isNotInAdapter(uniqueMockEventName));
    }

    @After
    public void cleanup() {
        // Clear Firebase Auth session and Singleton memory to prevent test leakage
        FirebaseAuth.getInstance().signOut();
        CurrentUser.clear();
    }


    /**
     * Custom Matcher to find an Event in an Adapter by its name.
     */
    public static Matcher<Object> withEventName(final String expectedName) {
        return new TypeSafeMatcher<Object>() {

            @Override
            public boolean matchesSafely(Object target) {
                if (!(target instanceof Event)) {
                    return false;
                }
                return expectedName.equals(((Event) target).getName());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Event with name: " + expectedName);
            }
        };
    }

    /**
     * Custom ViewAssertion to verify an Event is NO LONGER inside the ListView's Adapter.
     */
    public static ViewAssertion isNotInAdapter(final String eventName) {
        return new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                if (noViewFoundException != null) {
                    throw noViewFoundException;
                }

                // Ensure the view we are checking is actually a ListView
                assertTrue("The view is not a ListView", view instanceof ListView);

                ListView listView = (ListView) view;
                Adapter adapter = listView.getAdapter();

                // Read through every item the UI currently knows about
                for (int i = 0; i < adapter.getCount(); i++) {
                    Object item = adapter.getItem(i);
                    if (item instanceof Event) {
                        if (eventName.equals(((Event) item).getName())) {
                            // If we find the name, the UI didn't delete it! Fail the test.
                            fail("UI Failure: Event '" + eventName + "' is still present in the ListView!");
                        }
                    }
                }
            }
        };
    }
}