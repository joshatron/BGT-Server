package io.joshatron.bgt.server.response;

import io.joshatron.bgt.server.database.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfo {
    private String username;
    private String userId;
    private int rating;
    private State state;

    public UserInfo(User user) {
        this.username = user.getUsername();
        this.userId = user.getId().toString();
        this.rating = user.getRating();
        this.state = user.getState();
    }
}
