package io.joshatron.bgt.server.database.model;

import io.joshatron.bgt.engine.state.GameParameters;

import java.util.List;

public class GameRequest {
    private int id;
    private User requester;
    //If empty, request for game with random opponents
    private List<User> recipients;
    private GameParameters parameters;
}
