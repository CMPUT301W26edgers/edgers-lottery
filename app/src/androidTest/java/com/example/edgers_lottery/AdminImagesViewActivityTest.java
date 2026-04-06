package com.example.edgers_lottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.edgers_lottery.views.AdminImagesViewActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminImagesViewActivityTest {

    @Rule
    public ActivityScenarioRule<AdminImagesViewActivity> rule =
            new ActivityScenarioRule<>(AdminImagesViewActivity.class);

    // US 03.06.01
    @Test
    public void testRecyclerViewDisplayed() {
        onView(withId(R.id.imagesView)).check(matches(isDisplayed()));
    }

    @Test
    public void testBackButton() {
        onView(withId(R.id.backButton)).perform(click());
    }
}