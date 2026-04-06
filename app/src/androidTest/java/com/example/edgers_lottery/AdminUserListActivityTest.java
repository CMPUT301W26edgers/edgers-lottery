package com.example.edgers_lottery;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.anything;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.edgers_lottery.views.admin.AdminUserListActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminUserListActivityTest {

    @Rule
    public ActivityScenarioRule<AdminUserListActivity> rule =
            new ActivityScenarioRule<>(AdminUserListActivity.class);

    @Test
    public void testListDisplayed() {
        onView(withId(R.id.adminUserList)).check(matches(isDisplayed()));
    }

    @Test
    public void testBackButton() {
        onView(withId(R.id.backButton)).perform(click());
    }

    // US 03.02.01
    @Test
    public void testDeleteDialogAppears() {
        onData(anything())
                .inAdapterView(withId(R.id.adminUserList))
                .atPosition(0)
                .onChildView(withId(R.id.deleteButton))
                .perform(click());

        onView(withText("Delete User")).check(matches(isDisplayed()));
    }
}
