package com.example.edgers_lottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.example.edgers_lottery.models.CurrentUser;
import com.example.edgers_lottery.models.Event;
import com.example.edgers_lottery.models.User;
import com.example.edgers_lottery.views.EventDetailsActivity;
import com.example.edgers_lottery.views.EventDetailsOrganizer;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class WaitlistMapOrganizerTest {

    @Test
    public void testUserPromptedForLocationWhenJoiningWaitlist() throws InterruptedException, UiObjectNotFoundException {
        // 1. Setup the mock logged-in user
        User mockCurrentUser = new User();
        mockCurrentUser.setId("MockUserId123");
        mockCurrentUser.setName("Test Entrant");

        // Inject the dummy user into your app's global state
        CurrentUser.set(mockCurrentUser);

        // 2. We must mock an event in Firestore so EventDetailsActivity has something to load
        CountDownLatch latch = new CountDownLatch(1);
        String mockEventId = "GeoUserTestEvent";

        Event mockEvent = new Event();
        mockEvent.setId(mockEventId);
        mockEvent.setName("User Geo Test Event");
        mockEvent.setEnforceLocation(true);

        FirebaseFirestore.getInstance().collection("events").document(mockEventId)
                .set(mockEvent).addOnCompleteListener(task -> latch.countDown());

        latch.await(5, TimeUnit.SECONDS);

        // 3. Launch the User Event Details Activity with the mock event ID
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailsActivity.class);
        intent.putExtra("eventId", mockEventId); // CamelCase for the User view
        ActivityScenario.launch(intent);

        Thread.sleep(3000); // Wait for Firestore data to load into the UI

        // 4. Click the Join Waitlist button
        onView(withId(R.id.join_button)).perform(click());
        Thread.sleep(1500); // Wait for the Android OS to pop up the permission dialog

        // 5. USE UIAUTOMATOR to interact with the Android OS permission dialog
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Android permission dialogs vary by version ("While using the app", "Allow", etc.)
        UiObject allowLocationBtn = device.findObject(new UiSelector().textMatches("(?i).*while using.*|.*allow.*|.*precise.*"));

        // ASSERT: The system dialog is present and we click it
        if (allowLocationBtn.exists()) {
            allowLocationBtn.click();
        } else {
            throw new AssertionError("Location permission dialog did not appear! Ensure location permissions aren't already granted on this emulator.");
        }
    }

    @Test
    public void testOrganizerCanNavigateToMapForEvent() throws InterruptedException {
        // 1. Setup the mock logged-in Organizer/Admin user
        User mockOrganizer = new User();
        mockOrganizer.setId("MockOrgId999");
        mockOrganizer.setName("Test Organizer");
        mockOrganizer.setRole(User.Role.ADMIN.name()); // Assuming ADMIN is your organizer role

        // Inject the dummy organizer into your app's global state
        CurrentUser.set(mockOrganizer);

        // 2. Setup: Inject a mock event so the Organizer view has data to load
        CountDownLatch latch = new CountDownLatch(1);
        String mockEventId = "GeoOrgTestEvent";

        Event mockEvent = new Event();
        mockEvent.setId(mockEventId);
        mockEvent.setName("Organizer Map Test Event");
        mockEvent.setEnforceLocation(true);

        // Inject a dummy user into the waitlist with coordinates
        User dummyUser = new User();
        dummyUser.setId("MockUser123");
        dummyUser.setName("Map Test User");
        dummyUser.setLatitude(53.5461); // Edmonton Coordinates
        dummyUser.setLongitude(-113.4938);

        // Safely initialize the ArrayList before adding it to the event
        ArrayList<User> mockWaitlist = new ArrayList<>();
        mockWaitlist.add(dummyUser);
        mockEvent.setWaitingList(mockWaitlist);

        FirebaseFirestore.getInstance().collection("events").document(mockEventId)
                .set(mockEvent).addOnCompleteListener(task -> latch.countDown());

        latch.await(5, TimeUnit.SECONDS);

        // 3. Launch the Organizer Event Details Activity directly
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailsOrganizer.class);
        intent.putExtra("event_id", mockEventId); // snake_case for the Organizer view
        ActivityScenario.launch(intent);

        Thread.sleep(3000); // Wait for Firebase data to populate the UI

        // 4. Scroll to the Map button in the horizontal scroll view and click it
        onView(withId(R.id.mapBtn)).perform(scrollTo(), click());
        Thread.sleep(2000); // Wait for OrganizerWaitlistMapActivity to launch

        // 5. ASSERT: Verify the Map Fragment container is displayed on the new screen.
        onView(withId(R.id.mapFragment)).check(matches(isDisplayed()));
    }
}