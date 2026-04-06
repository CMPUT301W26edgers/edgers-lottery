package com.example.edgers_lottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.edgers_lottery.models.CurrentUser;
import com.example.edgers_lottery.models.User;
import com.example.edgers_lottery.views.HomeActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SearchEventTest {

    @Rule
    public ActivityScenarioRule<HomeActivity> activityRule =
            new ActivityScenarioRule<>(HomeActivity.class);

    @Before
    public void setup() {
        // set up a mock user so HomeActivity doesn't redirect to StartActivity
        User mockUser = new User();
        mockUser.setId("testUserId");
        mockUser.setName("Test User");
        mockUser.setEmail("test@test.com");
//        mockUser.setRole("ENTRANT");
        CurrentUser.set(mockUser);
    }

    @Test
    public void testSearchFiltersEvents() {
        // type in the search bar
        onView(withId(R.id.searchView))
                .perform(click());
        onView(withId(R.id.searchView))
                .perform(typeText("Music"));

        // list should be showing filtered results
        onView(withId(R.id.eventListView))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testSearchResetOnClear() {
        // type something first
        onView(withId(R.id.searchView))
                .perform(click());
        onView(withId(R.id.searchView))
                .perform(typeText("Music"));

        // clear the search
        onView(withId(R.id.searchView))
                .perform(clearText());

        // list should be back to full/filtered list
        onView(withId(R.id.eventListView))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testSearchWithNoResults() {
        onView(withId(R.id.searchView))
                .perform(click());
        onView(withId(R.id.searchView))
                .perform(typeText("xyzabc123notanevent"));

        // list should still be displayed even if empty
        onView(withId(R.id.eventListView))
                .check(matches(isDisplayed()));
    }

    // remove the @Rule
// @Rule
// public ActivityScenarioRule<HomeActivity> activityRule = ...

    @Test
    public void testFilterButtonOpensDialog() {
        // set user first
        User mockUser = new User();
        mockUser.setId("testUserId");
        mockUser.setName("Test User");
        mockUser.setEmail("test@test.com");
        mockUser.setRole("ENTRANT");
        CurrentUser.set(mockUser);

        // then launch activity
        try (ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(HomeActivity.class)) {
            onView(withId(R.id.btnFilter)).perform(click());
            onView(withText("Filter Events")).check(matches(isDisplayed()));
        }
    }
}