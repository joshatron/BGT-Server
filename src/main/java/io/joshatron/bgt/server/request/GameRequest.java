package io.joshatron.bgt.server.request;

import io.joshatron.bgt.engine.state.GameParameters;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameRequest {
    private String playerIndicator;
    private String[] opponents;
    private GameParameters parameters;
}
