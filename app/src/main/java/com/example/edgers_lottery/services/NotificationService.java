package com.example.edgers_lottery.services;

import android.util.Log;

import com.example.edgers_lottery.models.User;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles writing notifications to Firestore.
 * Each user has a single document (ID = userId) in the notifications collection.
 * Notifications are stored as an array of maps inside that document.
 */
public class NotificationService {

    private static final String COLLECTION = "notifications";
    private static final String TYPE_SELECTED = "SELECTED";
    private static final String TYPE_NOT_SELECTED = "NOT_SELECTED";
    private static final String TYPE_JOINED_WAITLIST = "JOINED_WAITLIST";
    private static final String TYPE_PRIVATE_INVITE = "PRIVATE_EVENT_INVITE";
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_ACCEPTED = "accepted";
    private static final String STATUS_DECLINED = "declined";
    /**
     * Appends a notification to each selected and rejected user's document.
     * Called by LotteryService once the lottery resolves.
     */
    public static void sendLotteryResults(String eventId, String eventName,
                                          ArrayList<User> selected,
                                          ArrayList<User> notSelected) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (User user : selected) {
            appendNotification(db, user.getId(), eventId, eventName, TYPE_SELECTED);
        }
        for (User user : notSelected) {
            appendNotification(db, user.getId(), eventId, eventName, TYPE_NOT_SELECTED);
        }
    }

    /**
     * Appends a JOINED_WAITLIST notification for a single user.
     * Called by EventDetailsActivity when a user joins a waitlist.
     *
     * @param userId    the user who joined
     * @param eventId   the event they joined
     * @param eventName display name of the event
     */
    public static void sendWaitlistJoinedNotification(String userId, String eventId, String eventName) {
        appendNotification(FirebaseFirestore.getInstance(), userId, eventId, eventName, TYPE_JOINED_WAITLIST);
    }

    /**
     * Shared internal method — builds and writes one notification entry.
     */
    private static void appendNotification(FirebaseFirestore db, String userId,
                                           String eventId, String eventName, String type) {
        Map<String, Object> notificationEntry = new HashMap<>();
        notificationEntry.put("eventId",   eventId);
        notificationEntry.put("eventName", eventName);
        notificationEntry.put("type",      type);
        notificationEntry.put("timestamp", com.google.firebase.Timestamp.now());
        notificationEntry.put("isRead",    false);

        Map<String, Object> update = new HashMap<>();
        update.put("notifications", FieldValue.arrayUnion(notificationEntry));

        db.collection(COLLECTION).document(userId)
                .set(update, SetOptions.merge())
                .addOnSuccessListener(unused ->
                        android.util.Log.d("NotificationService", type + " notif written for " + userId))
                .addOnFailureListener(e ->
                        android.util.Log.e("NotificationService", "Failed " + type + " notif for " + userId, e));
    }

    public static void sendPrivateEventInvite(String userId, String eventId, String eventName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> notificationEntry = new HashMap<>();
        notificationEntry.put("eventId", eventId);
        notificationEntry.put("eventName", eventName);
        notificationEntry.put("type", TYPE_PRIVATE_INVITE);
        notificationEntry.put("timestamp", Timestamp.now());
        notificationEntry.put("isRead", false);
        notificationEntry.put("status", STATUS_PENDING);
        notificationEntry.put("message", "You have been invited to join the waitlist for this event.");

        Map<String, Object> update = new HashMap<>();
        update.put("notifications", FieldValue.arrayUnion(notificationEntry));

        db.collection(COLLECTION).document(userId)
                .set(update, SetOptions.merge())
                .addOnSuccessListener(unused ->
                        Log.d("NotificationService", "PRIVATE_EVENT_INVITE notif written for " + userId))
                .addOnFailureListener(e ->
                        Log.e("NotificationService", "Failed PRIVATE_EVENT_INVITE notif for " + userId, e));
    }

}