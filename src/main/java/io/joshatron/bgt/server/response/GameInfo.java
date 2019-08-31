package io.joshatron.bgt.server.response;

import io.joshatron.tak.engine.game.GameState;
import io.joshatron.tak.engine.game.Player;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameInfo {
    private String gameId;
    private String white;
    private String black;
    private int size;
    private Player first;
    private Player current;
    private long start;
    private long last;
    private long end;
    private Player winner;
    private boolean done;
    private String[] turns;
    private GameState fullState;
    private Message[] messages;
}
