package com.example.edgers_lottery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
public class OrganizerCommentTest {

    private static final String TAG = "OrganizerCommentTest";
    private static final String EVENT_ID = "BgI19xgs0731KryGQdUq";

    // organizer
    private static final String ORGANIZER_ID = "HIIQ64MVJMSLSD9cq27rvIzlKQH3";

    // entrants whose comments the organizer may delete
    private static final String MAANAS_ID = "8PtvERpeA7V3zQStUhz11fH4b7a2";
    private static final String NATHAN_ID = "gwiY6Wr2LxhFJ7jkZic6J2sMzqz2";
    private static final String FAIYAZ_ID = "uXFeVGUOySZm5h3UFuNuwjrvnNi1";
    private static final String OKIB_ID   = "JNINC70qaJXD6kVXbA7Xc0QWbZ63";
    private static final String TONY_ID   = "Nhsl3856lnX3PCJQRIJP3b3sKRy2";

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
    public void testOrganizerCanPostComment() throws InterruptedException {
        String id = addComment(buildComment(ORGANIZER_ID, "Welcome everyone to this event!"));
        assertNotNull("Organizer should be able to post a comment", id);
    }

    @Test
    public void testOrganizerCanDeleteOwnComment() throws InterruptedException {
        String id = addComment(buildComment(ORGANIZER_ID, "This comment will be deleted by the organizer"));
        assertNotNull("Comment should be saved", id);

        CountDownLatch latch = new CountDownLatch(1);
        boolean[] deleted = {false};
        db.collection("comments").document(id).delete()
                .addOnSuccessListener(aVoid -> {
                    deleted[0] = true;
                    addedCommentIds.remove(id);
                    latch.countDown();
                })
                .addOnFailureListener(e -> latch.countDown());
        latch.await(20, TimeUnit.SECONDS);
        assertTrue("Organizer should be able to delete their own comment", deleted[0]);

        // verify gone
        CountDownLatch latch2 = new CountDownLatch(1);
        boolean[] gone = {false};
        db.collection("comments").document(id).get()
                .addOnSuccessListener(doc -> {
                    gone[0] = !doc.exists();
                    latch2.countDown();
                })
                .addOnFailureListener(e -> latch2.countDown());
        latch2.await(20, TimeUnit.SECONDS);
        assertTrue("Comment should no longer exist", gone[0]);
    }

    @Test
    public void testOrganizerCanDeleteEntrantComment() throws InterruptedException {
        // entrant posts a comment
        String id = addComment(buildComment(MAANAS_ID, "This entrant comment will be deleted by the organizer"));
        assertNotNull("Entrant comment should be saved", id);

        // verify the comment belongs to the entrant
        CountDownLatch latch1 = new CountDownLatch(1);
        boolean[] isEntrant = {false};
        db.collection("comments").document(id).get()
                .addOnSuccessListener(doc -> {
                    isEntrant[0] = MAANAS_ID.equals(doc.getString("userID"));
                    latch1.countDown();
                })
                .addOnFailureListener(e -> latch1.countDown());
        latch1.await(20, TimeUnit.SECONDS);
        assertTrue("Comment should belong to the entrant", isEntrant[0]);

        // organizer deletes it
        CountDownLatch latch2 = new CountDownLatch(1);
        boolean[] deleted = {false};
        db.collection("comments").document(id).delete()
                .addOnSuccessListener(aVoid -> {
                    deleted[0] = true;
                    addedCommentIds.remove(id);
                    latch2.countDown();
                })
                .addOnFailureListener(e -> latch2.countDown());
        latch2.await(20, TimeUnit.SECONDS);
        assertTrue("Organizer should be able to delete entrant comment", deleted[0]);

        // verify gone
        CountDownLatch latch3 = new CountDownLatch(1);
        boolean[] gone = {false};
        db.collection("comments").document(id).get()
                .addOnSuccessListener(doc -> {
                    gone[0] = !doc.exists();
                    latch3.countDown();
                })
                .addOnFailureListener(e -> latch3.countDown());
        latch3.await(20, TimeUnit.SECONDS);
        assertTrue("Comment should no longer exist after organizer deletion", gone[0]);
    }

    @Test
    public void testOrganizerCanDeleteMultipleEntrantComments() throws InterruptedException {
        // multiple entrants post comments
        String id1 = addComment(buildComment(NATHAN_ID, "Nathan's comment to be deleted"));
        String id2 = addComment(buildComment(FAIYAZ_ID, "Faiyaz's comment to be deleted"));
        String id3 = addComment(buildComment(OKIB_ID,   "Okib's comment to be deleted"));
        String id4 = addComment(buildComment(TONY_ID,   "Tony's comment to be deleted"));

        assertNotNull(id1);
        assertNotNull(id2);
        assertNotNull(id3);
        assertNotNull(id4);

        // organizer deletes all of them
        CountDownLatch latch = new CountDownLatch(4);
        boolean[] allDeleted = {true};

        for (String id : new String[]{id1, id2, id3, id4}) {
            db.collection("comments").document(id).delete()
                    .addOnSuccessListener(aVoid -> {
                        addedCommentIds.remove(id);
                        latch.countDown();
                    })
                    .addOnFailureListener(e -> {
                        allDeleted[0] = false;
                        latch.countDown();
                    });
        }

        latch.await(20, TimeUnit.SECONDS);
        assertTrue("Organizer should be able to delete all entrant comments", allDeleted[0]);
    }

    @Test
    public void testOrganizerCanFetchAllCommentsForEvent() throws InterruptedException {
        // add some comments from different users
        addComment(buildComment(MAANAS_ID, "Maanas fetch test comment"));
        addComment(buildComment(NATHAN_ID, "Nathan fetch test comment"));
        addComment(buildComment(ORGANIZER_ID, "Organizer fetch test comment"));

        CountDownLatch latch = new CountDownLatch(1);
        boolean[] fetched = {false};

        db.collection("comments")
                .whereEqualTo("eventID", EVENT_ID)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    fetched[0] = !querySnapshot.isEmpty();
                    android.util.Log.d(TAG, "Organizer fetched " + querySnapshot.size() + " comments");
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e(TAG, "Failed: " + e.getMessage());
                    latch.countDown();
                });

        latch.await(20, TimeUnit.SECONDS);
        assertTrue("Organizer should be able to fetch all comments for the event", fetched[0]);
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