package com.example.edgers_lottery.models;

import android.net.Uri;
import java.util.ArrayList;

/**
 * Model class representing a lottery event.
 * Stores all event details including scheduling, capacity, registration windows,
 * participant lists, and location enforcement.
 * Used throughout the app and mapped to Firestore documents in the {@code events} collection.
 */
public class Event {

    /** Unique Firestore document ID for this event. */
    private String id;

    /** Display name of the event. */
    private String name;

    /** Description of the event. */
    private String description;

    /** Date of the event in {@code yyyy-MM-dd} format. */
    private String date;

    /** Time of the event. */
    private String time;

    /** Location of the event. */
    private String location;

    /** Firestore document ID of the organizer user. */
    private String organizerId;

    /** Maximum number of entrants allowed for this event. */
    private int capacity;

    /** Maximum number of users allowed on the waitlist. */
    private int waitlistCapacity;

    /** Entry price for the event (stored as a String to preserve formatting). */
    private String price;

    /** Base64-encoded JPEG string of the event poster image. */
    private String image;

    /** Start of the registration window in {@code yyyy-MM-dd} format. */
    private String registrationStart;

    /** End of the registration window in {@code yyyy-MM-dd} format. */
    private String registrationEnd;

    /** URI pointing to the event poster image (local only, not persisted to Firestore). */
    private Uri poster;

    /** List of users currently on the waiting list for this event. */
    private ArrayList<User> waitingList;

    /** List of users selected by the lottery to be invited to this event. */
    private ArrayList<User> invitedUsers;

    /** List of users who have confirmed their participation in this event. */
    private ArrayList<User> entrants;

    /** Whether entrants are required to be at the event location to participate. */
    private boolean enforceLocation;

    /** Whether the event is publicly visible. */
    private boolean ispublic;

    /**
     * Default no-argument constructor required by Firestore for deserialization.
     */
    public Event() {}

    /**
     * Constructs a new Event with all core fields.
     *
     * @param name              the display name of the event
     * @param description       a description of the event
     * @param date              the date of the event in {@code yyyy-MM-dd} format
     * @param time              the time of the event
     * @param location          the location of the event
     * @param organizer         the {@link User} organizing the event (not stored directly)
     * @param capacity          the maximum number of entrants allowed
     * @param registrationStart the start of the registration window
     * @param registrationEnd   the end of the registration window
     */
    public Event(String name, String description, String date, String time, String location,
                 User organizer, int capacity, String registrationStart, String registrationEnd) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.time = time;
        this.location = location;
        this.capacity = capacity;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        this.waitingList = new ArrayList<>();
        this.entrants = new ArrayList<>();
    }

    // ─── ID ──────────────────────────────────────────────────────────────────

    /** @return the unique Firestore document ID of this event */
    public String getId() { return id; }

    /** @param id the Firestore document ID to assign to this event */
    public void setId(String id) { this.id = id; }

    // ─── Name ────────────────────────────────────────────────────────────────

    /** @return the display name of the event */
    public String getName() { return name; }

    /** @param name the display name to set */
    public void setName(String name) { this.name = name; }

    // ─── Description ─────────────────────────────────────────────────────────

    /** @return the description of the event */
    public String getDescription() { return description; }

    /** @param description the description to set */
    public void setDescription(String description) { this.description = description; }

    // ─── Date ────────────────────────────────────────────────────────────────

    /** @return the date of the event in {@code yyyy-MM-dd} format */
    public String getDate() { return date; }

    /** @param date the date to set in {@code yyyy-MM-dd} format */
    public void setDate(String date) { this.date = date; }

    // ─── Time ────────────────────────────────────────────────────────────────

    /** @return the time of the event */
    public String getTime() { return time; }

    /** @param time the time to set */
    public void setTime(String time) { this.time = time; }

    // ─── Location ────────────────────────────────────────────────────────────

    /** @return the location of the event */
    public String getLocation() { return location; }

    /** @param location the location to set */
    public void setLocation(String location) { this.location = location; }

    // ─── Capacity ────────────────────────────────────────────────────────────

    /** @return the maximum number of entrants allowed */
    public int getCapacity() { return capacity; }

    /** @param capacity the maximum entrant capacity to set */
    public void setCapacity(int capacity) { this.capacity = capacity; }

    // ─── Waitlist Capacity ───────────────────────────────────────────────────

    /** @return the maximum number of users allowed on the waitlist */
    public int getWaitlistCapacity() { return waitlistCapacity; }

    /** @param waitlistCapacity the maximum waitlist capacity to set */
    public void setWaitlistCapacity(int waitlistCapacity) { this.waitlistCapacity = waitlistCapacity; }

    // ─── Price ───────────────────────────────────────────────────────────────

    /** @return the entry price of the event */
    public String getPrice() { return price; }

    /** @param price the entry price to set */
    public void setPrice(String price) { this.price = price; }

    // ─── Image ───────────────────────────────────────────────────────────────

    /** @return the Base64-encoded JPEG string of the event poster, or {@code null} if none */
    public String getImage() { return image; }

    /** @param image the Base64-encoded JPEG string to set */
    public void setImage(String image) { this.image = image; }

    // ─── Registration Window ─────────────────────────────────────────────────

    /** @return the registration window start date in {@code yyyy-MM-dd} format */
    public String getRegistrationStart() { return registrationStart; }

    /** @param registrationStart the registration start date to set */
    public void setRegistrationStart(String registrationStart) { this.registrationStart = registrationStart; }

    /** @return the registration window end date in {@code yyyy-MM-dd} format */
    public String getRegistrationEnd() { return registrationEnd; }

    /** @param registrationEnd the registration end date to set */
    public void setRegistrationEnd(String registrationEnd) { this.registrationEnd = registrationEnd; }

    // ─── Poster (local URI, not persisted) ───────────────────────────────────

    /** @return the URI of the event poster image */
    public Uri getPoster() { return poster; }

    /** @param poster the URI of the event poster image to set */
    public void setPoster(Uri poster) { this.poster = poster; }

    // ─── Participant Lists ────────────────────────────────────────────────────

    /** @return the list of users on the waiting list */
    public ArrayList<User> getWaitingList() { return waitingList; }

    /** @param waitingList the waiting list to set */
    public void setWaitingList(ArrayList<User> waitingList) { this.waitingList = waitingList; }

    /** @return the list of users invited after the lottery draw */
    public ArrayList<User> getInvitedUsers() { return invitedUsers; }

    /** @param invitedUsers the invited users list to set */
    public void setInvitedUsers(ArrayList<User> invitedUsers) { this.invitedUsers = invitedUsers; }

    /** @return the list of confirmed event entrants */
    public ArrayList<User> getEntrants() { return entrants; }

    /** @param entrants the entrants list to set */
    public void setEntrants(ArrayList<User> entrants) { this.entrants = entrants; }

    // ─── Flags ───────────────────────────────────────────────────────────────

    /** @return true if entrants are required to be at the event location */
    public boolean isEnforceLocation() { return enforceLocation; }

    /** @param enforceLocation true to require location enforcement, false otherwise */
    public void setEnforceLocation(boolean enforceLocation) { this.enforceLocation = enforceLocation; }

    /** @return true if the event is publicly visible */
    public boolean isIspublic() { return ispublic; }

    /** @param ispublic true to make the event public, false to keep it private */
    public void setIspublic(boolean ispublic) { this.ispublic = ispublic; }

    // ─── Organizer ───────────────────────────────────────────────────────────

    /** @return the Firestore document ID of the organizer user */
    public String getOrganizerId() { return organizerId; }

    /** @param organizerId the organizer's Firestore document ID to set */
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    // ─── Firestore Serialization Helper ──────────────────────────────────────

    /**
     * Converts this Event into a {@link java.util.Map} suitable for writing to Firestore.
     * Only primitive-compatible fields are included; local-only fields like {@link #poster}
     * and participant lists are excluded.
     *
     * @return a map of field names to values ready for {@code DocumentReference.set()} or {@code .update()}
     */
    public java.util.Map<String, Object> toFirestoreMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("id",               getId());
        map.put("name",             getName());
        map.put("date",             getDate());
        map.put("registrationEnd",  getRegistrationEnd());
        map.put("price",            getPrice());
        map.put("description",      getDescription());
        map.put("capacity",         getCapacity());
        map.put("waitlistCapacity", getWaitlistCapacity());
        map.put("enforceLocation",  isEnforceLocation());
        map.put("ispublic",         isIspublic());
        map.put("image",            getImage()); // null-safe; Firestore accepts null
        return map;
    }
}