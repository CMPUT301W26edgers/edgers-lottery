package com.example.edgers_lottery;

import android.content.Intent;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.edgers_lottery.views.AdminUserProfileActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminUserProfileActivityTest {

    // US 03.05.01
    @Test
    public void testProfileLoads() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                AdminUserProfileActivity.class
        );
        intent.putExtra("userId", "testUserId");

        try (ActivityScenario<AdminUserProfileActivity> scenario =
                     ActivityScenario.launch(intent)) {

            onView(withId(R.id.profileName)).check(matches(isDisplayed()));
            onView(withId(R.id.username)).check(matches(isDisplayed()));
        }
    }
}
