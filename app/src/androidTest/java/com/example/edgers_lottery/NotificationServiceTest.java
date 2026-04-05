package com.example.edgers_lottery;

import static org.junit.Assert.*;

import com.example.edgers_lottery.models.AppNotification;
import com.example.edgers_lottery.models.User;
import com.example.edgers_lottery.services.NotificationService;

import org.junit.Test;

import java.util.ArrayList;

public class NotificationServiceTest {

    //Helpers
    private User createMockUser(String id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        return user;
    }

//    // ─── US 01.05.06 — Private Event Invite Notification ──────────────────────
//
//    @Test
//    public void testAppNotification_privateInvite_typeIsCorrect() {
//        AppNotification n = new AppNotification("user1", "event1", "Secret Event", "PRIVATE_EVENT_INVITE");
//        assertEquals("PRIVATE_EVENT_INVITE", n.getType());
//    }
//
//    @Test
//    public void testAppNotification_privateInvite_defaultsToUnread() {
//        AppNotification n = new AppNotification("user1", "event1", "Secret Event", "PRIVATE_EVENT_INVITE");
//        assertFalse("Private invite notification should default to unread", n.isRead());
//    }
//
//    @Test
//    public void testSendPrivateEventInvite_doesNotCrash() {
//        try {
//            NotificationService.sendPrivateEventInvite("user_abc", "event_xyz", "Secret Event");
//        } catch (Exception e) {
//            fail("sendPrivateEventInvite threw an exception: " + e.getMessage());
//        }
//    }
//
//    // ─── US 01.04.02 — Not Selected Notification ──────────────────────────────
//
//    @Test
//    public void testAppNotification_notSelected_typeIsCorrect() {
//        AppNotification n = new AppNotification("user1", "event1", "Test Event", "NOT_SELECTED");
//        assertEquals("NOT_SELECTED", n.getType());
//    }
//
//    @Test
//    public void testAppNotification_notSelected_defaultsToUnread() {
//        AppNotification n = new AppNotification("user1", "event1", "Test Event", "NOT_SELECTED");
//        assertFalse("Not selected notification should default to unread", n.isRead());
//    }
//
//    @Test
//    public void testSendLotteryResults_notSelectedList_doesNotCrash() {
//        ArrayList<User> notSelected = new ArrayList<>();
//        notSelected.add(createMockUser("user_1", "Alice"));
//        notSelected.add(createMockUser("user_2", "Bob"));
//        try {
//            NotificationService.sendLotteryResults(
//                    "event123",
//                    "Test Event",
//                    new ArrayList<>(),
//                    notSelected
//            );
//        } catch (Exception e) {
//            fail("sendLotteryResults threw an exception with notSelected users: " + e.getMessage());
//        }
//    }
//    // ─── US 02.07.02 — Send Notifications to Selected Entrants ────────────────
//
//    @Test
//    public void testAppNotification_selected_typeIsCorrect() {
//        AppNotification n = new AppNotification("user1", "event1", "Test Event", "SELECTED");
//        assertEquals("SELECTED", n.getType());
//    }
//
//    @Test
//    public void testAppNotification_selected_defaultsToUnread() {
//        AppNotification n = new AppNotification("user1", "event1", "Test Event", "SELECTED");
//        assertFalse("Selected notification should default to unread", n.isRead());
//    }
//
//    @Test
//    public void testSendLotteryResults_selectedList_doesNotCrash() {
//        ArrayList<User> selected = new ArrayList<>();
//        selected.add(createMockUser("user_1", "Alice"));
//        selected.add(createMockUser("user_2", "Bob"));
//        try {
//            NotificationService.sendLotteryResults(
//                    "event123",
//                    "Test Event",
//                    selected,
//                    new ArrayList<>()
//            );
//        } catch (Exception e) {
//            fail("sendLotteryResults threw an exception with selected users: " + e.getMessage());
//        }
//    }


    // ─── US 02.07.03 — Cancelled Entrant Notification ─────────────────────────

    @Test
    public void testAppNotification_cancelled_typeIsCorrect() {
        AppNotification n = new AppNotification("user1", "event1", "Test Event", "CANCELLED");
        assertEquals("CANCELLED", n.getType());
    }

    @Test
    public void testAppNotification_cancelled_defaultsToUnread() {
        AppNotification n = new AppNotification("user1", "event1", "Test Event", "CANCELLED");
        assertFalse("Cancelled notification should default to unread", n.isRead());
    }

    @Test
    public void testSendCancelledNotification_doesNotCrash() {
        try {
            NotificationService.sendCancelledNotification(
                    "user_abc",
                    "event_xyz",
                    "Test Event"
            );
        } catch (Exception e) {
            fail("sendCancelledNotification threw an exception: " + e.getMessage());
        }
    }
}