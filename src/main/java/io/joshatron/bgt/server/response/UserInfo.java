package io.joshatron.bgt.server.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfo {
    private String username;
    private String userId;
    private int rating;
    private State state;
}
