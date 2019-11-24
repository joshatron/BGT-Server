package io.joshatron.bgt.server.database;

import io.joshatron.bgt.engine.player.PlayerIndicator;
import io.joshatron.bgt.engine.state.GameParameters;
import io.joshatron.bgt.engine.state.GameState;
import io.joshatron.bgt.server.database.model.GameRequest;
import io.joshatron.bgt.server.request.Complete;
import io.joshatron.bgt.server.request.Pending;
import io.joshatron.bgt.server.request.RequestGame;
import io.joshatron.bgt.server.response.GameInfo;
import io.joshatron.bgt.server.response.GameNotifications;
import io.joshatron.bgt.server.response.RandomRequestInfo;
import io.joshatron.bgt.server.response.RequestInfo;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class GameDAO {

    public void createGameRequest(RequestGame gameRequest) {

    }

    public void deleteGameRequest(UUID gameRequest) {

    }

    public void createRandomGameRequest(UUID user) {

    }

    public void deleteRandomGameRequest(UUID user) {

    }

    public void startGame(UUID gameRequest) {

    }
    public void updateState(UUID gameId, GameState state) {

    }

    public void finishGame(UUID gameId) {

    }

    public boolean playingGame(UUID user, UUID other) {
        return false;
    }

    public boolean gameRequestExists(UUID request) {
        return false;
    }

    public boolean randomGameRequestExists(String user) {
        return false;
    }

    public boolean gameExists(String gameId) {
        return false;
    }

    public boolean userAuthorizedForGame(String user, String gameId) {
        return false;
    }

    public boolean isYourTurn(String user, String gameId) {
        return false;
    }

    public GameRequest getGameRequestInfo(UUID request) {
        return null;
    }

    public RequestInfo[] getIncomingGameRequests(String user) {
        return null;
    }

    public RequestInfo[] getOutgoingGameRequests(String user) {
        return null;
    }

    public int getOutgoingRandomRequestSize(String user) {
        return 0;
    }

    public RandomRequestInfo[] getRandomGameRequests() {
        return null;
    }

    public GameInfo getGameInfo(String gameId) {
        return null;
    }

    public GameInfo[] listGames(String userId, String[] opponents, Date start, Date end, Complete complete, Pending pending) {
        return null;
    }

    public GameNotifications getGameNotifications(String userId) {
        return null;
    }
}
