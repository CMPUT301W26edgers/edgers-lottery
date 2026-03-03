package com.example.edgers_lottery;

import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
@RunWith(AndroidJUnit4.class)
public class ProfileTest {

    // try to add a profile
    private static final String TAG = "ProfileTest";
    private FirebaseFirestore db;
    private CollectionReference profiles;

    @Before
    public void setup() {
        db = FirebaseFirestore.getInstance();
//        db.useEmulator("10.0.2.2", 8080); // Android emulator localhost
        profiles = db.collection("profiles");
    }
    @Test
    public void addProfileTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1); // need this to get the test to wait for the store to be done
        boolean[] success = {false};
        profiles.document("1").set(new Entrant("1", "John Doe", "john@email.com", "123"))
                .addOnSuccessListener(aVoid -> {
                    success[0] = true;
                    android.util.Log.d(TAG, "Profile added successfully");
                    latch.countDown();
                })
                .addOnFailureListener(e -> {latch.countDown();
                    android.util.Log.e(TAG, "Error adding document", e);
                });

        latch.await(20, TimeUnit.SECONDS);
        assertTrue("Profile should have been saved", success[0]);
    }
}




