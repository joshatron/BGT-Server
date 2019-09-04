package io.joshatron.bgt.server.database.model;

import io.joshatron.bgt.server.response.State;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static javax.persistence.CascadeType.ALL;

@Entity
@Data
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private int rating;
    @Column(nullable = false)
    private int loginsFailed;
    @Enumerated
    private State state;
    @Column(nullable = false)
    private Timestamp lastActivity;
    @ManyToMany(cascade = ALL)
    private List<User> friends;
    @ManyToMany
    private List<User> blocking;
    @ManyToMany
    private List<User> outgoingFriendRequests;
    @ManyToMany
    private List<User> incomingFriendRequests;

    public User() {
        id = null;
        username = "";
        password = "";
        rating = 1000;
        loginsFailed = 0;
        state = State.NORMAL;
        lastActivity = new Timestamp(new Date().getTime());
        friends = new ArrayList<>();
        blocking = new ArrayList<>();
        outgoingFriendRequests = new ArrayList<>();
        incomingFriendRequests = new ArrayList<>();
    }

    public void updateLastActivity() {
        lastActivity = new Timestamp(new Date().getTime());
    }

    public void incrementFailed() {
        loginsFailed++;
    }
}
