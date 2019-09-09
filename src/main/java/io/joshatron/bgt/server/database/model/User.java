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
import java.util.stream.Collectors;

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
    @ManyToMany
    @JoinTable(name="FRIENDS",joinColumns=@JoinColumn(name="person"),inverseJoinColumns=@JoinColumn(name="friend"))
    private List<User> friends;
    @ManyToMany(mappedBy = "friends")
    private List<User> friended;
    @ManyToMany
    @JoinTable(name="BLOCKS",joinColumns=@JoinColumn(name="person"),inverseJoinColumns=@JoinColumn(name="blocked"))
    private List<User> blocking;
    @ManyToMany(mappedBy = "blocking")
    private List<User> blocked;
    @ManyToMany
    @JoinTable(name="FRIEND_REQUESTS",joinColumns=@JoinColumn(name="person"),inverseJoinColumns=@JoinColumn(name="request"))
    private List<User> outgoingFriendRequests;
    @ManyToMany(mappedBy = "outgoingFriendRequests")
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
        friended = new ArrayList<>();
        blocking = new ArrayList<>();
        blocked = new ArrayList<>();
        outgoingFriendRequests = new ArrayList<>();
        incomingFriendRequests = new ArrayList<>();
    }

    public void updateLastActivity() {
        lastActivity = new Timestamp(new Date().getTime());
    }

    public void incrementFailed() {
        loginsFailed++;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(id: ");
        builder.append(id.toString());
        builder.append(", username: ");
        builder.append(username);
        builder.append(", friends: ");
        builder.append(friends.stream().map(User::getId).collect(Collectors.toList()));
        builder.append(", outgoing requests: ");
        builder.append(outgoingFriendRequests.stream().map(User::getId).collect(Collectors.toList()));
        builder.append(", incoming requests: ");
        builder.append(incomingFriendRequests.stream().map(User::getId).collect(Collectors.toList()));

        return builder.toString();
    }
}
