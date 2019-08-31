package io.joshatron.bgt.server.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameNotifications {
    private int incomingRequests;
    private int pendingGames;
}
