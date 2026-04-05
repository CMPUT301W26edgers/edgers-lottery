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

/**
 * End-to-End (E2E) UI tests for verifying the Admin Event Management functionality.
 * This class uses real Firebase Authentication to establish an Admin session and
 * utilizes Espresso to navigate the UI, verify admin-specific privileges (like deleting events),
 * and assert that UI state updates correctly upon deletion.
 */
@RunWith(AndroidJUnit4.class)
public class EventListAdminTest {

    private static final String TAG = "EventListAdminTest";

    /**
     * Sets up the testing environment before each test executes.
     * <p>
     * Setup flow:
     * - Authenticates with Firebase Auth using real Admin credentials.
     * - Fetches the Admin's user document from Firestore to ensure valid role access.
     * - Initializes the global {@link CurrentUser} singleton with the fetched Admin data.
     * - Launches the {@link AdminHomeActivity} to serve as the starting point.
     * * @throws InterruptedException if the thread is interrupted while waiting for the async network calls to resolve.
     */
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

    /**
     * Tests whether an authenticated Admin can successfully navigate from the Admin Home
     * screen to the Event List screen.
     * <p>
     * Execution:
     * - Clicks the Event List menu item.
     * - Verifies that the underlying ListView responsible for rendering events is displayed.
     * * @throws InterruptedException if the thread is interrupted while waiting for UI transitions.
     */
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

    /**
     * Tests the Admin's specific privilege to view the "Delete Event" button.
     * <p>
     * Execution:
     * - Navigates to the Event List screen.
     * - Clicks the first event in the ListView to open its details.
     * - Asserts that the Delete button is visible, which confirms the UI correctly parsed
     * the Admin role from the {@link CurrentUser} singleton.
     * * @throws InterruptedException if the thread is interrupted while waiting for UI interactions or data fetching.
     */
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

    /**
     * Tests the full End-to-End flow of an Admin successfully deleting an event.
     * <p>
     * Setup:
     * - Injects a mock event directly into Firestore to guarantee a safe test target without
     * destroying permanent production data.
     * <p>
     * Execution:
     * - Navigates to the Event List screen.
     * - Uses the custom {@link #withEventName(String)} matcher to find and click the exact mock event.
     * - Clicks the Delete Event button and confirms the action on the resulting popup dialog.
     * - Uses the custom {@link #isNotInAdapter(String)} ViewAssertion to verify the item has been
     * fully removed from the ListView's underlying adapter data.
     * * @throws InterruptedException if the thread is interrupted during the complex UI deletion sequence.
     */
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

    /**
     * Cleans up the test environment after each test concludes.
     * Safely signs out the authenticated user from Firebase and clears the local {@link CurrentUser} cache
     * to prevent session leakages between consecutive test runs.
     */
    @After
    public void cleanup() {
        // Clear Firebase Auth session and Singleton memory to prevent test leakage
        FirebaseAuth.getInstance().signOut();
        CurrentUser.clear();
    }


    /**
     * Custom Matcher to find an Event object inside an AdapterView (like ListView) by its string name.
     * Espresso requires custom Matchers to target specific complex objects when using {@code onData()}.
     * * @param expectedName The exact string name of the Event to search for.
     * @return A {@link Matcher} that evaluates to true if the item is an Event with a matching name.
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
     * Custom ViewAssertion to verify that an Event is completely removed from a ListView's Adapter.
     * Traditional Espresso checks struggle to assert that dynamic data is truly gone,
     * so this iterates through the Adapter to prove the item no longer exists.
     * * @param eventName The name of the event that should no longer be present in the adapter.
     * @return A {@link ViewAssertion} that will fail the test if the specified event is still found.
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