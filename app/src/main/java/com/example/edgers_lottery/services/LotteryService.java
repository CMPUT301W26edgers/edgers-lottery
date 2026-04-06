package com.example.edgers_lottery.services;

import android.widget.Toast;

import com.example.edgers_lottery.models.Event;
import com.example.edgers_lottery.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
/**
 * Service class for running the event lottery on a waitlist of users.
 */
public class LotteryService {
    // this service should be able to randomly sample from a waitlist of users
    // these functions can be static and make edits to the firebase
    /**
     * Samples the waitlist for a given event, selects users up to the remaining
     * capacity, updates the invited and waiting lists in Firestore, and triggers
     * lottery result notifications. Returns early if the event is full or the
     * waitlist is empty. Used by the organizer to run the lottery.
     *
     * @param eventId the ID of the event to run the lottery for
     * @param result  callback called with a message describing the outcome
     */
    public static void sampleWaitlist(String eventId, LotteryCallback result) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // get the waitlist of users for the event
        if (eventId != null) {
            db.collection("events").document(eventId).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            // list of users on the chosen list
                            ArrayList<User> chosenList = new ArrayList<>();
                            // list of users on the waitlist
                            ArrayList<User> waitList = new ArrayList<>();
                            // list of users invited to the event
                            ArrayList<User> invitedList;
                            // list of confirmed users attending the event
                            ArrayList<User> entrantsList;
                            // list of users not invited to the event
                            ArrayList<User> notInvitedList;
                            // the capacity of the event
                            ArrayList<User> AllinvitedUsers;
                            int eventCapacity;
                            // the remaining capacity of the event if the organizer is rerunning the lottery
                            int remainingCapacity;

                            // get the event object from the document
                            Event event = document.toObject(Event.class);
                            if (event != null) {
                                event.setId(document.getId());
                                // we need to get the number of users on the waitlist and subtract the number of invited users
                                // and the number of confirmed users to get the number of people we can invite to the event remaining on the waitlist
                                waitList = event.getWaitingList() != null
                                        ? event.getWaitingList()
                                        : new ArrayList<>(); // make it empty to prevent error in complete listener
                                invitedList = event.getInvitedUsers() != null
                                        ? event.getInvitedUsers()
                                        : new ArrayList<>(); // make it empty to prevent error in complete listener
                                entrantsList = event.getEntrants() != null
                                        ? event.getEntrants()
                                        : new ArrayList<>(); // make it empty to prevent error in complete listener

                                // get the number of users on the currently in the event
                                eventCapacity = event.getCapacity();
                                remainingCapacity = eventCapacity - invitedList.size() - entrantsList.size();
                                if (remainingCapacity <= 0) {
                                    android.util.Log.d("LotteryService", "Event is already full");
                                    result.onComplete("Event is already full");
                                    return;
                                }
                                if (waitList.isEmpty()) {
                                    android.util.Log.d("LotteryService", "Waitlist is empty, nothing to sample");
                                    result.onComplete("Waitlist is empty, nothing to sample");
                                    return;
                                }
                                // run the lottery on the waitlist

                                Map.Entry<ArrayList<User>,ArrayList<User>> lotteryResult = runLottery(waitList, remainingCapacity);

                                // send the invites to the chosen list
                                chosenList = lotteryResult.getKey();
                                notInvitedList = lotteryResult.getValue();

                                // update the event object with the new lists
                                event.setInvitedUsers(chosenList);
                                event.setWaitingList(notInvitedList);
                                if (event.getAllInvitedUsers() != null){
                                    AllinvitedUsers = event.getAllInvitedUsers();
                                } else {
                                    AllinvitedUsers = new ArrayList<>();
                                }
                                for (User u : chosenList) {
                                    boolean exists = false;
                                    for (User existing : AllinvitedUsers) {
                                        if (existing.getId().equals(u.getId())) {
                                            exists = true;
                                            break;
                                        }
                                    }
                                    if (!exists) {
                                        AllinvitedUsers.add(u);
                                    }
                                }
                                // update the document in the database and hand off final data to notification service
                                final ArrayList<User> finalChosenList = chosenList;
                                final ArrayList<User> finalNotInvitedList = notInvitedList;

                                db.collection("events").document(eventId)
                                        .update("invitedUsers", chosenList,
                                                "waitingList", notInvitedList,
                                                "AllInvitedUsers", AllinvitedUsers )
                                        .addOnSuccessListener(aVoid -> {
                                            android.util.Log.d("LotteryService", "Lottery complete");
                                            // hand off results to NotificationService once DB is confirmed updated
                                            NotificationService.sendLotteryResults(eventId, event.getName(), finalChosenList, finalNotInvitedList);
                                            result.onComplete("Lottery complete");
                                        });


                            }
                        } else {
                            // log failure in logcat
                            android.util.Log.d("LotteryService", "Event not found");
                            result.onComplete("Event not found");
                        }
                    })
                    .addOnFailureListener(e -> {
                            // log failure in logcat
                            android.util.Log.d("LotteryService", "Failed to load event");
                            result.onComplete("Failed to load event");
                    });
        }
    }
    /**
     * Randomly selects users from a sampling list up to the remaining capacity.
     * If capacity exceeds the list size, all users are selected.
     * Used interally within this class.
     *
     * @param samplingList      the list of users to sample from
     * @param remainingCapacity the number of users that can be invited
     * @return a map entry where the key is the list of chosen users
     *         and the value is the list of users not invited
     */
    public static Map.Entry<ArrayList<User>,ArrayList<User>> runLottery(ArrayList<User> samplingList, int remainingCapacity) {
        ArrayList<User> chosenList;
        ArrayList<User> notInvitedList;
        // sample waitlist
        // Take a sample of the waitlist
        // add this check before sampling
        if (remainingCapacity >= samplingList.size()) { // if the remaining capacity is greater than the size of the waitlist
            // just invite everyone on the waitlist
            chosenList = new ArrayList<>(samplingList);
            notInvitedList = new ArrayList<>();
        } else {
            Collections.shuffle(samplingList);
            chosenList = new ArrayList<>(samplingList.subList(0, remainingCapacity));
            notInvitedList = new ArrayList<>(samplingList.subList(remainingCapacity, samplingList.size()));
        }
        return new AbstractMap.SimpleEntry<>(chosenList, notInvitedList);
    }
}
