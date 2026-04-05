package com.example.edgers_lottery;

import static org.junit.Assert.*;

import com.example.edgers_lottery.models.AppNotification;
import com.example.edgers_lottery.services.NotificationService;

import org.junit.Test;

public class NotificationServiceTest {

    // ─── US 01.05.06 — Private Event Invite Notification ──────────────────────

    @Test
    public void testAppNotification_privateInvite_typeIsCorrect() {
        AppNotification n = new AppNotification("user1", "event1", "Secret Event", "PRIVATE_EVENT_INVITE");
        assertEquals("PRIVATE_EVENT_INVITE", n.getType());
    }

    @Test
    public void testAppNotification_privateInvite_defaultsToUnread() {
        AppNotification n = new AppNotification("user1", "event1", "Secret Event", "PRIVATE_EVENT_INVITE");
        assertFalse("Private invite notification should default to unread", n.isRead());
    }

    @Test
    public void testSendPrivateEventInvite_doesNotCrash() {
        try {
            NotificationService.sendPrivateEventInvite("user_abc", "event_xyz", "Secret Event");
        } catch (Exception e) {
            fail("sendPrivateEventInvite threw an exception: " + e.getMessage());
        }
    }
}