package io.joshatron.bgt.server.database.model;

import io.joshatron.bgt.engine.player.PlayerIndicator;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class GameRequest {
    private UUID id;
    private UUID requester;
    private Map<User, PlayerIndicator> players;
    private Map<String,String> parameters;
}
