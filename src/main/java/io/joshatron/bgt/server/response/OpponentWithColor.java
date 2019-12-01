package io.joshatron.bgt.server.response;

import io.joshatron.bgt.engine.player.PlayerIndicator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpponentWithColor {
    private UUID user;
    private PlayerIndicator color;
}
