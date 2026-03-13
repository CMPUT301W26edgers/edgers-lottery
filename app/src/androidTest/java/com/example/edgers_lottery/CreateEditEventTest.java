package com.example.edgers_lottery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for the pure logic used in CreateEditEventActivity.
 * Tests parseDate(), date cross-validation, price validation,
 * and event data map construction — no Android or Firestore dependencies.
 */
@RunWith(JUnit4.class)
public class CreateEditEventTest {

    // -----------------------------------------------------------------------
    // Helpers — mirrors private methods from CreateEditEventActivity
    // -----------------------------------------------------------------------

    private Calendar parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            String[] parts = dateStr.split("-");
            Calendar cal = Calendar.getInstance();
            cal.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal;
        } catch (Exception e) {
            return null;
        }
    }

    /** Returns true if the registration deadline is valid relative to the event date. */
    private boolean isDeadlineValid(String deadlineStr, String eventDateStr) {
        Calendar deadline = parseDate(deadlineStr);
        Calendar eventDate = parseDate(eventDateStr);
        if (deadline == null || eventDate == null) return false;
        return !deadline.after(eventDate);
    }

    /** Returns true if the event date is valid relative to the registration deadline. */
    private boolean isEventDateValid(String eventDateStr, String deadlineStr) {
        Calendar eventDate = parseDate(eventDateStr);
        Calendar deadline = parseDate(deadlineStr);
        if (eventDate == null || deadline == null) return false;
        return !eventDate.before(deadline);
    }

    /** Returns true if the price string is a valid non-negative number. */
    private boolean isPriceValid(String priceText) {
        if (priceText == null || priceText.isEmpty()) return false;
        try {
            double val = Double.parseDouble(priceText);
            return val >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Returns true if all required fields are non-empty. */
    private boolean areFieldsComplete(String eventName, String eventDate,
                                      String deadline, String price) {
        return eventName != null && !eventName.isEmpty()
                && eventDate != null && !eventDate.isEmpty()
                && deadline != null && !deadline.isEmpty()
                && price != null && !price.isEmpty();
    }

    /** Builds the event data map mirroring saveChanges(). */
    private Map<String, Object> buildEventMap(String name, String eventDate,
                                              String deadline, String price,
                                              String description, int capacity,
                                              boolean geoRequired, boolean waitlistEnabled) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("eventDate", eventDate);
        map.put("date", deadline);
        map.put("price", price);
        map.put("description", description);
        map.put("capacity", capacity);
        map.put("geoRequired", geoRequired);
        map.put("waitlistEnabled", waitlistEnabled);
        return map;
    }

    // -----------------------------------------------------------------------
    // parseDate() tests
    // -----------------------------------------------------------------------

    @Test
    public void parseDate_validDateReturnsCalendar() {
        Calendar cal = parseDate("2026-06-15");
        assertNotNull(cal);
        assertEquals(2026, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JUNE, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void parseDate_nullInputReturnsNull() {
        assertNull(parseDate(null));
    }

    @Test
    public void parseDate_emptyStringReturnsNull() {
        assertNull(parseDate(""));
    }

    @Test
    public void parseDate_malformedStringReturnsNull() {
        assertNull(parseDate("not-a-date"));
    }

    @Test
    public void parseDate_timeIsZeroedOut() {
        Calendar cal = parseDate("2026-08-01");
        assertNotNull(cal);
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    // -----------------------------------------------------------------------
    // Cross-field date validation tests
    // -----------------------------------------------------------------------

    @Test
    public void deadlineBeforeEventDate_isValid() {
        assertTrue(isDeadlineValid("2026-05-01", "2026-06-01"));
    }

    @Test
    public void deadlineEqualsEventDate_isValid() {
        assertTrue(isDeadlineValid("2026-06-01", "2026-06-01"));
    }

    @Test
    public void deadlineAfterEventDate_isInvalid() {
        assertFalse(isDeadlineValid("2026-07-01", "2026-06-01"));
    }

    @Test
    public void eventDateAfterDeadline_isValid() {
        assertTrue(isEventDateValid("2026-06-01", "2026-05-01"));
    }

    @Test
    public void eventDateEqualsDeadline_isValid() {
        assertTrue(isEventDateValid("2026-06-01", "2026-06-01"));
    }

    @Test
    public void eventDateBeforeDeadline_isInvalid() {
        assertFalse(isEventDateValid("2026-04-01", "2026-06-01"));
    }

    @Test
    public void deadlineValidation_nullDeadlineReturnsFalse() {
        assertFalse(isDeadlineValid(null, "2026-06-01"));
    }

    @Test
    public void deadlineValidation_nullEventDateReturnsFalse() {
        assertFalse(isDeadlineValid("2026-05-01", null));
    }

    // -----------------------------------------------------------------------
    // Price validation tests
    // -----------------------------------------------------------------------

    @Test
    public void price_validIntegerPasses() {
        assertTrue(isPriceValid("10"));
    }

    @Test
    public void price_validDecimalPasses() {
        assertTrue(isPriceValid("9.99"));
    }

    @Test
    public void price_zeroPasses() {
        assertTrue(isPriceValid("0"));
    }

    @Test
    public void price_emptyStringFails() {
        assertFalse(isPriceValid(""));
    }

    @Test
    public void price_nullFails() {
        assertFalse(isPriceValid(null));
    }

    @Test
    public void price_alphabeticStringFails() {
        assertFalse(isPriceValid("abc"));
    }

    @Test
    public void price_dollarSignAloneFails() {
        assertFalse(isPriceValid("$"));
    }

    // -----------------------------------------------------------------------
    // Required fields validation tests
    // -----------------------------------------------------------------------

    @Test
    public void allFieldsPresent_returnsTrue() {
        assertTrue(areFieldsComplete("Concert", "2026-06-01", "2026-05-01", "20.00"));
    }

    @Test
    public void missingEventName_returnsFalse() {
        assertFalse(areFieldsComplete("", "2026-06-01", "2026-05-01", "20.00"));
    }

    @Test
    public void missingEventDate_returnsFalse() {
        assertFalse(areFieldsComplete("Concert", "", "2026-05-01", "20.00"));
    }

    @Test
    public void missingDeadline_returnsFalse() {
        assertFalse(areFieldsComplete("Concert", "2026-06-01", "", "20.00"));
    }

    @Test
    public void missingPrice_returnsFalse() {
        assertFalse(areFieldsComplete("Concert", "2026-06-01", "2026-05-01", ""));
    }

    // -----------------------------------------------------------------------
    // Event data map tests
    // -----------------------------------------------------------------------

    @Test
    public void buildEventMap_containsAllKeys() {
        Map<String, Object> map = buildEventMap(
                "Concert", "2026-06-01", "2026-05-01",
                "20.00", "A great show", 50, true, false);
        assertTrue(map.containsKey("name"));
        assertTrue(map.containsKey("eventDate"));
        assertTrue(map.containsKey("date"));
        assertTrue(map.containsKey("price"));
        assertTrue(map.containsKey("description"));
        assertTrue(map.containsKey("capacity"));
        assertTrue(map.containsKey("geoRequired"));
        assertTrue(map.containsKey("waitlistEnabled"));
    }

    @Test
    public void buildEventMap_valuesAreCorrect() {
        Map<String, Object> map = buildEventMap(
                "Concert", "2026-06-01", "2026-05-01",
                "20.00", "A great show", 50, true, false);
        assertEquals("Concert", map.get("name"));
        assertEquals("2026-06-01", map.get("eventDate"));
        assertEquals("2026-05-01", map.get("date"));
        assertEquals("20.00", map.get("price"));
        assertEquals("A great show", map.get("description"));
        assertEquals(50, map.get("capacity"));
        assertEquals(true, map.get("geoRequired"));
        assertEquals(false, map.get("waitlistEnabled"));
    }

    @Test
    public void buildEventMap_geoAndWaitlistFalseByDefault() {
        Map<String, Object> map = buildEventMap(
                "Event", "2026-07-01", "2026-06-01",
                "0", "", 1, false, false);
        assertEquals(false, map.get("geoRequired"));
        assertEquals(false, map.get("waitlistEnabled"));
    }
}