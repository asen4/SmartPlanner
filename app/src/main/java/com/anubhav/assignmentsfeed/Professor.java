package com.anubhav.assignmentsfeed;

public class Professor {

    public String profileImage, fullName;

    public Professor() {

    }

    public Professor(String profileImage, String fullName) {
        this.profileImage = profileImage;
        this.fullName = fullName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
