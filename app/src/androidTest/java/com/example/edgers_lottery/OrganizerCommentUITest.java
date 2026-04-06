package com.example.edgers_lottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.edgers_lottery.models.CurrentUser;
import com.example.edgers_lottery.models.User;
import com.example.edgers_lottery.views.EventCommentsOrganizer;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class OrganizerCommentUITest {
    private static final String EVENT_ID = "BgI19xgs0731KryGQdUq";
    private static final String ORGANIZER_ID = "HIIQ64MVJMSLSD9cq27rvIzlKQH3";
    private FirebaseFirestore db;
    private List<String> addedCommentIds = new ArrayList<>();

    @Before
    public void setup() {
        db = FirebaseFirestore.getInstance();
        // set up organizer user
        User organizer = new User();
        organizer.setId(ORGANIZER_ID);
        organizer.setName("tamu s");
        organizer.setEmail("tamu@test.com");
        organizer.setRole("ORGANIZER");
        organizer.setOrganizer(true);
        CurrentUser.set(organizer);
    }

    private ActivityScenario<EventCommentsOrganizer> launchActivity() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCommentsOrganizer.class);
        intent.putExtra("event_id", EVENT_ID);
        return ActivityScenario.launch(intent);
    }

    @Test
    public void testCommentsScreenLoads() {
        try (ActivityScenario<EventCommentsOrganizer> scenario = launchActivity()) {
            onView(withId(R.id.commentsList)).check(matches(isDisplayed()));
            onView(withId(R.id.etCommentInput)).check(matches(isDisplayed()));
            onView(withId(R.id.btnPostComment)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testOrganizerCanTypeComment() {
        try (ActivityScenario<EventCommentsOrganizer> scenario = launchActivity()) {
            onView(withId(R.id.etCommentInput))
                    .perform(click(), clearText(), replaceText("Organizer UI test comment"), closeSoftKeyboard());

            onView(withId(R.id.etCommentInput))
                    .check(matches(withText("Organizer UI test comment")));
        }
    }

    @Test
    public void testOrganizerCanPostComment() throws InterruptedException {
        try (ActivityScenario<EventCommentsOrganizer> scenario = launchActivity()) {
            // wait for comments to load
            Thread.sleep(2000);

            onView(withId(R.id.etCommentInput))
                    .perform(click(), typeText("UI test organizer comment"), closeSoftKeyboard());

            onView(withId(R.id.btnPostComment)).perform(click());

            // wait for Firestore to save
            Thread.sleep(2000);

            // input should be cleared after posting
            onView(withId(R.id.etCommentInput)).check(matches(withText("")));
        }

        // clean up the comment we just posted
        cleanupCommentsByUser(ORGANIZER_ID, "UI test organizer comment");
    }

    @Test
    public void testPostCommentButtonIsVisible() {
        try (ActivityScenario<EventCommentsOrganizer> scenario = launchActivity()) {
            onView(withId(R.id.btnPostComment)).check(matches(isDisplayed()));
        }
    }


    @Test
    public void testEmptyCommentDoesNotPost() throws InterruptedException {
        try (ActivityScenario<EventCommentsOrganizer> scenario = launchActivity()) {
            Thread.sleep(1000);
            // click post without typing anything
            onView(withId(R.id.btnPostComment)).perform(click());
            // input should still be empty and no crash
            onView(withId(R.id.etCommentInput)).check(matches(withText("")));
        }
    }

    private void cleanupCommentsByUser(String userId, String text) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        db.collection("comments")
                .whereEqualTo("eventID", EVENT_ID)
                .whereEqualTo("userID", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    CountDownLatch deleteLatch = new CountDownLatch(querySnapshot.size());
                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        if (text.equals(doc.getString("commentText"))) {
                            doc.getReference().delete()
                                    .addOnCompleteListener(t -> deleteLatch.countDown());
                        } else {
                            deleteLatch.countDown();
                        }
                    }
                    try { deleteLatch.await(10, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
                    latch.countDown();
                })
                .addOnFailureListener(e -> latch.countDown());
        latch.await(20, TimeUnit.SECONDS);
    }

    @After
    public void cleanup() throws InterruptedException {
        if (!addedCommentIds.isEmpty()) {
            CountDownLatch latch = new CountDownLatch(addedCommentIds.size());
            for (String id : addedCommentIds) {
                db.collection("comments").document(id).delete()
                        .addOnCompleteListener(t -> latch.countDown());
            }
            latch.await(20, TimeUnit.SECONDS);
        }
    }
}