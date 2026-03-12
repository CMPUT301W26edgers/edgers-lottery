package com.example.edgers_lottery;

import android.net.Uri;

import java.util.ArrayList;

public class Event {
    private String id; // events need unique IDs made by
    private String name;
    private String description;
    private String date; // can be changed to a date object
    private String time; // can be changed to a time object
    private String location;
//    private User organizer; // store organizer as a User object
    // this actaully cannot be a User because firebase does not know how to do stuff with that
    private String organizerId;
    private int capacity;
    private String registrationStart; // can be changed to a date object
    private String registrationEnd; // can be changed to a date object
    private Uri poster; // this will be a URI to the poster image, we can change this later
    private ArrayList<User> waitingList;
    private ArrayList<User> invitedUsers; // after lottery, keep selected users here
    private ArrayList<User> entrants;
    private boolean enforceLocation;
//    public Lottery lottery;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

//    public User getOrganizer() {
//        return organizer;
//    }
//
//    public void setOrganizer(User organizer) {
//        this.organizer = organizer;
//    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getRegistrationStart() {
        return registrationStart;
    }

    public void setRegistrationStart(String registrationStart) {
        this.registrationStart = registrationStart;
    }

    public String getRegistrationEnd() {
        return registrationEnd;
    }

    public void setRegistrationEnd(String registrationEnd) {
        this.registrationEnd = registrationEnd;
    }

    public Uri getPoster() {
        return poster;
    }

    public void setPoster(Uri poster) {
        this.poster = poster;
    }

    public ArrayList<User> getWaitingList() {
        return waitingList;
    }

    public void setWaitingList(ArrayList<User> waitingList) {
        this.waitingList = waitingList;
    }

    public ArrayList<User> getInvitedUsers() {
        return invitedUsers;
    }

    public void setInvitedUsers(ArrayList<User> invitedUsers) {
        this.invitedUsers = invitedUsers;
    }

    public ArrayList<User> getEntrants() {
        return entrants;
    }

    public void setEntrants(ArrayList<User> entrants) {
        this.entrants = entrants;
    }

    public boolean isEnforceLocation() {
        return enforceLocation;
    }

    public void setEnforceLocation(boolean enforceLocation) {
        this.enforceLocation = enforceLocation;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public Event(){
        // set defaults like the current day or location or something
    }
    public Event(String name, String description, String date, String time, String location, User organizer, int capacity, String registrationStart, String registrationEnd){
        this.name = name;
        this.description = description;
        this.date = date;
        this.time = time;
        this.location = location;
//        this.organizer = organizer;
        this.capacity = capacity;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        this.waitingList = new ArrayList<>();
        this.entrants = new ArrayList<>();
    }
}
