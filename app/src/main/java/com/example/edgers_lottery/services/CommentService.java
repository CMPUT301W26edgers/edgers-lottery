package com.example.edgers_lottery.services;

import android.content.Context;
import android.widget.Toast;

import com.example.edgers_lottery.models.Comment;
import com.example.edgers_lottery.models.CurrentUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CommentService {
    public static void addComment(String eventId, String userID , String commentText) {
        // get the current date and time

//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()); // this is for UTC
//        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//        String timestamp = sdf.format(new Date());

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()); // this is for local time
        // create a new comment object
        Comment comment = new Comment();
        comment.setUserID(userID);
        comment.setEventID(eventId);
        comment.setCommentText(commentText);
        comment.setTimestamp(timestamp);
        // send to firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("comments").add(comment)
                .addOnSuccessListener(documentReference -> {
                    android.util.Log.d("CommentService", "Comment added with ID: " + documentReference.getId());
                    db.collection("comments").document(documentReference.getId()).update("id", documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("CommentService", "Failed to add comment: " + e.getMessage());
                });
    }
    // used on both the user and organizer sides
    public static void getCommentsForEvent(String eventId, CommentCallback callback) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("comments")
                .whereEqualTo("eventID", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Comment> comments = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots) {
                        Comment comment = document.toObject(Comment.class);
                        if (comment != null) {
                            comment.setId(document.getId());
                            comments.add(comment);
                        }

                    }
                    callback.onComplete(comments);
                    android.util.Log.d("CommentService", "Comments retrieved for event: " + eventId);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("CommentService", "Failed to retrieve comments for event: " + eventId, e);
                    callback.onComplete(new ArrayList<>());
                });
    }
    public static void getCommentsForAdmin(CommentCallback callback){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("comments").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Comment> comments = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots) {
                        Comment comment = document.toObject(Comment.class);
                        comments.add(comment);
                    }
                    callback.onComplete(comments);
                    android.util.Log.d("CommentService", "Comments retrieved for admin");
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("CommentService", "Failed to retrieve comments for admin", e);
                    callback.onComplete(new ArrayList<>());
                });
    }

    // this might not be implemented in the app, its here though
    public static void userDeleteComment(String commentId, Context context) {
        String currentUserId = CurrentUser.get().getId();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("comments").document(commentId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String commentOwnerId = document.getString("userID");
                        if (currentUserId.equals(commentOwnerId)) {
                            db.collection("comments").document(commentId).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to delete comment", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(context, "You can only delete your own comments", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to find comment", Toast.LENGTH_SHORT).show();
                });
    }
    public static void orgranizerDeleteComment(String commentId, Context context) {
        if (CurrentUser.get().isOrganizer()) {
            // then check if it's their event
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("comments").document(commentId).get()
                    .addOnSuccessListener(commentDocument -> {
                        if (commentDocument.exists()) {
                            String commentEventId = commentDocument.getString("eventID");
                            if (commentEventId == null) {
                                Toast.makeText(context, "Comment has no event", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            db.collection("events").document(commentEventId).get()
                                    .addOnSuccessListener(eventDocument -> {
                                        if (eventDocument.exists()) {
                                            String eventOwnerId = eventDocument.getString("organizerId");
                                            String currentUserId = CurrentUser.get().getId();
                                            if (currentUserId.equals(eventOwnerId)) {
                                                db.collection("comments").document(commentId).delete()
                                                        .addOnSuccessListener(aVoid -> {
                                                            Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(context, "Failed to delete comment", Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        }else{
                                            Toast.makeText(context, "Failed to find event", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to find event", Toast.LENGTH_SHORT).show();
                                    });
                                }else{
                                    Toast.makeText(context, "Failed to find comment", Toast.LENGTH_SHORT).show();
                                }


                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to find comment", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(context, "You are not an organizer... How'd you get here?!", Toast.LENGTH_LONG).show();
        }
    }
    public static void adminDeleteComment(String commentId, Context context) {
        // check user is admin
        if ("ADMIN".equals(CurrentUser.get().getRole())) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("comments").document(commentId).delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to delete comment", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(context, "You are not an admin... How'd you get here?!", Toast.LENGTH_LONG).show();
        }
    }
    public static void deleteCommentsOnEvent(String eventId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("comments")
                .whereEqualTo("eventID", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots) {
                        db.collection("comments").document(document.getId()).delete();
                    }
                    android.util.Log.d("CommentService", "Comments for event deleted: " + eventId);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("CommentService", "Failed to delete comments for event: " + eventId, e);
                });
    }
}
