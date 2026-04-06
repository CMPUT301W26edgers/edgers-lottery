package com.example.edgers_lottery.models.core;

import com.example.edgers_lottery.models.adapters.WaitlistAdapter;

/**
 * Model class representing a user entry on an event's waitlist.
 * Holds display information used by {@link WaitlistAdapter}.
 */
public class WaitlistUser {

    /** Unique Firestore user ID for this waitlist entry. */
    private String userId;

    /** Display name of the waitlisted user. */
    private String name;

    /** Base64-encoded profile image of the waitlisted user, or null if not set. */
    private String profileImage;

    /**
     * Constructs a new WaitlistUser with all required fields.
     *
     * @param userId       the unique Firestore user ID
     * @param name         the display name of the user
     * @param profileImage the Base64-encoded profile image, or null if unavailable
     */
    public WaitlistUser(String userId, String name, String profileImage) {
        this.userId = userId;
        this.name = name;
        this.profileImage = profileImage;
    }

    /**
     * @return the unique Firestore user ID
     */
    public String getUserId() { return userId; }

    /**
     * @return the display name of the waitlisted user
     */
    public String getName() { return name; }

    /**
     * @return the Base64-encoded profile image, or null if not set
     */
    public String getProfileImage() { return profileImage; }
}