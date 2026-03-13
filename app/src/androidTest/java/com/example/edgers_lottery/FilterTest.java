package com.example.edgers_lottery;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class FilterTest {

    private ArrayList<Event> allEvents;

    @Before
    public void setUp() {
        allEvents = new ArrayList<>();

        Event e1 = new Event();
        e1.setName("Chess Tournament");
        e1.setDescription("A chess event");
        e1.setDate("2025-01-15");

        Event e2 = new Event();
        e2.setName("Soccer Game");
        e2.setDescription("Outdoor soccer");
        e2.setDate("2025-06-20");

        Event e3 = new Event();
        e3.setName("Art Show");
        e3.setDescription("Modern art exhibition");
        e3.setDate("2025-03-10");

        Event e4 = new Event();
        e4.setName("No Date Event");
        e4.setDescription("This event has no date");
        e4.setDate(null);

        allEvents.add(e1);
        allEvents.add(e2);
        allEvents.add(e3);
        allEvents.add(e4);
    }

    // mirrors the filter logic in HomeActivity.onFilterApplied
    private ArrayList<Event> applyFilter(String interests, String availabilityStart, String availabilityEnd) {
        ArrayList<Event> filtered = new ArrayList<>();
        for (Event event : allEvents) {
            boolean matchesInterest = interests == null || interests.isEmpty()
                    || (event.getName() != null && event.getName().toLowerCase().contains(interests.toLowerCase()))
                    || (event.getDescription() != null && event.getDescription().toLowerCase().contains(interests.toLowerCase()));
            boolean matchesStart = availabilityStart == null || availabilityStart.isEmpty()
                    || (event.getDate() != null && event.getDate().compareTo(availabilityStart) >= 0);
            boolean matchesEnd = availabilityEnd == null || availabilityEnd.isEmpty()
                    || (event.getDate() != null && event.getDate().compareTo(availabilityEnd) <= 0);

            if (matchesInterest && matchesStart && matchesEnd) {
                filtered.add(event);
            }
        }
        return filtered;
    }

    @Test
    public void noFilters_returnsAllEvents() {
        ArrayList<Event> result = applyFilter("", "", "");
        assertEquals(4, result.size());
    }

    @Test
    public void filterByInterest_returnsMatchingEvents() {
        ArrayList<Event> result = applyFilter("chess", "", "");
        assertEquals(1, result.size());
        assertEquals("Chess Tournament", result.get(0).getName());
    }

    @Test
    public void filterByStartDate_returnsEventsAfterStart() {
        ArrayList<Event> result = applyFilter("", "2025-03-01", "");
        // Soccer (June), Art (March) match; Chess (January) and null date do not
        assertEquals(2, result.size());
    }

    @Test
    public void filterByEndDate_returnsEventsBeforeEnd() {
        ArrayList<Event> result = applyFilter("", "", "2025-03-31");
        // Chess (January), Art (March) match; Soccer (June) and null date do not
        assertEquals(2, result.size());
    }

    @Test
    public void filterByDateRange_returnsEventsWithinRange() {
        ArrayList<Event> result = applyFilter("", "2025-02-01", "2025-05-01");
        // Only Art (March) matches
        assertEquals(1, result.size());
        assertEquals("Art Show", result.get(0).getName());
    }

    @Test
    public void filterByInterestAndDate_returnsCombinedMatch() {
        ArrayList<Event> result = applyFilter("soccer", "2025-05-01", "");
        assertEquals(1, result.size());
        assertEquals("Soccer Game", result.get(0).getName());
    }

    @Test
    public void nullDateEvents_notIncludedWhenDateFilterActive() {
        ArrayList<Event> result = applyFilter("", "2025-01-01", "");
        for (Event e : result) {
            assertNotNull(e.getDate());
        }
    }
}
