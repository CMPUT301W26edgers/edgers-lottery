package com.example.edgers_lottery.models.core;

import com.google.firebase.Timestamp;

/**
 * Represents a single in-app notification tied to a lottery outcome.
 * Mapped to documents in the Firestore {@code notifications} collection.
 */
public class AppNotification {

    /** Firestore document ID of the user this notification belongs to. */
    private String userId;

    /** Firestore document ID of the related event. */
    private String eventId;

    /** Display name of the related event. */
    private String eventName;

    /** Notification type. One of: {@code SELECTED}, {@code NOT_SELECTED}, {@code JOINED_WAITLIST}. */
    private String type;

    /** Time the notification was created. */
    private Timestamp timestamp;

    /** False until the user views the notification. Drives the unread indicator. */
    private boolean isRead;

    /** Required no-argument constructor for Firestore deserialization. */
    public AppNotification() {}

    /**
     * @param userId    Firestore ID of the recipient user
     * @param eventId   Firestore ID of the related event
     * @param eventName display name of the related event
     * @param type      one of {@code SELECTED}, {@code NOT_SELECTED}, {@code JOINED_WAITLIST}
     */
    public AppNotification(String userId, String eventId, String eventName, String type) {
        this.userId = userId;
        this.eventId = eventId;
        this.eventName = eventName;
        this.type = type;
        this.timestamp = Timestamp.now();
        this.isRead = false;
    }

    /** @return Firestore ID of the recipient user */
    public String getUserId() { return userId; }
    /** @param userId Firestore ID of the recipient user */
    public void setUserId(String userId) { this.userId = userId; }

    /** @return Firestore ID of the related event */
    public String getEventId() { return eventId; }
    /** @param eventId Firestore ID of the related event */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /** @return display name of the related event */
    public String getEventName() { return eventName; }
    /** @param eventName display name of the related event */
    public void setEventName(String eventName) { this.eventName = eventName; }

    /** @return notification type flag */
    public String getType() { return type; }
    /** @param type one of {@code SELECTED}, {@code NOT_SELECTED}, {@code JOINED_WAITLIST} */
    public void setType(String type) { this.type = type; }

    /** @return time the notification was created */
    public Timestamp getTimestamp() { return timestamp; }
    /** @param timestamp time the notification was created */
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    /** @return false until the user views the notification */
    public boolean isRead() { return isRead; }
    /** @param read true once the user has viewed the notification */
    public void setRead(boolean read) { isRead = read; }
}