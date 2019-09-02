package io.joshatron.bgt.server.database.model;

import java.util.List;

public class User {
    private long id;
    private String username;
    private String password;
    private int rating;
    private int loginsFailed;
    private UserState state;
    private long lastActivity;
    private List<User> friends;
    private List<User> blocking;
    private List<User> friendRequests;
}
