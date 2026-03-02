package com.example.edgers_lottery;

import android.net.Uri;

import java.util.ArrayList;

public class Event {
    private String name;
    private String description;
    private String date; // can be changed to a date object
    private String time; // can be changed to a time object
    private String location;
    private String organizer;
    private int capacity;
    private String registrationStart; // can be changed to a date object
    private String registrationEnd; // can be changed to a date object
    private Uri poster; // this will be a URI to the poster image, we can change this later
    private ArrayList<Entrant> waitingList;
    private ArrayList<Entrant> entrants;
    private boolean enforeLocation;
//    public Lottery lottery;
}
