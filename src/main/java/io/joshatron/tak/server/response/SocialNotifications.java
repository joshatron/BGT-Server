package io.joshatron.tak.server.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SocialNotifications {
    private int incomingRequests;
    private int incomingMessages;
}
