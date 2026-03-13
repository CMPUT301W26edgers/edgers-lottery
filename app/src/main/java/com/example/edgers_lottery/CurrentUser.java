package com.example.edgers_lottery;

/**
 * Singleton helper class that holds the currently authenticated user in memory.
 * Provides static methods to set, retrieve, and clear the global {@link User} instance
 * across activities without needing to pass it through intents.
 */
public class CurrentUser {

    private static User instance;

    /**
     * Sets the current logged-in user.
     *
     * @param user the {@link User} object to store as the current user
     */
    public static void set(User user) {
        instance = user;
    }

    /**
     * Retrieves the current logged-in user.
     *
     * @return the current {@link User} instance, or null if no user is logged in
     */
    public static User get() {
        return instance;
    }

    /**
     * Clears the current user, typically called on logout.
     */
    public static void clear() {
        instance = null;
    }
}