package com.example.edgers_lottery;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.example.edgers_lottery.models.User;

import java.util.ArrayList;
import java.util.List;

public class AdminUserListTest {

    static class TestUserListActivity {

        final List<User> users = new ArrayList<>();

        void loadUsers(List<User> source) {
            users.clear();
            for (User u : source) {
                if (!"ADMIN".equals(u.getRole())) {
                    users.add(u);
                }
            }
        }

        void removeUser(String userId, List<User> source) {
            source.removeIf(u -> userId.equals(u.getId()));
            loadUsers(source);
        }
    }

    private TestUserListActivity activity;
    private List<User> firestoreSource;

    private User mockUser;
    private User adminUser;
    private User secondUser;

    @Before
    public void setUp() {
        activity = new TestUserListActivity();
        firestoreSource = new ArrayList<>();

        mockUser = new User();
        mockUser.setId("user1");
        mockUser.setName("Hamid");
        mockUser.setRole("ENTRANT");

        adminUser = new User();
        adminUser.setId("admin1");
        adminUser.setName("Admin Test2");
        adminUser.setRole("ADMIN");

        secondUser = new User();
        secondUser.setId("user2");
        secondUser.setName("John");
        secondUser.setRole("ENTRANT");
    }

    @Test
    public void testMockUser_fieldsAreCorrect() {
        assertEquals("user1",                    mockUser.getId());
        assertEquals("Hamid",               mockUser.getName());
        assertEquals("ENTRANT",                        mockUser.getRole());
    }

    @Test
    public void testMockUser_appearsInList() {
        firestoreSource.add(mockUser);
        activity.loadUsers(firestoreSource);

        assertEquals(1, activity.users.size());
        assertEquals("user1", activity.users.get(0).getId());
        assertEquals("Hamid", activity.users.get(0).getName());
    }

    @Test
    public void testAdminUser_isFilteredFromList() {
        firestoreSource.add(mockUser);
        firestoreSource.add(adminUser);
        activity.loadUsers(firestoreSource);

        assertEquals(1, activity.users.size());
        for (User u : activity.users) {
            assertNotEquals("ADMIN", u.getRole());
        }
    }

    @Test
    public void testList_isEmptyWhenOnlyAdminPresent() {
        firestoreSource.add(adminUser);
        activity.loadUsers(firestoreSource);

        assertTrue(activity.users.isEmpty());
    }

    @Test
    public void testMultipleUsers_allAppearInList() {
        firestoreSource.add(mockUser);
        firestoreSource.add(secondUser);
        firestoreSource.add(adminUser);
        activity.loadUsers(firestoreSource);

        assertEquals(2, activity.users.size());
    }

    @Test
    public void testRemoveUser_removesCorrectUser() {
        firestoreSource.add(mockUser);
        firestoreSource.add(secondUser);
        activity.loadUsers(firestoreSource);

        activity.removeUser("user1", firestoreSource);

        assertEquals(1, activity.users.size());
        assertEquals("user2", activity.users.get(0).getId());
    }

    @Test
    public void testRemoveUser_withUnknownId_doesNothing() {
        firestoreSource.add(mockUser);
        activity.loadUsers(firestoreSource);

        activity.removeUser("user3", firestoreSource);

        assertEquals(1, activity.users.size());
    }

    @Test
    public void testRemoveUser_lastUser_listBecomesEmpty() {
        firestoreSource.add(mockUser);
        activity.loadUsers(firestoreSource);

        activity.removeUser("user1", firestoreSource);

        assertTrue(activity.users.isEmpty());
    }
}
