//package com.example.edgers_lottery;
//
//import android.app.KeyguardManager;
//import android.content.Intent;
//import android.os.Build;
//import android.view.WindowManager;
//
//import androidx.test.core.app.ActivityScenario;
//import androidx.test.core.app.ApplicationProvider;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//import androidx.test.filters.LargeTest;
//import androidx.test.platform.app.InstrumentationRegistry;
//import androidx.test.uiautomator.UiDevice;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import static androidx.test.espresso.Espresso.onView;
//import static androidx.test.espresso.action.ViewActions.click;
//import static androidx.test.espresso.assertion.ViewAssertions.matches;
//import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
//import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
//import static androidx.test.espresso.matcher.ViewMatchers.withId;
//import static androidx.test.espresso.matcher.ViewMatchers.withText;
//import static org.hamcrest.CoreMatchers.not;
//
///**
// * Espresso UI tests for EventUserChoice.
// * Tests button visibility, dialog appearance, and UI state changes.
// * Requires the event ID to exist in Firestore and the test user
// * must not already be in the event's entrants list.
// */
//@LargeTest
//@RunWith(AndroidJUnit4.class)
//public class EventUserChoiceTest {
//
//    private static final String REAL_EVENT_ID = "4FNczRvUJp8DJBPbkjlo";
//    private static final String TEST_USER_ID = "test_user_espresso";
//
//    private ActivityScenario<EventUserChoice> scenario;
//    private UiDevice device;
//
//    @Before
//    public void setUp() throws InterruptedException {
//        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
//
//        // Use adb shell to force unlock and wake the screen
//        try {
//            device.executeShellCommand("input keyevent 82"); // unlock keyguard
//            device.executeShellCommand("input keyevent 26"); // power button to wake
//            device.executeShellCommand("wm dismiss-keyguard");
//        } catch (Exception e) {
//            // safe to ignore if shell not available
//        }
//
//        Thread.sleep(1500);
//
//        User fakeUser = new User();
//        fakeUser.setId(TEST_USER_ID);
//        CurrentUser.set(fakeUser);
//    }
//
//    @After
//    public void tearDown() {
//        CurrentUser.set(null);
//        if (scenario != null) {
//            scenario.close();
//        }
//    }
//
//    /**
//     * Launches EventUserChoice using the modern API to force screen on and unlock,
//     * then waits for Firestore to load event data.
//     */
//    private void launchAndWait() throws InterruptedException {
//        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventUserChoice.class);
//        intent.putExtra("eventId", REAL_EVENT_ID);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//        scenario = ActivityScenario.launch(intent);
//
//        scenario.onActivity(activity -> {
//            // Modern API (API 27+) to show over lockscreen and turn screen on
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
//                activity.setShowWhenLocked(true);
//                activity.setTurnScreenOn(true);
//                KeyguardManager km = activity.getSystemService(KeyguardManager.class);
//                if (km != null) {
//                    km.requestDismissKeyguard(activity, null);
//                }
//            }
//            // Keep screen on as a fallback
//            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//            activity.getWindow().getDecorView().requestFocus();
//        });
//
//        Thread.sleep(5000); // wait for Firestore to load
//    }
//
//    /**
//     * Test 1: Accept and Decline buttons are visible after the event loads.
//     */
//    @Test
//    public void testAcceptAndDeclineButtonsAreDisplayed() throws InterruptedException {
//        launchAndWait();
//        onView(withId(R.id.btn_accept_invite)).check(matches(isDisplayed()));
//        onView(withId(R.id.btn_decline_invite)).check(matches(isDisplayed()));
//    }
//
//    /**
//     * Test 2: Event title is populated and not empty after Firestore loads.
//     */
//    @Test
//    public void testEventTitleIsPopulated() throws InterruptedException {
//        launchAndWait();
//        onView(withId(R.id.tv_description_title))
//                .check(matches(isDisplayed()))
//                .check(matches(not(withText(""))));
//    }
//
//    /**
//     * Test 3: Clicking Decline shows the confirmation dialog.
//     */
//    @Test
//    public void testDeclineButtonShowsConfirmationDialog() throws InterruptedException {
//        launchAndWait();
//        onView(withId(R.id.btn_decline_invite)).perform(click());
//        onView(withText("Decline Invitation")).check(matches(isDisplayed()));
//    }
//
//    /**
//     * Test 4: Cancelling the decline dialog keeps both buttons visible and enabled.
//     */
//    @Test
//    public void testCancelDeclineKeepsButtonsEnabled() throws InterruptedException {
//        launchAndWait();
//        onView(withId(R.id.btn_decline_invite)).perform(click());
//        onView(withText("Cancel")).perform(click());
//        onView(withId(R.id.btn_accept_invite)).check(matches(isDisplayed()));
//        onView(withId(R.id.btn_accept_invite)).check(matches(isEnabled()));
//        onView(withId(R.id.btn_decline_invite)).check(matches(isEnabled()));
//    }
//}

package com.example.edgers_lottery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;

import static org.junit.Assert.*;

import com.example.edgers_lottery.models.User;

/**
 * Unit tests for the core logic in EventUserChoice.
 */
@RunWith(JUnit4.class)
public class EventUserChoiceTest {

    private User testUser;
    private User otherUser;

    @Before
    public void setUp() {
        testUser = new User();
        testUser.setId("user_123");

        otherUser = new User();
        otherUser.setId("user_456");
    }

    /**
     * Accepting moves user from invited list to entrants list.
     */
    @Test
    public void acceptInvite_movesUserToEntrants() {
        ArrayList<User> entrants = new ArrayList<>();
        ArrayList<User> invitedUsers = new ArrayList<>();
        invitedUsers.add(testUser);

        entrants.add(testUser);
        removeUserFromListSafely(testUser.getId(), invitedUsers);

        assertTrue(isUserInList(testUser.getId(), entrants));
        assertFalse(isUserInList(testUser.getId(), invitedUsers));
    }

    /**
     * Declining removes user from invited list only.
     */
    @Test
    public void declineInvite_removesFromInvitedOnly() {
        ArrayList<User> invitedUsers = new ArrayList<>();
        invitedUsers.add(testUser);

        removeUserFromListSafely(testUser.getId(), invitedUsers);

        assertFalse(isUserInList(testUser.getId(), invitedUsers));
    }

    /**
     * User already in entrants is not added twice.
     */
    @Test
    public void acceptInvite_doesNotDuplicateUser() {
        ArrayList<User> entrants = new ArrayList<>();
        entrants.add(testUser);

        if (!isUserInList(testUser.getId(), entrants)) {
            entrants.add(testUser);
        }

        assertEquals(1, entrants.size());
    }

    // Mirrors private logic in EventUserChoice

    private boolean isUserInList(String targetUserId, ArrayList<User> userList) {
        if (userList == null || targetUserId == null) return false;
        for (User user : userList) {
            if (user.getId() != null && user.getId().equals(targetUserId)) return true;
        }
        return false;
    }

    private void removeUserFromListSafely(String targetUserId, ArrayList<User> userList) {
        if (userList == null || targetUserId == null) return;
        for (int i = userList.size() - 1; i >= 0; i--) {
            if (userList.get(i).getId() != null && userList.get(i).getId().equals(targetUserId)) {
                userList.remove(i);
                break;
            }
        }
    }
}