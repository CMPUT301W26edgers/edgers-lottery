package com.example.edgers_lottery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.edgers_lottery.models.core.Comment;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class EntrantCommentTest {

    private static final String TAG = "EntrantCommentTest";
    private static final String EVENT_ID = "BgI19xgs0731KryGQdUq";

    // entrant users
    private static final String MAANAS_ID = "8PtvERpeA7V3zQStUhz11fH4b7a2";
    private static final String NATHAN_ID = "gwiY6Wr2LxhFJ7jkZic6J2sMzqz2";
    private static final String FAIYAZ_ID = "uXFeVGUOySZm5h3UFuNuwjrvnNi1";
    private static final String OKIB_ID   = "JNINC70qaJXD6kVXbA7Xc0QWbZ63";
    private static final String TONY_ID   = "Nhsl3856lnX3PCJQRIJP3b3sKRy2";
    private static final String TAMU_ID   = "HIIQ64MVJMSLSD9cq27rvIzlKQH3";

    private FirebaseFirestore db;
    private List<String> addedCommentIds = new ArrayList<>();

    @Before
    public void setup() {
        db = FirebaseFirestore.getInstance();
    }

    private Comment buildComment(String userId, String text) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
        Comment comment = new Comment();
        comment.setUserID(userId);
        comment.setEventID(EVENT_ID);
        comment.setCommentText(text);
        comment.setTimestamp(timestamp);
        return comment;
    }

    private String addComment(Comment comment) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String[] id = {null};
        db.collection("comments").add(comment)
                .addOnSuccessListener(ref -> {
                    id[0] = ref.getId();
                    addedCommentIds.add(ref.getId());
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e(TAG, "Failed to add comment: " + e.getMessage());
                    latch.countDown();
                });
        latch.await(20, TimeUnit.SECONDS);
        return id[0];
    }

    @Test
    public void testEntrantsCanPostComments() throws InterruptedException {
        boolean[] allSuccess = {true};

        String[] commentIds = new String[6];
        commentIds[0] = addComment(buildComment(MAANAS_ID, "Really excited for this event!"));
        commentIds[1] = addComment(buildComment(NATHAN_ID, "What should we bring?"));
        commentIds[2] = addComment(buildComment(FAIYAZ_ID, "Is parking available nearby?"));
        commentIds[3] = addComment(buildComment(OKIB_ID,   "Can't wait to see everyone there!"));
        commentIds[4] = addComment(buildComment(TONY_ID,   "Will there be food at the event?"));
        commentIds[5] = addComment(buildComment(TAMU_ID,   "Looking forward to it!"));

        for (String id : commentIds) {
            if (id == null) allSuccess[0] = false;
        }

        assertTrue("All entrants should be able to post comments", allSuccess[0]);
    }

    @Test
    public void testCommentsAreLinkedToEvent() throws InterruptedException {
        String id = addComment(buildComment(MAANAS_ID, "This comment should be linked to the event"));
        assertTrue("Comment should have been saved", id != null);

        CountDownLatch latch = new CountDownLatch(1);
        boolean[] linked = {false};

        db.collection("comments").document(id).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        linked[0] = EVENT_ID.equals(doc.getString("eventID"));
                    }
                    latch.countDown();
                })
                .addOnFailureListener(e -> latch.countDown());

        latch.await(20, TimeUnit.SECONDS);
        assertTrue("Comment should be linked to the correct event", linked[0]);
    }

    @Test
    public void testCommentsAreLinkedToUser() throws InterruptedException {
        String id = addComment(buildComment(TONY_ID, "This comment should be linked to Tony"));
        assertTrue("Comment should have been saved", id != null);

        CountDownLatch latch = new CountDownLatch(1);
        boolean[] linked = {false};

        db.collection("comments").document(id).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        linked[0] = TONY_ID.equals(doc.getString("userID"));
                    }
                    latch.countDown();
                })
                .addOnFailureListener(e -> latch.countDown());

        latch.await(20, TimeUnit.SECONDS);
        assertTrue("Comment should be linked to the correct user", linked[0]);
    }

    @Test
    public void testFetchCommentsForEvent() throws InterruptedException {
        // add a comment first
        addComment(buildComment(NATHAN_ID, "Test fetch comment"));

        CountDownLatch latch = new CountDownLatch(1);
        boolean[] fetched = {false};

        db.collection("comments")
                .whereEqualTo("eventID", EVENT_ID)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    fetched[0] = !querySnapshot.isEmpty();
                    android.util.Log.d(TAG, "Fetched " + querySnapshot.size() + " comments");
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e(TAG, "Failed: " + e.getMessage());
                    latch.countDown();
                });

        latch.await(20, TimeUnit.SECONDS);
        assertTrue("Should be able to fetch comments for the event", fetched[0]);
    }

    @Test
    public void testEntrantCanDeleteOwnComment() throws InterruptedException {
        // add a comment
        String id = addComment(buildComment(OKIB_ID, "I want to delete this comment"));
        assertTrue("Comment should have been saved", id != null);

        // verify it exists
        CountDownLatch latch1 = new CountDownLatch(1);
        boolean[] exists = {false};
        db.collection("comments").document(id).get()
                .addOnSuccessListener(doc -> {
                    exists[0] = doc.exists();
                    latch1.countDown();
                })
                .addOnFailureListener(e -> latch1.countDown());
        latch1.await(20, TimeUnit.SECONDS);
        assertTrue("Comment should exist before deletion", exists[0]);

        // delete it
        CountDownLatch latch2 = new CountDownLatch(1);
        boolean[] deleted = {false};
        db.collection("comments").document(id).delete()
                .addOnSuccessListener(aVoid -> {
                    deleted[0] = true;
                    addedCommentIds.remove(id); // already deleted, no need to clean up
                    latch2.countDown();
                })
                .addOnFailureListener(e -> latch2.countDown());
        latch2.await(20, TimeUnit.SECONDS);
        assertTrue("Comment should be deleted successfully", deleted[0]);

        // verify it's gone
        CountDownLatch latch3 = new CountDownLatch(1);
        boolean[] gone = {false};
        db.collection("comments").document(id).get()
                .addOnSuccessListener(doc -> {
                    gone[0] = !doc.exists();
                    latch3.countDown();
                })
                .addOnFailureListener(e -> latch3.countDown());
        latch3.await(20, TimeUnit.SECONDS);
        assertTrue("Comment should no longer exist after deletion", gone[0]);
    }

    @After
    public void cleanup() throws InterruptedException {
        if (addedCommentIds.isEmpty()) return;
        CountDownLatch latch = new CountDownLatch(addedCommentIds.size());
        for (String id : addedCommentIds) {
            db.collection("comments").document(id).delete()
                    .addOnCompleteListener(t -> latch.countDown());
        }
        latch.await(20, TimeUnit.SECONDS);
    }
}