package io.joshatron.bgt.server.request;

import io.joshatron.bgt.engine.player.PlayerIndicator;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class PlayerAndIndicator {
    private UUID player;
    private PlayerIndicator color;
}
