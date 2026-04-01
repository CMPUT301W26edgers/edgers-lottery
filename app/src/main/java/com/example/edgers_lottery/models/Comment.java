package com.example.edgers_lottery.models;

/**
 * Represents a comment made by a user on an event.
 */
public class Comment {

    private String id;
    private String userID;
    private String eventID;
    private String commentText;
    private String timestamp;

    /**
     * @return the comment's unique ID
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the unique ID to set for this comment
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the ID of the user who made the comment
     */
    public String getUserID() {
        return userID;
    }

    /**
     * @param userID the user ID to associate with this comment
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * @return the ID of the event this comment belongs to
     */
    public String getEventID() {
        return eventID;
    }

    /**
     * @param eventID the event ID to associate with this comment
     */
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    /**
     * @return the text content of the comment
     */
    public String getCommentText() {
        return commentText;
    }

    /**
     * @param commentText the text to set for this comment
     */
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    /**
     * @return the timestamp of when the comment was made
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set for this comment
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}