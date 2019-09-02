package io.joshatron.bgt.server.database.model;

import io.joshatron.bgt.engine.state.GameState;

import java.util.List;

public class Game {
    private List<User> players;
    private long start;
    private long last;
    private long end;
    private GameState state;
}
