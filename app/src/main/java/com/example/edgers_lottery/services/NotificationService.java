package com.example.edgers_lottery.services;

import com.example.edgers_lottery.models.AppNotification;
import com.example.edgers_lottery.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Handles writing lottery outcome notifications to Firestore.
 * Called by {@link LotteryService} once the lottery resolves.
 */
public class NotificationService {

    private static final String COLLECTION = "notifications";
    private static final String TYPE_SELECTED = "SELECTED";
    private static final String TYPE_NOT_SELECTED = "NOT_SELECTED";

    /**
     * Writes a notification doc to Firestore for every selected and rejected user.
     *
     * @param eventId     Firestore ID of the event the lottery was run for
     * @param eventName   display name of the event
     * @param selected    users chosen by the lottery
     * @param notSelected users not chosen by the lottery
     */
    public static void sendLotteryResults(String eventId, String eventName,
                                          ArrayList<User> selected,
                                          ArrayList<User> notSelected) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (User user : selected) {
            AppNotification notif = new AppNotification(
                    user.getId(), eventId, eventName, TYPE_SELECTED
            );
            db.collection(COLLECTION).add(notif)
                    .addOnSuccessListener(ref ->
                            android.util.Log.d("NotificationService", "SELECTED notif written for " + user.getId()))
                    .addOnFailureListener(e ->
                            android.util.Log.e("NotificationService", "Failed SELECTED notif for " + user.getId(), e));
        }

        for (User user : notSelected) {
            AppNotification notif = new AppNotification(
                    user.getId(), eventId, eventName, TYPE_NOT_SELECTED
            );
            db.collection(COLLECTION).add(notif)
                    .addOnSuccessListener(ref ->
                            android.util.Log.d("NotificationService", "NOT_SELECTED notif written for " + user.getId()))
                    .addOnFailureListener(e ->
                            android.util.Log.e("NotificationService", "Failed NOT_SELECTED notif for " + user.getId(), e));
        }
    }
}