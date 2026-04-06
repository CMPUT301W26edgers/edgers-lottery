package com.example.edgers_lottery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;

import static org.junit.Assert.*;

import com.example.edgers_lottery.models.core.Event;
import com.example.edgers_lottery.models.core.User;

/**
 * Unit tests for the core filtering logic used in EventHistoryActivity.
 */
@RunWith(JUnit4.class)
public class EventHistoryActivityTest {

    private User testUser;
    private User otherUser;

    @Before
    public void setUp() {
        testUser = new User();
        testUser.setId("user_123");

        otherUser = new User();
        otherUser.setId("user_456");
    }

    @Test
    public void userInWaitingList_returnsTrue() {
        ArrayList<User> waitingList = new ArrayList<>();
        waitingList.add(testUser);

        Event event = new Event();
        event.setWaitingList(waitingList);
        event.setInvitedUsers(new ArrayList<>());

        assertTrue(isUserInEvent(event, testUser.getId()));
    }

    @Test
    public void userInInvitedList_returnsTrue() {
        ArrayList<User> invitedUsers = new ArrayList<>();
        invitedUsers.add(testUser);

        Event event = new Event();
        event.setWaitingList(new ArrayList<>());
        event.setInvitedUsers(invitedUsers);

        assertTrue(isUserInEvent(event, testUser.getId()));
    }

    @Test
    public void userNotInAnyList_returnsFalse() {
        ArrayList<User> waitingList = new ArrayList<>();
        waitingList.add(otherUser);

        Event event = new Event();
        event.setWaitingList(waitingList);
        event.setInvitedUsers(new ArrayList<>());

        assertFalse(isUserInEvent(event, testUser.getId()));
    }

    @Test
    public void nullLists_returnsFalse() {
        Event event = new Event();
        event.setWaitingList(null);
        event.setInvitedUsers(null);

        assertFalse(isUserInEvent(event, testUser.getId()));
    }

    // Mirrors the private logic in EventHistoryActivity
    private boolean isUserInEvent(Event event, String currentUserId) {
        return checkListForUser(event.getWaitingList(), currentUserId)
                || checkListForUser(event.getInvitedUsers(), currentUserId);
    }

    private boolean checkListForUser(ArrayList<User> list, String currentUserId) {
        if (list == null) return false;
        for (User u : list) {
            if (u.getId() != null && u.getId().equals(currentUserId)) return true;
        }
        return false;
    }
}