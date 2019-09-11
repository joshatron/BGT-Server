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
import java.util.stream.Stream;

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

    public boolean isRequestedByUser(UUID other) {
        return incomingFriendRequests.parallelStream().anyMatch(u -> u.getId().equals(other));
    }

    public boolean isRequestingUser(UUID other) {
        return outgoingFriendRequests.parallelStream().anyMatch(u -> u.getId().equals(other));
    }

    public boolean isFriend(UUID other) {
        return getAllFriends().parallelStream().anyMatch(u -> u.getId().equals(other));
    }

    public boolean isBlocking(UUID other) {
        return blocking.parallelStream().anyMatch(u -> u.getId().equals(other));
    }

    public boolean isBlocked(UUID other) {
        return blocked.parallelStream().anyMatch(u -> u.getId().equals(other));
    }

    public List<User> getAllFriends() {
        return Stream.concat(friends.stream(), friended.stream()).collect(Collectors.toList());
    }

    public String toString() {

        return  "(id: " + id.toString() +
                ", username: " + username +
                ", friends: " + getAllFriends().parallelStream().map(User::getId).collect(Collectors.toList()) +
                ", blocking: " + blocking.parallelStream().map(User::getId).collect(Collectors.toList()) +
                ", blocked: " + blocked.parallelStream().map(User::getId).collect(Collectors.toList()) +
                ", outgoing requests: " + outgoingFriendRequests.parallelStream().map(User::getId).collect(Collectors.toList()) +
                ", incoming requests: " + incomingFriendRequests.parallelStream().map(User::getId).collect(Collectors.toList()) +
                ")";
    }
}
