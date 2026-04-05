package com.example.edgers_lottery;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.edgers_lottery.models.CurrentUser;
import com.example.edgers_lottery.models.User;
import com.example.edgers_lottery.views.AdminHomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * End-to-End (E2E) UI tests for verifying the Admin Notification Logs functionality.
 * This class uses real Firebase Authentication to establish an Admin session,
 * then utilizes Espresso to navigate the UI, click list items, and verify screen states.
 */
@RunWith(AndroidJUnit4.class)
public class NotificationLogsAdminTest {

    /**
     * Sets up the testing environment before each test runs.
     * <p>
     * Setup flow:
     * - Authenticates with Firebase Auth using real Admin credentials.
     * - Fetches the Admin's user document from Firestore.
     * - Initializes the global {@link CurrentUser} singleton.
     * - Launches the {@link AdminHomeActivity} to act as the starting point for tests.
     * * @throws InterruptedException if thread sleep or latch await is interrupted during network calls.
     */
    @Before
    public void setupAdminSession() throws InterruptedException {
        // Authenticate as the Admin before running the tests
        CountDownLatch latch = new CountDownLatch(1);
        boolean[] setupSuccess = {false};

        String adminEmail = "sfaiyaz@ualberta.ca";
        String adminPassword = "Oj1_i11xx4YFzgJ";

        FirebaseAuth.getInstance().signInWithEmailAndPassword(adminEmail, adminPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        String uid = task.getResult().getUser().getUid();

                        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                                .addOnSuccessListener(document -> {
                                    if (document.exists()) {
                                        User adminUser = document.toObject(User.class);
                                        if (adminUser != null) {
                                            adminUser.setId(document.getId());
                                            CurrentUser.set(adminUser);
                                            setupSuccess[0] = true;
                                        }
                                    }
                                    latch.countDown();
                                })
                                .addOnFailureListener(e -> latch.countDown());
                    } else {
                        latch.countDown();
                    }
                });

        latch.await(10, TimeUnit.SECONDS);
        assertTrue("Firebase login failed. Check credentials.", setupSuccess[0]);

        // Launch the Admin Home Activity
        Thread.sleep(1000);
        ActivityScenario.launch(AdminHomeActivity.class);
    }

    /**
     * Tests whether an authenticated Admin can successfully navigate from the Home screen
     * to the Notification Logs screen.
     * <p>
     * Execution:
     * - Clicks the designated navigation button on the Admin dashboard.
     * - Verifies that the "Notification Logs" title is displayed.
     * - Verifies that the underlying ListView responsible for showing users is visible.
     * * @throws InterruptedException if the thread is interrupted while waiting for UI transitions.
     */
    @Test
    public void testAdminCanNavigateToNotificationLogs() throws InterruptedException {
        // 1. Let the Admin Home Screen load
        Thread.sleep(2000);

        // 2. Click the button to navigate to Notification Logs

        onView(withId(R.id.exportNotificationsMenu)).perform(click());

        // 3. Let the new screen load
        Thread.sleep(2000);

        // 4. ASSERT: Check if the title text from your XML is displayed
        onView(withText("Notification Logs")).check(matches(isDisplayed()));

        // 5. ASSERT: Double-check that the ListView itself is visible on the screen
        onView(withId(R.id.adminNotificationsUserList)).check(matches(isDisplayed()));
    }

    /**
     * Tests the Admin's ability to view specific notification logs for a user.
     * <p>
     * Setup:
     * - Temporarily injects a mock user into the Firestore 'users' collection.
     * - Temporarily injects a mock notification payload into the Firestore 'notifications' collection.
     * This guarantees the ListView is never empty, preventing test crashes.
     * <p>
     * Execution:
     * - Navigates to the Notification Logs screen.
     * - Uses Espresso's {@code onData()} to click the very first item (position 0) in the ListView.
     * - Asserts that an alert dialog/popup appears containing the string "Logs for:".
     * * @throws InterruptedException if the thread is interrupted while waiting for Firestore writes or UI updates.
     */
    @Test
    public void testAdminCanClickUserAndSeeLogsPopup() throws InterruptedException {
        // --- STEP 1: INJECT MOCK DATA ---
        // We inject a mock user and notification to ensure the database isn't empty,
        // which prevents the test from crashing when trying to click position 0.
        CountDownLatch insertLatch = new CountDownLatch(2);
        String mockUserId = "Espresso_Mock_User_" + System.currentTimeMillis();
        String mockUserName = "Mock Test User";

        // Create a Mock User
        Map<String, Object> mockUser = new HashMap<>();
        mockUser.put("name", mockUserName);
        mockUser.put("id", mockUserId);

        FirebaseFirestore.getInstance().collection("users").document(mockUserId)
                .set(mockUser).addOnCompleteListener(task -> insertLatch.countDown());

        // Create a Mock Notification Document based on your database structure
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("eventId", "mockEvent123");
        notificationData.put("eventName", "Mock Espresso Event");
        notificationData.put("isRead", false);
        notificationData.put("timestamp", "April 5, 2026 at 4:20:18 PM UTC-6");
        notificationData.put("type", "SELECTED");

        Map<String, Object> mockNotificationDoc = new HashMap<>();
        mockNotificationDoc.put("0", notificationData); // Wrapping in the "0" index key

        FirebaseFirestore.getInstance().collection("notifications").document(mockUserId)
                .set(mockNotificationDoc).addOnCompleteListener(task -> insertLatch.countDown());

        insertLatch.await(10, TimeUnit.SECONDS);

        // --- STEP 2: NAVIGATE TO LOGS ---
        Thread.sleep(2000);

        onView(withId(R.id.exportNotificationsMenu)).perform(click());

        // Let the screen load and fetch the users
        Thread.sleep(3000);

        // --- STEP 3: CLICK A USER ---
        // Click the first user in the ListView
        onData(anything())
                .inAdapterView(withId(R.id.adminNotificationsUserList))
                .atPosition(0)
                .perform(click());

        // --- STEP 4: VERIFY THE POPUP ---
        Thread.sleep(2000);

        // We check that the popup contains "Logs for:" anywhere in its text.
        // This is safe regardless of which user happens to be at position 0!
        onView(withText(containsString("Logs for:"))).check(matches(isDisplayed()));
    }

    /**
     * Cleans up the test environment after each test concludes.
     * Safely signs out the authenticated user from Firebase and clears the local {@link CurrentUser} cache
     * to prevent session leakages between consecutive test runs.
     */
    @After
    public void cleanup() {
        FirebaseAuth.getInstance().signOut();
        CurrentUser.clear();
    }
}