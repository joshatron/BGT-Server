package io.joshatron.bgt.server.response;

import io.joshatron.bgt.engine.state.GameParameters;
import io.joshatron.bgt.server.request.PlayerAndIndicator;
import io.joshatron.tak.engine.game.Player;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestInfo {
    private PlayerAndIndicator[] players;
    GameParameters parameters;
}
