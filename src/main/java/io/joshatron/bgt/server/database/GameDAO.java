package io.joshatron.bgt.server.database;

import io.joshatron.bgt.engine.player.PlayerIndicator;
import io.joshatron.bgt.engine.state.GameParameters;
import io.joshatron.bgt.engine.state.GameState;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.request.Complete;
import io.joshatron.bgt.server.request.Pending;
import io.joshatron.bgt.server.request.Winner;
import io.joshatron.bgt.server.response.GameInfo;
import io.joshatron.bgt.server.response.GameNotifications;
import io.joshatron.bgt.server.response.RandomRequestInfo;
import io.joshatron.bgt.server.response.RequestInfo;
import io.joshatron.tak.engine.game.Player;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class GameDAO {

    public void createGameRequest(UUID requester, List<UUID> others, PlayerIndicator requesterColor, GameParameters gameParameters) throws GameServerException {

    }

    public void deleteGameRequest(UUID gameRequest) throws GameServerException {

    }

    public void createRandomGameRequest(UUID user) throws GameServerException {

    }

    public void deleteRandomGameRequest(UUID user) throws GameServerException {

    }

    public void startGame(UUID gameRequest) throws GameServerException {

    }
    public void updateState(UUID gameId, GameState state) throws GameServerException {

    }

    public void finishGame(UUID gameId) throws GameServerException {

    }

    public boolean playingGame(UUID user, UUID other) throws GameServerException {
        return false;
    }

    public boolean gameRequestExists(String requester, String other) throws GameServerException {
        return false;
    }

    public boolean randomGameRequestExists(String user) throws GameServerException {
        return false;
    }

    public boolean gameExists(String gameId) throws GameServerException {
        return false;
    }

    public boolean userAuthorizedForGame(String user, String gameId) throws GameServerException {
        return false;
    }

    public boolean isYourTurn(String user, String gameId) throws GameServerException {
        return false;
    }

    public RequestInfo getGameRequestInfo(String requester, String other) throws GameServerException {
        return null;
    }

    public RequestInfo[] getIncomingGameRequests(String user) throws GameServerException {
        return null;
    }

    public RequestInfo[] getOutgoingGameRequests(String user) throws GameServerException {
        return null;
    }

    public int getOutgoingRandomRequestSize(String user) throws GameServerException {
        return 0;
    }

    public RandomRequestInfo[] getRandomGameRequests() throws GameServerException {
        return null;
    }

    public GameInfo getGameInfo(String gameId) throws GameServerException {
        return null;
    }

    public GameInfo[] listGames(String userId, String[] opponents, Date start, Date end, Complete complete, Pending pending, int[] sizes, Winner winner, Player color) throws GameServerException {
        return null;
    }

    public GameNotifications getGameNotifications(String userId) throws GameServerException {
        return null;
    }
}
