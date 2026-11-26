package com.nextstop.model;

public class User {
    private int userId;
    private String username;
    private String passwordHash;

    public User(String username, String passwordHash) {
        this(0, username, passwordHash);
    }

    public User(int userId, String username, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
}