package io.joshatron.bgt.server.request;

import io.joshatron.bgt.engine.player.PlayerIndicator;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameRequestAnswer {
    Answer answer;
    PlayerIndicator playerIndicator;
}
