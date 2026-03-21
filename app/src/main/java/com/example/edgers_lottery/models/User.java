package com.example.edgers_lottery.models;

import java.io.Serializable;

/**
 * Model class representing a user account in the Edgers Lottery system.
 * Supports three roles: ENTRANT, ORGANIZER, and ADMIN.
 * Implements {@link Serializable} to allow passing instances between activities.
 */
public class User implements Serializable {

    /** Unique Firestore document ID for this user. */
    private String id;

    /** Full name of the user. */
    private String name;

    /** Email address of the user. */
    private String email;

    /** Phone number of the user. */
    private String phone;

    /** Optional bio or description provided by the user. */
    private String description;

    /** Optional location provided by the user. */
    private String location;

    /** Optional display username chosen by the user. */
    private String username;

    /** Role assigned to this user, corresponding to {@link Role}. */
    private String role;

    /**
     * Enum representing the possible roles a user can hold.
     */
    public static enum Role {
        ENTRANT,
        ORGANIZER,
        ADMIN
    }

    /**
     * Required no-argument constructor for Firestore deserialization.
     */
    public User() {
    }

    /**
     * Constructs a new User with the required core fields.
     *
     * @param id    the unique Firestore document ID
     * @param name  the full name of the user
     * @param email the email address of the user
     * @param role  the role assigned to the user
     */
    public User(String id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    /**
     * @return the user's full name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the full name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the user's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the user's phone number, or an empty string if not set
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone the phone number to set, or null to store an empty string
     */
    public void setPhone(String phone) {
        this.phone = phone != null ? phone : "";
    }

    /**
     * @return the user's description, or an empty string if not set
     */
    public String getDescription() {
        return description != null ? description : "";
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the user's location, or an empty string if not set
     */
    public String getLocation() {
        return location != null ? location : "";
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the user's display username, or {@code "no username"} if not set
     */
    public String getUsername() {
        return username != null ? username : "no username";
    }

    /**
     * @param username the display username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the user's Firestore document ID
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the Firestore document ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the user's role as a string, e.g. {@code "ENTRANT"}, {@code "ORGANIZER"}, or {@code "ADMIN"}
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(String role) {
        this.role = role;
    }
}