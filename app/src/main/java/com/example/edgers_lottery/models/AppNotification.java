package com.example.edgers_lottery.models;

import com.google.firebase.Timestamp;

public class AppNotification {
    private String userId;
    private String eventId;
    private String eventName;
    private String type;         // "SELECTED", "NOT_SELECTED", "JOINED_WAITLIST"
    private Timestamp timestamp;
    private boolean isRead;

    public AppNotification() {}

    public AppNotification(String userId, String eventId, String eventName, String type) {
        this.userId = userId;
        this.eventId = eventId;
        this.eventName = eventName;
        this.type = type;
        this.timestamp = Timestamp.now();
        this.isRead = false;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}