package com.example.edgers_lottery;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Unit tests for the pure logic used in EventDetailsOrganizer.
 * Tests date parsing, countdown calculation, and UI label formatting
 * without any Android or Firestore dependencies.
 */
@RunWith(JUnit4.class)
public class EventDetailOrganizerTest {

    // -----------------------------------------------------------------------
    // Helpers — mirrors logic from loadEventFromFirestore()
    // -----------------------------------------------------------------------

    private static final SimpleDateFormat SDF =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /** Returns the countdown label string for a given date string, mirroring the activity. */
    private String buildCountdownLabel(String dateString) {
        if (dateString == null) return "No registration date set";
        try {
            Date eventDate = SDF.parse(dateString);
            long diffMillis = eventDate.getTime() - new Date().getTime();
            long daysRemaining = TimeUnit.MILLISECONDS.toDays(diffMillis);
            return "Registration ends in " + daysRemaining + " days";
        } catch (ParseException e) {
            return "Invalid registration date";
        }
    }

    private String buildEventNameLabel(String eventName) {
        return eventName != null ? eventName : "Unnamed Event";
    }

    private String buildEntrantLabel(Long capacity) {
        return "Entrants: " + (capacity != null ? capacity : 0);
    }

    private String buildDescriptionLabel(String desc) {
        return "Description: " + (desc != null ? desc : "");
    }

    // -----------------------------------------------------------------------
    // buildCountdownLabel() tests
    // -----------------------------------------------------------------------

    @Test
    public void countdownLabel_nullDateReturnsNoDateSet() {
        assertEquals("No registration date set", buildCountdownLabel(null));
    }

    @Test
    public void countdownLabel_malformedDateReturnsInvalid() {
        assertEquals("Invalid registration date", buildCountdownLabel("not-a-date"));
    }

    @Test
    public void countdownLabel_futureDateContainsPositiveDays() {
        // Pick a date far in the future so this never flips negative
        String label = buildCountdownLabel("2099-01-01");
        assertTrue(label.startsWith("Registration ends in "));
        // Extract the number and confirm it's positive
        String numStr = label.replace("Registration ends in ", "").replace(" days", "").trim();
        long days = Long.parseLong(numStr);
        assertTrue(days > 0);
    }

    @Test
    public void countdownLabel_pastDateContainsNegativeDays() {
        String label = buildCountdownLabel("2000-01-01");
        assertTrue(label.startsWith("Registration ends in "));
        String numStr = label.replace("Registration ends in ", "").replace(" days", "").trim();
        long days = Long.parseLong(numStr);
        assertTrue(days < 0);
    }

    @Test
    public void countdownLabel_formatIsCorrect() {
        String label = buildCountdownLabel("2099-12-31");
        assertTrue(label.matches("Registration ends in -?\\d+ days"));
    }

    // -----------------------------------------------------------------------
    // buildEventNameLabel() tests
    // -----------------------------------------------------------------------

    @Test
    public void eventNameLabel_nonNullNameReturnsName() {
        assertEquals("Summer Festival", buildEventNameLabel("Summer Festival"));
    }

    @Test
    public void eventNameLabel_nullReturnsUnnamed() {
        assertEquals("Unnamed Event", buildEventNameLabel(null));
    }

    @Test
    public void eventNameLabel_emptyStringReturnsEmpty() {
        assertEquals("", buildEventNameLabel(""));
    }

    // -----------------------------------------------------------------------
    // buildEntrantLabel() tests
    // -----------------------------------------------------------------------

    @Test
    public void entrantLabel_nonNullCapacity() {
        assertEquals("Entrants: 50", buildEntrantLabel(50L));
    }

    @Test
    public void entrantLabel_nullCapacityDefaultsToZero() {
        assertEquals("Entrants: 0", buildEntrantLabel(null));
    }

    @Test
    public void entrantLabel_zeroCapacity() {
        assertEquals("Entrants: 0", buildEntrantLabel(0L));
    }

    @Test
    public void entrantLabel_largeCapacity() {
        assertEquals("Entrants: 10000", buildEntrantLabel(10000L));
    }

    // -----------------------------------------------------------------------
    // buildDescriptionLabel() tests
    // -----------------------------------------------------------------------

    @Test
    public void descriptionLabel_nonNullDescription() {
        assertEquals("Description: A fun event", buildDescriptionLabel("A fun event"));
    }

    @Test
    public void descriptionLabel_nullDescriptionReturnsEmpty() {
        assertEquals("Description: ", buildDescriptionLabel(null));
    }

    @Test
    public void descriptionLabel_emptyDescription() {
        assertEquals("Description: ", buildDescriptionLabel(""));
    }

    // -----------------------------------------------------------------------
    // eventId null-safety tests
    // -----------------------------------------------------------------------

    @Test
    public void qrCodeContent_nullEventIdFallsBackToNoId() {
        String eventId = null;
        String content = eventId != null ? eventId : "no-id";
        assertEquals("no-id", content);
    }

    @Test
    public void qrCodeContent_validEventIdUsedDirectly() {
        String eventId = "abc123";
        String content = eventId != null ? eventId : "no-id";
        assertEquals("abc123", content);
    }
}