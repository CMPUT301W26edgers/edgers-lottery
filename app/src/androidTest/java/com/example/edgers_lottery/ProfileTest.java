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
        profiles.document("1").set(new User("1", "John Doe", "john@email.com", "ENTRANT"))
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
    @Test
    public void updateProfileTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        boolean[] updated = {false};
        profiles.document("1").set(new User("1", "John Doe", "john@email.com", "ENTRANT"))
                .addOnSuccessListener(aVoid -> {
                    profiles.document("1").update("name", "Jane Doe")
                            .addOnSuccessListener(aVoid1 -> {
                                profiles.document("1")
                                        .get()
                                        .addOnSuccessListener(snapshot -> {
                                            String name = snapshot.getString("name");
                                            updated[0] = name != null && name.equals("Jane Doe");
                                            android.util.Log.d(TAG, "Profile updated successfully");
                                            latch.countDown();
                                        });
                            });
                })
                .addOnFailureListener(e -> {latch.countDown();
                    android.util.Log.e(TAG, "Error adding document", e);
                });

                latch.await(20, TimeUnit.SECONDS);
                if (updated[0]) {
                    assertTrue("Profile should have been updated", updated[0]);
                }
    }

    @Test
    public void deleteProfileTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        boolean[] deleted = {false};

        profiles.document("2").set(new User("2", "Bob", "bob@email.com", "ENTRANT"))
                .addOnSuccessListener(aVoid -> {
                    profiles.document("2")
                            .delete()
                            .addOnSuccessListener(aVoid1 -> {
                                profiles.document("2")
                                        .get()
                                        .addOnSuccessListener(snapshot -> {
                                            deleted[0] = !snapshot.exists();
                                            android.util.Log.d(TAG, "Profile deleted successfully");
                                            latch.countDown();
                                        })
                                        .addOnFailureListener(e -> {latch.countDown();
                                            android.util.Log.e(TAG, "Error fetching profile", e);
                                        });;
                            })
                            .addOnFailureListener(e -> {latch.countDown();
                                android.util.Log.e(TAG, "Error deleting profile", e);
                            });
                })
                .addOnFailureListener(e -> {latch.countDown();
                    android.util.Log.e(TAG, "Error adding profile", e);
                });

        latch.await(20, TimeUnit.SECONDS);
        assertTrue("Profile should be deleted", deleted[0]);

    }
}




