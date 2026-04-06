package com.example.edgers_lottery;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.edgers_lottery.views.admin.AdminHomeActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class AdminHomeActivityTest {

    @Rule
    public ActivityScenarioRule<AdminHomeActivity> rule =
            new ActivityScenarioRule<>(AdminHomeActivity.class);

    @Test
    public void testNavigateToOrganizerList() {
        onView(withId(R.id.organizerListMenu)).perform(click());
    }

    @Test
    public void testNavigateToEvents() {
        onView(withId(R.id.eventListMenu)).perform(click());
    }

    @Test
    public void testNavigateToImages() {
        onView(withId(R.id.imagesViewMenu)).perform(click());
    }

    @Test
    public void testNavigateToUsers() {
        onView(withId(R.id.userListMenu)).perform(click());
    }

    @Test
    public void testNavigateToExport() {
        onView(withId(R.id.exportNotificationsMenu)).perform(click());
    }

    // US 03.09.01
    @Test
    public void testBottomNavButtons() {
        onView(withId(R.id.HomeButton)).perform(click());
        onView(withId(R.id.qrButton)).perform(click());
        onView(withId(R.id.ProfileButton)).perform(click());
    }
}