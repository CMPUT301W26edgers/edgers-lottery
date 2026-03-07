package com.example.edgers_lottery;

import android.net.Uri;

import java.util.ArrayList;

public class Event {
    private String name;
    private String description;
    private String date; // can be changed to a date object
    private String time; // can be changed to a time object
    private String location;
    private Organizer organizer;
    private String organizerId;
    private int capacity;
    private String registrationStart; // can be changed to a date object
    private String registrationEnd; // can be changed to a date object
    private Uri poster; // this will be a URI to the poster image, we can change this later
    private ArrayList<Entrant> waitingList;
    private ArrayList<Entrant> entrants;
    private boolean enforeLocation;
//    public Lottery lottery;

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

    public Organizer getOrganizer() {
        return organizer;
    }

    public void setOrganizer(Organizer organizer) {
        this.organizer = organizer;
    }

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

    public ArrayList<Entrant> getWaitingList() {
        return waitingList;
    }

    public void setWaitingList(ArrayList<Entrant> waitingList) {
        this.waitingList = waitingList;
    }

    public ArrayList<Entrant> getEntrants() {
        return entrants;
    }

    public void setEntrants(ArrayList<Entrant> entrants) {
        this.entrants = entrants;
    }

    public boolean isEnforeLocation() {
        return enforeLocation;
    }

    public void setEnforeLocation(boolean enforeLocation) {
        this.enforeLocation = enforeLocation;
    }
}
