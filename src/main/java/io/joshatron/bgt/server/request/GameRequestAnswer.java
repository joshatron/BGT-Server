package io.joshatron.bgt.server.request;

import io.joshatron.bgt.engine.player.PlayerIndicator;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class GameRequestAnswer {
    Answer answer;
    PlayerIndicator playerIndicator;
    Map<String,String> parameters;
}
