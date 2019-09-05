package io.joshatron.bgt.server.logic.utils;

public class UserInfo {

    private String username;
    private String userId;
    private int rating;

    public UserInfo(String username, String userId, int rating) {
        this.username = username;
        this.userId = userId;
        this.rating = rating;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
