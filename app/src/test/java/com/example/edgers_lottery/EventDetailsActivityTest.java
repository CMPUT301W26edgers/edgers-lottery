package com.example.edgers_lottery;

//import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
//import org.junit.runner.RunWith;
import java.util.ArrayList;
import static org.junit.Assert.*;

import com.example.edgers_lottery.models.User;
import com.example.edgers_lottery.views.EventDetailsActivity;


public class EventDetailsActivityTest {
    private ArrayList<User> waitingList;
    private User user1;
    private User user2;
    private ArrayList<User> allInvitedUsers;
    private ArrayList<User> declinedUsers;

    @Before
    public void setUp() {
        waitingList = new ArrayList<>();
        allInvitedUsers = new ArrayList<>();
        declinedUsers = new ArrayList<>();

        user1 = new User();
        user1.setId("u1");
        user1.setName("Tony");

        user2 = new User();
        user2.setId("u2");
        user2.setName("Alice");
    }
    private void acceptInvite(User user, ArrayList<User> waitingList, ArrayList<User> invitedList) {
        EventDetailsActivity.addUserToList(user, waitingList);
        EventDetailsActivity.removeUserFromListSafely(user.getId(), invitedList);
    }

    private void declineInvite(User user, ArrayList<User> declinedList, ArrayList<User> invitedList) {
        EventDetailsActivity.addUserToList(user, declinedList);
        EventDetailsActivity.removeUserFromListSafely(user.getId(), invitedList);
    }

    @Test
    public void isUserInList_emptyList_returnsFalse() {
        boolean result = EventDetailsActivity.isUserInList("u1", waitingList);
        assertFalse(result);
    }

    @Test
    public void isUserInList_userExists_returnsTrue() {
        waitingList.add(user1);

        boolean result = EventDetailsActivity.isUserInList("u1", waitingList);

        assertTrue(result);
    }

    @Test
    public void isUserInList_differentUser_returnsFalse() {
        waitingList.add(user2);

        boolean result = EventDetailsActivity.isUserInList("u1", waitingList);

        assertFalse(result);
    }

    @Test
    public void addUserToList_addsUser() {
        EventDetailsActivity.addUserToList(user1, waitingList);

        assertEquals(1, waitingList.size());
        assertEquals("u1", waitingList.get(0).getId());
    }

    @Test
    public void addUserToList_doesNotDuplicateUser() {
        waitingList.add(user1);

        EventDetailsActivity.addUserToList(user1, waitingList);

        assertEquals(1, waitingList.size());
    }

    @Test
    public void removeUserFromListSafely_removesMatchingUser() {
        waitingList.add(user1);
        waitingList.add(user2);

        EventDetailsActivity.removeUserFromListSafely("u1", waitingList);

        assertEquals(1, waitingList.size());
        assertEquals("u2", waitingList.get(0).getId());
    }

    @Test
    public void removeUserFromListSafely_nonexistentUser_doesNothing() {
        waitingList.add(user1);
        waitingList.add(user2);

        EventDetailsActivity.removeUserFromListSafely("u3", waitingList);

        assertEquals(2, waitingList.size());
    }

    @Test
    public void isWaitlistFull_whenBelowCapacity_returnsFalse() {
        waitingList.add(user1);

        boolean result = EventDetailsActivity.isWaitlistFull(3, waitingList);

        assertFalse(result);
    }

    @Test
    public void isWaitlistFull_whenEqualCapacity_returnsTrue() {
        waitingList.add(user1);
        waitingList.add(user2);

        boolean result = EventDetailsActivity.isWaitlistFull(2, waitingList);

        assertTrue(result);
    }

    @Test
    public void isWaitlistFull_whenAboveCapacity_returnsTrue() {
        waitingList.add(user1);
        waitingList.add(user2);

        boolean result = EventDetailsActivity.isWaitlistFull(1, waitingList);

        assertTrue(result);
    }

    @Test
    public void isWaitlistFull_whenListEmpty_returnsFalse() {
        boolean result = EventDetailsActivity.isWaitlistFull(3, waitingList);

        assertFalse(result);
    }
    @Test
    public void acceptInvite_addsUserToWaitingList() {
        allInvitedUsers.add(user1);

        acceptInvite(user1, waitingList, allInvitedUsers);

        assertTrue(EventDetailsActivity.isUserInList("u1", waitingList));
    }

    @Test
    public void acceptInvite_removesUserFromAllInvitedUsers() {
        allInvitedUsers.add(user1);

        acceptInvite(user1, waitingList, allInvitedUsers);

        assertFalse(EventDetailsActivity.isUserInList("u1", allInvitedUsers));
    }

    @Test
    public void acceptInvite_doesNotDuplicateUserInWaitingList() {
        waitingList.add(user1);
        allInvitedUsers.add(user1);

        acceptInvite(user1, waitingList, allInvitedUsers);

        assertEquals(1, waitingList.size());
    }

    @Test
    public void declineInvite_addsUserToDeclinedUsers() {
        allInvitedUsers.add(user1);

        declineInvite(user1, declinedUsers, allInvitedUsers);

        assertTrue(EventDetailsActivity.isUserInList("u1", declinedUsers));
    }

    @Test
    public void declineInvite_removesUserFromAllInvitedUsers() {
        allInvitedUsers.add(user1);

        declineInvite(user1, declinedUsers, allInvitedUsers);

        assertFalse(EventDetailsActivity.isUserInList("u1", allInvitedUsers));
    }

    @Test
    public void declineInvite_doesNotDuplicateUserInDeclinedUsers() {
        declinedUsers.add(user1);
        allInvitedUsers.add(user1);

        declineInvite(user1, declinedUsers, allInvitedUsers);

        assertEquals(1, declinedUsers.size());
    }
}