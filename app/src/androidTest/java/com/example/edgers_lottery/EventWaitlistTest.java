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

/**
 * Unit tests for the pure logic used in EventWaitlistTab.
 * Mirrors loadWaitlist() and removeFromWaitlist() without Android or Firestore dependencies.
 */
@RunWith(JUnit4.class)
public class EventWaitlistTest {

    // -----------------------------------------------------------------------
    // Helpers — mirrors loadWaitlist() and removeFromWaitlist() logic
    // -----------------------------------------------------------------------

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
        user1.put("id", "w1");
        user1.put("name", "Eve");
        user1.put("profileImage", "https://example.com/eve.jpg");

        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", "w2");
        user2.put("name", "Frank");
        user2.put("profileImage", null);

        Map<String, Object> user3 = new HashMap<>();
        user3.put("id", "w3");
        user3.put("name", "Grace");
        user3.put("profileImage", "https://example.com/grace.jpg");

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
        WaitlistUser eve = users.get(0);
        assertEquals("w1", eve.getUserId());
        assertEquals("Eve", eve.getName());
        assertEquals("https://example.com/eve.jpg", eve.getProfileImage());
    }

    @Test
    public void mapToUsers_nullProfileImageAllowed() {
        List<WaitlistUser> users = mapToUsers(sampleRawList);
        WaitlistUser frank = users.get(1);
        assertEquals("w2", frank.getUserId());
        assertNull(frank.getProfileImage());
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
        mixed.add(99);
        List<WaitlistUser> users = mapToUsers(mixed);
        assertEquals(3, users.size());
    }

    // -----------------------------------------------------------------------
    // removeUser() tests
    // -----------------------------------------------------------------------

    @Test
    public void removeUser_reducesCountByOne() {
        List<Object> result = removeUser(sampleRawList, "w2");
        assertEquals(2, result.size());
    }

    @Test
    public void removeUser_correctUserRemoved() {
        List<Object> result = removeUser(sampleRawList, "w2");
        for (Object item : result) {
            Map<?, ?> map = (Map<?, ?>) item;
            assertNotEquals("w2", map.get("id"));
        }
    }

    @Test
    public void removeUser_doesNotMutateOriginalList() {
        int originalSize = sampleRawList.size();
        removeUser(sampleRawList, "w1");
        assertEquals(originalSize, sampleRawList.size());
    }

    @Test
    public void removeUser_nonExistentIdLeavesListUnchanged() {
        List<Object> result = removeUser(sampleRawList, "w999");
        assertEquals(3, result.size());
    }

    @Test
    public void removeUser_onlyRemovesFirstMatch() {
        Map<String, Object> duplicate = new HashMap<>();
        duplicate.put("id", "w1");
        duplicate.put("name", "Eve Duplicate");
        duplicate.put("profileImage", null);
        sampleRawList.add(duplicate);

        List<Object> result = removeUser(sampleRawList, "w1");
        assertEquals(3, result.size());
    }

    @Test
    public void removeUser_emptyListReturnsEmptyList() {
        List<Object> result = removeUser(new ArrayList<>(), "w1");
        assertEquals(0, result.size());
    }

    @Test
    public void removeUser_lastUserCanBeRemoved() {
        List<Object> single = new ArrayList<>();
        Map<String, Object> only = new HashMap<>();
        only.put("id", "w1");
        only.put("name", "Solo");
        only.put("profileImage", null);
        single.add(only);

        List<Object> result = removeUser(single, "w1");
        assertEquals(0, result.size());
    }

    // -----------------------------------------------------------------------
    // Waitlist count label tests
    // -----------------------------------------------------------------------

    @Test
    public void waitlistCountLabel_formatsCorrectly() {
        List<WaitlistUser> users = mapToUsers(sampleRawList);
        String label = "Waitlisters for Event: " + users.size();
        assertEquals("Waitlisters for Event: 3", label);
    }

    @Test
    public void waitlistCountLabel_zeroWaitlisters() {
        String label = "Waitlisters for Event: " + 0;
        assertEquals("Waitlisters for Event: 0", label);
    }

    @Test
    public void waitlistCountLabel_updatesAfterRemoval() {
        List<Object> updated = removeUser(sampleRawList, "w1");
        List<WaitlistUser> users = mapToUsers(updated);
        String label = "Waitlisters for Event: " + users.size();
        assertEquals("Waitlisters for Event: 2", label);
    }
}