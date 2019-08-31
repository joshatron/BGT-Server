package io.joshatron.tak.server.database;

import io.joshatron.tak.engine.game.Player;
import io.joshatron.tak.server.exceptions.GameServerException;
import io.joshatron.tak.server.request.Complete;
import io.joshatron.tak.server.request.Pending;
import io.joshatron.tak.server.request.Winner;
import io.joshatron.tak.server.response.GameInfo;
import io.joshatron.tak.server.response.GameNotifications;
import io.joshatron.tak.server.response.RandomRequestInfo;
import io.joshatron.tak.server.response.RequestInfo;

import java.util.Date;

public interface GameDAO {
    void createGameRequest(String requester, String other, int size, Player requesterColor, Player first) throws GameServerException;
    void deleteGameRequest(String requester, String other) throws GameServerException;
    void createRandomGameRequest(String user, int size) throws GameServerException;
    void deleteRandomGameRequest(String user) throws GameServerException;
    void startGame(String requester, String other, int size, Player requesterColor, Player first) throws GameServerException;
    void addTurn(String gameId, String text) throws GameServerException;
    void finishGame(String gameId, Player winner) throws GameServerException;
    boolean playingGame(String user, String other) throws GameServerException;
    boolean gameRequestExists(String requester, String other) throws GameServerException;
    boolean randomGameRequestExists(String user) throws GameServerException;
    boolean gameExists(String gameId) throws GameServerException;
    boolean userAuthorizedForGame(String user, String gameId) throws GameServerException;
    boolean isYourTurn(String user, String gameId) throws GameServerException;
    RequestInfo getGameRequestInfo(String requester, String other) throws GameServerException;
    RequestInfo[] getIncomingGameRequests(String user) throws GameServerException;
    RequestInfo[] getOutgoingGameRequests(String user) throws GameServerException;
    int getOutgoingRandomRequestSize(String user) throws GameServerException;
    RandomRequestInfo[] getRandomGameRequests() throws GameServerException;
    GameInfo getGameInfo(String gameId) throws GameServerException;
    GameInfo[] listGames(String userId, String[] opponents, Date start, Date end, Complete complete, Pending pending, int[] sizes, Winner winner, Player color) throws GameServerException;
    GameNotifications getGameNotifications(String userId) throws GameServerException;
}
