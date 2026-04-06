package com.example.edgers_lottery;

import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.edgers_lottery.models.Comment;
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
public class CommentTest {

    private static final String TAG = "CommentTest";
    private static final String EVENT_ID = "BgI19xgs0731KryGQdUq";
    private static final String USER_ID = "HIIQ64MVJMSLSD9cq27rvIzlKQH3";

    private FirebaseFirestore db;
    private List<String> addedCommentIds = new ArrayList<>();

    @Before
    public void setup() {
        db = FirebaseFirestore.getInstance();
    }

    @Test
    public void testAddCommentsToEvent() throws InterruptedException {
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        CountDownLatch latch3 = new CountDownLatch(1);
        boolean[] success = {false};

        // comment 1
        Comment comment1 = new Comment();
        comment1.setUserID(USER_ID);
        comment1.setEventID(EVENT_ID);
        comment1.setCommentText("This event looks amazing!");
        comment1.setTimestamp("2026-03-18");

        db.collection("comments").add(comment1)
                .addOnSuccessListener(documentReference -> {
                    addedCommentIds.add(documentReference.getId());
                    success[0] = true;
                    android.util.Log.d(TAG, "Comment 1 added with ID: " + documentReference.getId());
                    latch1.countDown();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e(TAG, "Failed to add comment 1: " + e.getMessage());
                    latch1.countDown();
                });
        latch1.await(20, TimeUnit.SECONDS);
        assertTrue("Comment 1 should have been added", success[0]);

        // comment 2
        success[0] = false;
        Comment comment2 = new Comment();
        comment2.setUserID(USER_ID);
        comment2.setEventID(EVENT_ID);
        comment2.setCommentText("Can't wait to attend!");
        comment2.setTimestamp("2026-03-19");

        db.collection("comments").add(comment2)
                .addOnSuccessListener(documentReference -> {
                    addedCommentIds.add(documentReference.getId());
                    success[0] = true;
                    android.util.Log.d(TAG, "Comment 2 added with ID: " + documentReference.getId());
                    latch2.countDown();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e(TAG, "Failed to add comment 2: " + e.getMessage());
                    latch2.countDown();
                });
        latch2.await(20, TimeUnit.SECONDS);
        assertTrue("Comment 2 should have been added", success[0]);

        // comment 3
        success[0] = false;
        Comment comment3 = new Comment();
        comment3.setUserID(USER_ID);
        comment3.setEventID(EVENT_ID);
        comment3.setCommentText("Is there parking nearby?");
        comment3.setTimestamp("2026-03-20");

        db.collection("comments").add(comment3)
                .addOnSuccessListener(documentReference -> {
                    addedCommentIds.add(documentReference.getId());
                    success[0] = true;
                    android.util.Log.d(TAG, "Comment 3 added with ID: " + documentReference.getId());
                    latch3.countDown();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e(TAG, "Failed to add comment 3: " + e.getMessage());
                    latch3.countDown();
                });
        latch3.await(20, TimeUnit.SECONDS);
        assertTrue("Comment 3 should have been added", success[0]);
    }

    @After
    public void cleanup() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(addedCommentIds.size());
        for (String id : addedCommentIds) {
            db.collection("comments").document(id).delete()
                    .addOnCompleteListener(t -> latch.countDown());
        }
        latch.await(20, TimeUnit.SECONDS);
    }
}