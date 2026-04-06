package com.example.edgers_lottery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

import com.example.edgers_lottery.models.core.WaitlistUser;

/**
 * Unit tests for the pure logic used in EventEntrantOrganizer.
 * These tests exercise list operations and data-mapping directly,
 * with no Android or Firestore dependencies.
 */
@RunWith(JUnit4.class)
public class EventEntrantOrganizerTest {

    // -----------------------------------------------------------------------
    // Helpers — mirrors the logic in loadEntrants() and removeFromEntrants()
    // -----------------------------------------------------------------------

    /** Converts a raw Firestore-style list of Maps into WaitlistUser objects. */
    private List<WaitlistUser> mapToUsers(List<Object> raw) {
        List<WaitlistUser> result = new ArrayList<>();
        if (raw == null) return result;
        for (Object item : raw) {
            if (item instanceof Map) {
                Map<String, Object> userMap = (Map<String, Object>) item;
                String userId   = (String) userMap.get("id");
                String name     = (String) userMap.get("name");
                String imageUrl = (String) userMap.get("profileImage");
                result.add(new WaitlistUser(userId, name, imageUrl));
            }
        }
        return result;
    }

    /** Removes a user from a raw list by userId, mirroring removeFromEntrants(). */
    private List<Object> removeUser(List<Object> raw, String targetUserId) {
        List<Object> copy = new ArrayList<>(raw);
        for (int i = 0; i < copy.size(); i++) {
            Object item = copy.get(i);
            if (item instanceof Map) {
                if (targetUserId.equals(((Map<?, ?>) item).get("id"))) {
                    copy.remove(i);
                    break;
                }
            }
        }
        return copy;
    }

    // -----------------------------------------------------------------------
    // Test data
    // -----------------------------------------------------------------------

    private List<Object> sampleRawList;

    @Before
    public void setUp() {
        sampleRawList = new ArrayList<>();

        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", "u1");
        user1.put("name", "Alice");
        user1.put("profileImage", "https://example.com/alice.jpg");

        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", "u2");
        user2.put("name", "Bob");
        user2.put("profileImage", null);

        Map<String, Object> user3 = new HashMap<>();
        user3.put("id", "u3");
        user3.put("name", "Carol");
        user3.put("profileImage", "https://example.com/carol.jpg");

        sampleRawList.add(user1);
        sampleRawList.add(user2);
        sampleRawList.add(user3);
    }

    // -----------------------------------------------------------------------
    // mapToUsers() tests
    // -----------------------------------------------------------------------

    @Test
    public void mapToUsers_correctCount() {
        List<WaitlistUser> users = mapToUsers(sampleRawList);
        assertEquals(3, users.size());
    }

    @Test
    public void mapToUsers_fieldsMapCorrectly() {
        List<WaitlistUser> users = mapToUsers(sampleRawList);
        WaitlistUser alice = users.get(0);
        assertEquals("u1", alice.getUserId());
        assertEquals("Alice", alice.getName());
        assertEquals("https://example.com/alice.jpg", alice.getProfileImage());
    }

    @Test
    public void mapToUsers_nullProfileImageAllowed() {
        List<WaitlistUser> users = mapToUsers(sampleRawList);
        WaitlistUser bob = users.get(1);
        assertEquals("u2", bob.getUserId());
        assertNull(bob.getProfileImage());
    }

    @Test
    public void mapToUsers_nullInputReturnsEmptyList() {
        List<WaitlistUser> users = mapToUsers(null);
        assertNotNull(users);
        assertEquals(0, users.size());
    }

    @Test
    public void mapToUsers_emptyInputReturnsEmptyList() {
        List<WaitlistUser> users = mapToUsers(new ArrayList<>());
        assertEquals(0, users.size());
    }

    @Test
    public void mapToUsers_skipsNonMapEntries() {
        List<Object> mixed = new ArrayList<>(sampleRawList);
        mixed.add("not a map");
        mixed.add(42);
        List<WaitlistUser> users = mapToUsers(mixed);
        // Should still only produce 3 valid users
        assertEquals(3, users.size());
    }

    // -----------------------------------------------------------------------
    // removeUser() tests
    // -----------------------------------------------------------------------

    @Test
    public void removeUser_reducesCountByOne() {
        List<Object> result = removeUser(sampleRawList, "u2");
        assertEquals(2, result.size());
    }

    @Test
    public void removeUser_correctUserRemoved() {
        List<Object> result = removeUser(sampleRawList, "u2");
        for (Object item : result) {
            Map<?, ?> map = (Map<?, ?>) item;
            assertNotEquals("u2", map.get("id"));
        }
    }

    @Test
    public void removeUser_doesNotMutateOriginalList() {
        List<Object> original = new ArrayList<>(sampleRawList);
        removeUser(sampleRawList, "u1");
        assertEquals(original.size(), sampleRawList.size());
    }

    @Test
    public void removeUser_nonExistentIdLeavesListUnchanged() {
        List<Object> result = removeUser(sampleRawList, "u999");
        assertEquals(3, result.size());
    }

    @Test
    public void removeUser_onlyRemovesFirstMatch() {
        // Add a duplicate user id
        Map<String, Object> duplicate = new HashMap<>();
        duplicate.put("id", "u1");
        duplicate.put("name", "Alice Duplicate");
        duplicate.put("profileImage", null);
        sampleRawList.add(duplicate);

        List<Object> result = removeUser(sampleRawList, "u1");
        // Should remove exactly one, leaving 3
        assertEquals(3, result.size());
    }

    @Test
    public void removeUser_emptyListReturnsEmptyList() {
        List<Object> result = removeUser(new ArrayList<>(), "u1");
        assertEquals(0, result.size());
    }

    // -----------------------------------------------------------------------
    // WaitlistUser model tests
    // -----------------------------------------------------------------------

    @Test
    public void waitlistUser_gettersReturnCorrectValues() {
        WaitlistUser user = new WaitlistUser("u10", "Dave", "https://example.com/dave.jpg");
        assertEquals("u10", user.getUserId());
        assertEquals("Dave", user.getName());
        assertEquals("https://example.com/dave.jpg", user.getProfileImage());
    }

    @Test
    public void waitlistUser_nullFieldsAllowed() {
        WaitlistUser user = new WaitlistUser(null, null, null);
        assertNull(user.getUserId());
        assertNull(user.getName());
        assertNull(user.getProfileImage());
    }

    // -----------------------------------------------------------------------
    // Entrant count label tests
    // -----------------------------------------------------------------------

    @Test
    public void entrantCountLabel_formatsCorrectly() {
        List<WaitlistUser> users = mapToUsers(sampleRawList);
        String label = "Entrants for Event: " + users.size();
        assertEquals("Entrants for Event: 3", label);
    }

    @Test
    public void entrantCountLabel_zeroEntrants() {
        String label = "Entrants for Event: " + 0;
        assertEquals("Entrants for Event: 0", label);
    }
    // -----------------------------------------------------------------------
// cancelUser (AllInvited → Declined) logic tests
// -----------------------------------------------------------------------

    /**
     * Simulates cancelling a user:
     * - removes from AllInvitedUsers
     * - adds to declinedUsers
     */
    private void cancelUser(
            List<Map<String, Object>> allInvited,
            List<Map<String, Object>> declined,
            String targetUserId
    ) {
        Map<String, Object> movedUser = null;

        for (int i = 0; i < allInvited.size(); i++) {
            Map<String, Object> user = allInvited.get(i);
            if (targetUserId.equals(user.get("id"))) {
                movedUser = user;
                allInvited.remove(i);
                break;
            }
        }

        if (movedUser == null) return;

        boolean alreadyDeclined = false;
        for (Map<String, Object> d : declined) {
            if (targetUserId.equals(d.get("id"))) {
                alreadyDeclined = true;
                break;
            }
        }

        if (!alreadyDeclined) {
            declined.add(movedUser);
        }
    }

    @Test
    public void cancelUser_movesUserCorrectly() {
        List<Map<String, Object>> allInvited = new ArrayList<>();
        List<Map<String, Object>> declined = new ArrayList<>();

        Map<String, Object> user = new HashMap<>();
        user.put("id", "u1");
        user.put("name", "Alice");

        allInvited.add(user);

        cancelUser(allInvited, declined, "u1");

        // Removed from invited
        assertEquals(0, allInvited.size());

        // Added to declined
        assertEquals(1, declined.size());
        assertEquals("u1", declined.get(0).get("id"));
    }

    @Test
    public void cancelUser_doesNotDuplicateInDeclined() {
        List<Map<String, Object>> allInvited = new ArrayList<>();
        List<Map<String, Object>> declined = new ArrayList<>();

        Map<String, Object> user = new HashMap<>();
        user.put("id", "u1");

        allInvited.add(user);
        declined.add(user); // already declined

        cancelUser(allInvited, declined, "u1");

        // Should still only have 1 declined
        assertEquals(1, declined.size());
    }

    @Test
    public void cancelUser_userNotFound_doesNothing() {
        List<Map<String, Object>> allInvited = new ArrayList<>();
        List<Map<String, Object>> declined = new ArrayList<>();

        cancelUser(allInvited, declined, "u999");

        assertEquals(0, allInvited.size());
        assertEquals(0, declined.size());
    }
}