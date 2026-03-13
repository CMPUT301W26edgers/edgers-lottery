package com.example.edgers_lottery;

//import androidx.test.ext.junit.runners.AndroidJUnit4;

import static com.example.edgers_lottery.EventDetailsActivity.addUserToList;

import org.junit.Before;
import org.junit.Test;
//import org.junit.runner.RunWith;
import java.util.ArrayList;
import static org.junit.Assert.*;



public class EventDetailsActivityTest {
    private ArrayList<User> waitingList;
    private User user1;
    private User user2;

    @Before
    public void setUp() {
        waitingList = new ArrayList<>();

        user1 = new User();
        user1.setId("u1");
        user1.setName("Tony");

        user2 = new User();
        user2.setId("u2");
        user2.setName("Alice");
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
}