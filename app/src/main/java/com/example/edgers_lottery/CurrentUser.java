package com.example.edgers_lottery;

public class CurrentUser {
    private static User instance;
    public static void set(User user) {
        instance = user;
    }
    public static User get() {
        return instance;
    }
    public static void clear() {
        instance = null;
    }
}