package com.example.edgers_lottery;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.junit.Assert.*;

import com.example.edgers_lottery.models.core.User;

import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EditProfileTest {

    private User testUser;

    @Before
    public void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setName("John Doe");
    }

    @Test
    public void newUser_hasCorrectId() {
        assertEquals("user-123", testUser.getId());
    }

    @Test
    public void newUser_hasCorrectName() {
        assertEquals("John Doe", testUser.getName());
    }

    @Test
    public void editUser_setsEmailCorrectly() {
        testUser.setEmail("test@example.com");
        assertEquals("test@example.com", testUser.getEmail());
    }

    @Test
    public void editUser_setsDescriptionCorrectly() {
        testUser.setDescription("A new description");
        assertEquals("A new description", testUser.getDescription());
    }

    @Test
    public void editUser_setsLocationCorrectly() {
        testUser.setLocation("Edmonton, AB");
        assertEquals("Edmonton, AB", testUser.getLocation());
    }

    @Test
    public void editUser_setsPhoneCorrectly() {
        testUser.setPhone("555-1234");
        assertEquals("555-1234", testUser.getPhone());
    }

    @Test
    public void editUser_setsUsernameCorrectly() {
        testUser.setUsername("testuser");
        assertEquals("testuser", testUser.getUsername());
    }

    @Test
    public void editUser_overwritesExistingFields() {
        testUser.setEmail("old@example.com");
        testUser.setEmail("new@example.com");
        assertEquals("new@example.com", testUser.getEmail());
    }

    @Test
    public void editUser_withNullFields_doesNotCrash() {
        try {
            testUser.setEmail(null);
            testUser.setDescription(null);
            testUser.setLocation(null);
            testUser.setPhone(null);
            testUser.setUsername(null);
        } catch (Exception e) {
            fail("Setting null fields threw an exception: " + e.getMessage());
        }
    }
}