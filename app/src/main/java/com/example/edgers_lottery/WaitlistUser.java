package com.example.edgers_lottery;

public class WaitlistUser {
    private String userId;
    private String name;
    private String profileImage;

    public WaitlistUser(String userId, String name, String profileImage) {
        this.userId = userId;
        this.name = name;
        this.profileImage = profileImage;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getProfileImage() { return profileImage; }
}