package com.example.edgers_lottery.services;

import android.util.Log;

import com.example.edgers_lottery.models.core.User;
import com.google.firebase.Timestamp;
import com.example.edgers_lottery.models.core.WaitlistUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private static final String TYPE_DECLINED_INVITE = "CANCELLED";
    private static final String TYPE_WAITLIST_UPDATE = "WAITLIST_UPDATE";
    private static final String TYPE_CO_ORGANIZER_INVITE = "CO_ORGANIZER_INVITE";

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
        // Check if user has notifications enabled before writing
        db.collection("users").document(userId).get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        Boolean notificationsEnabled = userDoc.getBoolean("notificationsEnabled");
                        // If field is null it means existing user before feature — default to true
                        if (Boolean.FALSE.equals(notificationsEnabled)) {
                            android.util.Log.d("NotificationService", "Notifications disabled for " + userId + ", skipping");
                            return;
                        }
                    }
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
                });
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

    /**
     * Appends a CANCELLED notification for a single user.
     * Called by EventUserChoice when a user declines their event invitation.
     *
     * @param userId    the user who declined
     * @param eventId   the event they declined
     * @param eventName display name of the event
     */
    public static void sendCancelledNotification(String userId, String eventId, String eventName) {
        appendNotification(FirebaseFirestore.getInstance(), userId, eventId, eventName, TYPE_DECLINED_INVITE);
    }

    /**
     * Sends a WAITLIST_UPDATE notification to all users on the waitlist.
     * Called by EventWaitlistTab when the organizer clicks Notify Waitlisters.
     *
     * @param waitlistUsers list of users currently on the waitlist
     * @param eventId       the event ID
     * @param eventName     display name of the event
     */
    public static void sendWaitlistUpdateNotifications(List<WaitlistUser> waitlistUsers, String eventId, String eventName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (WaitlistUser user : waitlistUsers) {
            appendNotification(db, user.getUserId(), eventId, eventName, TYPE_WAITLIST_UPDATE);
        }
    }

    /**
     * Appends a CO_ORGANIZER_INVITE notification for a single user.
     * Called by EventEntrantOrganizer when an organizer assigns an entrant as co-organizer.
     *
     * @param userId    the user who has been assigned as co-organizer
     * @param eventId   the event they have been assigned to
     * @param eventName display name of the event
     */
    public static void sendCoOrganizerInviteNotification(String userId, String eventId, String eventName) {
        appendNotification(FirebaseFirestore.getInstance(), userId, eventId, eventName, TYPE_CO_ORGANIZER_INVITE);
    }
}