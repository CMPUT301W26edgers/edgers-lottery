package com.example.edgers_lottery;

import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.edgers_lottery.models.core.Event;
import com.example.edgers_lottery.models.core.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
@RunWith(AndroidJUnit4.class)

public class ViewEventsTest {
    private static final String TAG = "ViewEventsTest";

    private FirebaseFirestore db;
    private CollectionReference events;

    @Before
    public void setup() {
        db = FirebaseFirestore.getInstance();
//        db.useEmulator("10.0.2.2", 8080); // Android emulator localhost
        events = db.collection("events");
    }
    @Test
    public void viewEventsTest() throws InterruptedException {
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        CountDownLatch latch3 = new CountDownLatch(1);
        boolean[] success = {false};
        Event event1 = new Event("Event 1", "Description 1", "Date 1", "Time 1", "Location 1", new User(), 100, "Registration Start 1", "Registration End 1");
        Event event2 = new Event("Event 2", "Description 2", "Date 2", "Time 2", "Location 2", new User(), 200, "Registration Start 2", "Registration End 2");
        Event event3 = new Event("Event 3", "Description 3", "Date 3", "Time 3", "Location 3", new User(), 300, "Registration Start 3", "Registration End 3");
        /////// event one
        events.document("1").set(event1)
                .addOnSuccessListener(aVoid -> {
                    success[0] = true;
                    android.util.Log.d(TAG, "Event added successfully");
                    latch1.countDown();
                })
                .addOnFailureListener(e -> {latch1.countDown();
                    android.util.Log.e(TAG, "Error adding document", e);
                });
        latch1.await(20, TimeUnit.SECONDS);
        assertTrue("Event 1should have been saved", success[0]);
        // event 2
        success[0] = false;
        events.document("2").set(event2)
                .addOnSuccessListener(aVoid -> {
                    success[0] = true;
                    android.util.Log.d(TAG, "Event added successfully");
                    latch2.countDown();
                })
                .addOnFailureListener(e -> {latch2.countDown();
                    android.util.Log.e(TAG, "Error adding document", e);
                });
        latch2.await(20, TimeUnit.SECONDS);
        assertTrue("Event 2 should have been saved", success[0]);
        // event 3
        success[0] = false;
        events.document("3").set(event3)
                .addOnSuccessListener(aVoid -> {
                    success[0] = true;
                    android.util.Log.d(TAG, "Event added successfully");
                    latch3.countDown();
                })
                .addOnFailureListener(e -> {latch3.countDown();
                    android.util.Log.e(TAG, "Error adding document", e);
                });
        latch3.await(20, TimeUnit.SECONDS);
        assertTrue("Event 3 should have been saved", success[0]);

        // view events
        boolean[] fetched = {false};
        CountDownLatch fetchLatch = new CountDownLatch(1);
        events.get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = 0;
                    for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot.getDocuments()) {
                        count++;
                        fetched[0] = true;
                        android.util.Log.d(TAG, "Event fetched successfully");
                    }
                    android.util.Log.d(TAG, "Number of events fetched: " + count);
                    fetchLatch.countDown();
                });
        fetchLatch.await(20, TimeUnit.SECONDS);
        assertTrue("Events should have been fetched", fetched[0]);

    }
    // add @After to delete events after test
    @After
    public void cleanup() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        events.document("1").delete().addOnCompleteListener(t -> latch.countDown());
        events.document("2").delete().addOnCompleteListener(t -> latch.countDown());
        events.document("3").delete().addOnCompleteListener(t -> latch.countDown());
        latch.await(20, TimeUnit.SECONDS);
    }
}
