package com.example.edgers_lottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
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
import com.example.edgers_lottery.views.EventCommentsActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class EntrantCommentUITest {

    private static final String EVENT_ID = "BgI19xgs0731KryGQdUq";
    private static final String SOME_ID = "8PtvERpeA7V3zQStUhz11fH4b7a2"; // use Maanas's id

    private FirebaseFirestore db;
    private List<String> addedCommentIds = new ArrayList<>();

    @Before
    public void setup() {
        db = FirebaseFirestore.getInstance();

        User entrant = new User();
        entrant.setId(SOME_ID);
        entrant.setName("Test");
        entrant.setEmail("Test@test.com");
//        entrant.setRole("ENTRANT");
        CurrentUser.set(entrant);
    }

    private ActivityScenario<EventCommentsActivity> launchActivity() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventCommentsActivity.class);
        intent.putExtra("event_id", EVENT_ID);
        return ActivityScenario.launch(intent);
    }

    @Test
    public void testCommentsScreenLoads() {
        try (ActivityScenario<EventCommentsActivity> scenario = launchActivity()) {
            onView(withId(R.id.commentsList)).check(matches(isDisplayed()));
            onView(withId(R.id.etCommentInput)).check(matches(isDisplayed()));
            onView(withId(R.id.btnPostComment)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testEntrantCanTypeComment() {
        try (ActivityScenario<EventCommentsActivity> scenario = launchActivity()) {
            onView(withId(R.id.etCommentInput))
                    .perform(click(), replaceText("Entrant UI test comment"), closeSoftKeyboard());

            onView(withId(R.id.etCommentInput))
                    .check(matches(withText("Entrant UI test comment")));
        }
    }

    @Test
    public void testEmptyCommentDoesNotPost() throws InterruptedException {
        try (ActivityScenario<EventCommentsActivity> scenario = launchActivity()) {
            Thread.sleep(1000);
            onView(withId(R.id.btnPostComment))
                    .perform(click());
            onView(withId(R.id.etCommentInput)).check(matches(withText("")));
        }
    }

    @Test
    public void testEntrantCanPostComment() throws InterruptedException {
        try (ActivityScenario<EventCommentsActivity> scenario = launchActivity()) {
            Thread.sleep(2000);
            onView(withId(R.id.etCommentInput))
                    .perform(replaceText("UI test entrant comment"), closeSoftKeyboard());
            Thread.sleep(500);
            onView(withId(R.id.btnPostComment)).perform(click());
            Thread.sleep(2000);
            onView(withId(R.id.etCommentInput)).check(matches(withText("")));
        }
        cleanupCommentsByUser(SOME_ID, "UI test entrant comment");
    }

    @Test
    public void testBackButtonIsVisible() {
        try (ActivityScenario<EventCommentsActivity> scenario = launchActivity()) {
            onView(withId(R.id.btnBackComments)).check(matches(isDisplayed()));
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