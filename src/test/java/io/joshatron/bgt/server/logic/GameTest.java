package io.joshatron.bgt.server.logic;

import io.joshatron.bgt.server.logic.utils.AccountUtils;
import io.joshatron.bgt.server.logic.utils.GameUtils;
import io.joshatron.bgt.server.logic.utils.SocialUtils;
import io.joshatron.bgt.server.logic.utils.User;
import io.joshatron.tak.engine.exception.TakEngineException;
import io.joshatron.tak.engine.game.GameState;
import io.joshatron.tak.engine.game.Player;
import io.joshatron.tak.engine.turn.TurnUtils;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.Date;

public class GameTest extends BaseTest {

    private String playSimpleGame(User user1, User user2, String requesterColor, String first) throws Exception {
        GameUtils.requestGame(user1, user2, 3, requesterColor, first, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        String gameId = GameUtils.searchGames(user1, user2.getUserId(), null, null, "INCOMPLETE", null, null, null, null, client, HttpStatus.SC_OK, 1).getJSONObject(0).getString("gameId");
        GameUtils.playTurn(user1, gameId, "ps b1", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.playTurn(user2, gameId, "ps a1", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.playTurn(user1, gameId, "ps a2", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.playTurn(user2, gameId, "ps b2", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.playTurn(user1, gameId, "ps a3", client, HttpStatus.SC_NO_CONTENT);

        return gameId;
    }

    //Request a Game
    @Test(groups = {"parallel"})
    public void requestGame_RequestFriend_204RequestMade() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_OK, new User[]{user1}, null);
    }

    @Test(groups = {"parallel"})
    public void requestGame_RequestNonFriend_403RequestNotMade() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_FORBIDDEN);
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_OK, null, new User[]{user1});
    }

    @Test(groups = {"parallel"})
    public void requestGame_RequestPendingFriend_403RequestNotMade() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_FORBIDDEN);
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_OK, null, new User[]{user1});
    }

    @Test(groups = {"parallel"})
    public void requestGame_RequestBlocked_403RequestNotMade() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.blockUser(user2, user1, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_FORBIDDEN);
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_OK, null, new User[]{user1});
    }

    @Test(groups = {"parallel"})
    public void requestGame_RequestNonexistent_404RequestNotMade() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = new User(test + "03", "password");
        GameUtils.requestGame(user1, user3, 5, "WHITE", "WHITE", client, HttpStatus.SC_NOT_FOUND);
        GameUtils.checkOutgoing(user1, client, HttpStatus.SC_OK, null, new User[]{user3});
    }

    @Test(groups = {"parallel"})
    public void requestGame_RequestWithExistingRequest_403RequestNotMade() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_OK, new User[]{user1}, null);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_FORBIDDEN);
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_OK, new User[]{user1}, null);
    }

    @Test(groups = {"parallel"})
    public void requestGame_RequestYourself_403RequestNotMade() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user, user, 6, "BLACK", "BLACK", client, HttpStatus.SC_FORBIDDEN);
        GameUtils.checkOutgoing(user, client, HttpStatus.SC_OK, null, new User[]{user});
    }

    @Test(groups = {"parallel"})
    public void requestGame_RequestExistingGame_403RequestNotMade() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_OK, new User[]{user1}, null);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_FORBIDDEN);
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_OK, null, new User[]{user1});
    }

    @Test(groups = {"parallel"})
    public void requestGame_InvalidUser_401RequestNotMade() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = new User(test + "03", "password");
        GameUtils.requestGame(user3, user1, 5, "WHITE", "WHITE", client, HttpStatus.SC_UNAUTHORIZED);
        GameUtils.checkIncoming(user1, client, HttpStatus.SC_OK, null, new User[]{user3});
    }

    @Test(groups = {"parallel"})
    public void requestGame_InvalidCredentials_401RequestNotMade() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        user1.setPassword("drowssap");
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_UNAUTHORIZED);
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_OK, null, new User[]{user1});
    }

    //Cancel a Game Request
    @Test(groups = {"parallel"})
    public void cancelGameRequest_BasicRequest_204RequestRemoved() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.deleteGameRequest(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_OK, null, new User[]{user1});
    }

    @Test(groups = {"parallel"})
    public void cancelGameRequest_CancelInvalidUser_404() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = new User(test + "03", "password");
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.deleteGameRequest(user1, user3, client, HttpStatus.SC_NOT_FOUND);
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_OK, new User[]{user1}, null);
    }

    @Test(groups = {"parallel"})
    public void cancelGameRequest_RequestNotMade_404() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.deleteGameRequest(user1, user2, client, HttpStatus.SC_NOT_FOUND);
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_OK, null, new User[]{user1});
    }

    @Test(groups = {"parallel"})
    public void cancelGameRequest_InvalidUser_401() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = new User(test + "03", "password");
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.deleteGameRequest(user3, user2, client, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(groups = {"parallel"})
    public void cancelGameRequest_InvalidCredentials_401() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        user1.setPassword("drowssap");
        GameUtils.deleteGameRequest(user1, user2, client, HttpStatus.SC_UNAUTHORIZED);
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_OK, new User[]{user1}, null);
    }

    //Respond to Game Request
    @Test(groups = {"parallel"})
    public void respondToGameRequest_RespondAccept_204GameStartedRequestRemoved() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_OK, null, new User[]{user1});
        GameUtils.searchAllGames(user2, client, HttpStatus.SC_OK, 1);
    }

    @Test(groups = {"parallel"})
    public void respondToGameRequest_RespondDeny_204GameNotStartedRequestRemoved() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "DENY", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_OK, null, new User[]{user1});
        GameUtils.searchAllGames(user2, client, HttpStatus.SC_OK, 0);
    }

    @Test(groups = {"parallel"})
    public void respondToGameRequest_RespondBadFormatting_400GameNotStartedRequestStillThere() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "INVALID", client, HttpStatus.SC_BAD_REQUEST);
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_OK, new User[]{user1}, null);
        GameUtils.searchAllGames(user2, client, HttpStatus.SC_OK, 0);
    }

    @Test(groups = {"parallel"})
    public void respondToGameRequest_RespondNoRequest_404NoGameStarted() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NOT_FOUND);
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_OK, null, new User[]{user1});
        GameUtils.searchAllGames(user2, client, HttpStatus.SC_OK, 0);
    }

    @Test(groups = {"parallel"})
    public void respondToGameRequest_InvalidUser_401GameNotStartedRequestStillThere() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = new User(test + "03", "password");
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user3, user1, "ACCEPT", client, HttpStatus.SC_UNAUTHORIZED);
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_OK, new User[]{user1}, null);
        GameUtils.searchAllGames(user2, client, HttpStatus.SC_OK, 0);
    }

    @Test(groups = {"parallel"})
    public void respondToGameRequest_InvalidCredentials_401GameNotStartedRequestStillThere() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        user2.setPassword("drowssap");
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_UNAUTHORIZED);
        user2.setPassword("password");
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_OK, new User[]{user1}, null);
        GameUtils.searchAllGames(user2, client, HttpStatus.SC_OK, 0);
    }

    //Check Incoming Game Request
    @Test(groups = {"parallel"})
    public void checkIncomingGames_NoRequests_200EmptyArray() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.checkIncoming(user, client, HttpStatus.SC_OK, null, null);
    }

    @Test(groups = {"parallel"})
    public void checkIncomingGames_OneRequest_200ArrayWithOne() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_OK, new User[]{user1}, null);
    }

    @Test(groups = {"parallel"})
    public void checkIncomingGames_MultipleRequests_200ArrayWithMultiple() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user3, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user3, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user3, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_OK, new User[]{user1, user3}, null);
    }

    @Test(groups = {"parallel"})
    public void checkIncomingGames_OneOutgoing_200EmptyArray() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.checkIncoming(user1, client, HttpStatus.SC_OK, null, new User[]{user2});
    }

    @Test(groups = {"parallel"})
    public void checkIncomingGames_InvalidUser_401() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = new User(test + "03", "password");
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.checkIncoming(user3, client, HttpStatus.SC_UNAUTHORIZED, null, new User[]{user2, user1});
    }

    @Test(groups = {"parallel"})
    public void checkIncomingGames_InvalidCredentials_401() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        user2.setPassword("drowssap");
        GameUtils.checkIncoming(user2, client, HttpStatus.SC_UNAUTHORIZED, null, new User[]{user1});
    }

    //Check Outgoing Game Request
    @Test(groups = {"parallel"})
    public void checkOutgoingGames_NoRequests_200EmptyArray() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.checkOutgoing(user, client, HttpStatus.SC_OK, null, null);
    }

    @Test(groups = {"parallel"})
    public void checkOutgoingGames_200ArrayWithOne() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.checkOutgoing(user1, client, HttpStatus.SC_OK, new User[]{user2}, null);
    }

    @Test(groups = {"parallel"})
    public void checkOutgoingGames_200ArrayWithMultiple() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user3, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user3, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user3, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.checkOutgoing(user1, client, HttpStatus.SC_OK, new User[]{user2, user3}, null);
    }

    @Test(groups = {"parallel"})
    public void checkOutgoingGames_OneIncoming_200EmptyArray() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.checkOutgoing(user2, client, HttpStatus.SC_OK, null, new User[]{user1});
    }

    @Test(groups = {"parallel"})
    public void checkOutgoingGames_InvalidUser_401() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = new User(test + "03", "password");
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.checkOutgoing(user3, client, HttpStatus.SC_UNAUTHORIZED, null, new User[]{user2, user1});
    }

    @Test(groups = {"parallel"})
    public void checkOutgoingGames_InvalidCredentials_401() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        user1.setPassword("drowssap");
        GameUtils.checkOutgoing(user1, client, HttpStatus.SC_UNAUTHORIZED, null, new User[]{user2});
    }

    //Request a Random Game
    @Test(groups = {"serial"})
    public void requestRandomGame_OneRequest_204NoGameCreated() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user, 5, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchAllGames(user, client, HttpStatus.SC_OK, 0);
        GameUtils.rawDeleteRandomGameRequest(user, client);
    }

    @Test(groups = {"serial"})
    public void requestRandomGame_TwoMatchingRequests_204GameCreated() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user1, 5, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user2, 5, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchAllGames(user1, client, HttpStatus.SC_OK, 1);
        GameUtils.searchAllGames(user2, client, HttpStatus.SC_OK, 1);
        GameUtils.rawDeleteRandomGameRequest(user1, client);
        GameUtils.rawDeleteRandomGameRequest(user2, client);
    }

    @Test(groups = {"serial"})
    public void requestRandomGame_TwoMismatchingRequests_204NoGameCreated() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user1, 5, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user2, 6, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchAllGames(user1, client, HttpStatus.SC_OK, 0);
        GameUtils.searchAllGames(user2, client, HttpStatus.SC_OK, 0);
        GameUtils.rawDeleteRandomGameRequest(user1, client);
        GameUtils.rawDeleteRandomGameRequest(user2, client);
    }

    @Test(groups = {"serial"})
    public void requestRandomGame_TwoMatchingAlreadyInGame_204NoGameCreated() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user1, 5, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user2, 5, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchAllGames(user1, client, HttpStatus.SC_OK, 1);
        GameUtils.searchAllGames(user2, client, HttpStatus.SC_OK, 1);
        GameUtils.rawDeleteRandomGameRequest(user1, client);
        GameUtils.rawDeleteRandomGameRequest(user2, client);
    }

    @Test(groups = {"serial"})
    public void requestRandomGame_TwoFriendsMatching_204GameCreated() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user1, 5, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user2, 5, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchAllGames(user1, client, HttpStatus.SC_OK, 1);
        GameUtils.searchAllGames(user2, client, HttpStatus.SC_OK, 1);
        GameUtils.rawDeleteRandomGameRequest(user1, client);
        GameUtils.rawDeleteRandomGameRequest(user2, client);
    }

    @Test(groups = {"serial"})
    public void requestRandomGame_TwoRandomMatching_204GameCreated() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user1, 5, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user2, 5, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchAllGames(user1, client, HttpStatus.SC_OK, 1);
        GameUtils.searchAllGames(user2, client, HttpStatus.SC_OK, 1);
        GameUtils.rawDeleteRandomGameRequest(user1, client);
        GameUtils.rawDeleteRandomGameRequest(user2, client);
    }

    @Test(groups = {"serial"})
    public void requestRandomGame_TwoMatchingBlocked_204NoGameCreated() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.blockUser(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user1, 5, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user2, 5, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchAllGames(user1, client, HttpStatus.SC_OK, 0);
        GameUtils.searchAllGames(user2, client, HttpStatus.SC_OK, 0);
        GameUtils.rawDeleteRandomGameRequest(user1, client);
        GameUtils.rawDeleteRandomGameRequest(user2, client);
    }

    @Test(groups = {"serial"})
    public void requestRandomGame_TwoMismatchThenThirdMatch_204OneGameCreated() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user1, 5, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user2, 3, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user3, 5, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchAllGames(user1, client, HttpStatus.SC_OK, 1);
        GameUtils.searchAllGames(user2, client, HttpStatus.SC_OK, 0);
        GameUtils.searchAllGames(user3, client, HttpStatus.SC_OK, 1);
        GameUtils.rawDeleteRandomGameRequest(user1, client);
        GameUtils.rawDeleteRandomGameRequest(user2, client);
        GameUtils.rawDeleteRandomGameRequest(user3, client);
    }

    @Test(groups = {"serial"})
    public void requestRandomGame_TwoBlockedMatchedThenNonBlockMatch_204OneGameCreated() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.blockUser(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user1, 5, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user2, 5, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user3, 5, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchAllGames(user3, client, HttpStatus.SC_OK, 1);
        GameUtils.rawDeleteRandomGameRequest(user1, client);
        GameUtils.rawDeleteRandomGameRequest(user2, client);
        GameUtils.rawDeleteRandomGameRequest(user3, client);
    }

    @Test(groups = {"serial"})
    public void requestRandomGame_InvalidUser_401() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = new User(test + "02", "password");
        GameUtils.requestRandomGame(user2, 3, client, HttpStatus.SC_UNAUTHORIZED);
        GameUtils.rawDeleteRandomGameRequest(user1, client);
    }

    @Test(groups = {"serial"})
    public void requestRandomGame_InvalidCredentials_401() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        user.setPassword("drowssap");
        GameUtils.requestRandomGame(user, 5, client, HttpStatus.SC_UNAUTHORIZED);
        user.setPassword("password");
        GameUtils.rawDeleteRandomGameRequest(user, client);
    }

    @Test(groups = {"serial"})
    public void requestRandomGame_GameSizeIllegalNumber_400() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user, 7, client, HttpStatus.SC_BAD_REQUEST);
        GameUtils.rawDeleteRandomGameRequest(user, client);
    }

    //Cancel a Random Game Request
    @Test(groups = {"serial"})
    public void cancelRandomRequest_BasicRequest_204RequestRemoved() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user, 3, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.deleteRandomGameRequest(user, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.rawDeleteRandomGameRequest(user, client);
    }

    @Test(groups = {"serial"})
    public void cancelRandomRequest_RequestNotMade_404() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.deleteRandomGameRequest(user, client, HttpStatus.SC_NOT_FOUND);
    }

    @Test(groups = {"serial"})
    public void cancelRandomRequest_InvalidUser_401() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = new User(test + "02", "password");
        GameUtils.deleteRandomGameRequest(user2, client, HttpStatus.SC_UNAUTHORIZED);
    }

    @Test(groups = {"serial"})
    public void cancelRandomRequest_InvalidCredentials_401() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user, 3, client, HttpStatus.SC_NO_CONTENT);
        user.setPassword("drowssap");
        GameUtils.deleteRandomGameRequest(user, client, HttpStatus.SC_UNAUTHORIZED);
        user.setPassword("password");
        GameUtils.rawDeleteRandomGameRequest(user, client);
    }

    //Check Random Game Request
    @Test(groups = {"serial"})
    public void checkRandomRequest_BasicRequest_200Size() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user, 5, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.getRandomRequestSize(user, client, HttpStatus.SC_OK, 5);
        GameUtils.rawDeleteRandomGameRequest(user, client);
    }

    @Test(groups = {"serial"})
    public void checkRandomRequest_InvalidUser_401() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = new User(test + "02", "password");
        GameUtils.requestRandomGame(user1, 5, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.getRandomRequestSize(user2, client, HttpStatus.SC_UNAUTHORIZED, 0);
        GameUtils.rawDeleteRandomGameRequest(user1, client);
    }

    @Test(groups = {"serial"})
    public void checkRandomRequest_InvalidCredentials_401() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestRandomGame(user, 5, client, HttpStatus.SC_NO_CONTENT);
        user.setPassword("drowssap");
        GameUtils.getRandomRequestSize(user, client, HttpStatus.SC_UNAUTHORIZED, 0);
        user.setPassword("password");
        GameUtils.rawDeleteRandomGameRequest(user, client);
    }

    @Test(groups = {"serial"})
    public void checkRandomRequest_RequestNotMade_404() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.getRandomRequestSize(user, client, HttpStatus.SC_NOT_FOUND, 0);
        GameUtils.rawDeleteRandomGameRequest(user, client);
    }

    //List Games
    @Test(groups = {"parallel"})
    public void listGames_NoParametersNoGames_200EmptyArray() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchAllGames(user, client, HttpStatus.SC_OK, 0);
    }

    @Test(groups = {"parallel"})
    public void listGames_NoParametersOneGame_200ArrayWithOne() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchAllGames(user1, client, HttpStatus.SC_OK, 1);
    }

    @Test(groups = {"parallel"})
    public void listGames_NoParametersMultipleGames_200ArrayWithMultiple() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user3, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user3, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user3, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user3, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchAllGames(user1, client, HttpStatus.SC_OK, 2);
    }

    @Test(groups = {"parallel"})
    public void listGames_AllParameters_200ArrayWithSelected() throws Exception, InterruptedException {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user3, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user3, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        playSimpleGame(user1, user2, "BLACK", "BLACK");
        Date start = new Date();
        Thread.sleep(2000);
        playSimpleGame(user1, user2, "WHITE", "WHITE");
        playSimpleGame(user2, user1, "WHITE", "WHITE");
        playSimpleGame(user1, user3, "BLACK", "BLACK");
        playSimpleGame(user1, user2, "BLACK", "BLACK");
        Date end = new Date();
        Thread.sleep(2000);
        playSimpleGame(user1, user2, "BLACK", "BLACK");
        GameUtils.searchGames(user1, user2.getUserId(), start, end, "COMPLETE", null, "3", "ME", "BLACK", client, HttpStatus.SC_OK, 1);
    }

    @Test(groups = {"parallel"})
    public void listGames_ValidOpponents_200ArrayWithOpponents() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user3, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user3, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user3, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user3, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchGames(user1, user3.getUserId(), null, null, null, null, null, null, null, client, HttpStatus.SC_OK, 1);
    }

    @Test(groups = {"parallel"})
    public void listGames_InvalidOpponents_404EmptyArray() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = new User(test + "03", "password");
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchGames(user1, user3.getUserId(), null, null, null, null, null, null, null, client, HttpStatus.SC_NOT_FOUND, 0);
    }

    @Test(groups = {"parallel"})
    public void listGames_InvalidAndValidOpponents_404ArrayWithValid() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = new User(test + "03", "password");
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchGames(user1, user3.getUserId() + "," + user2.getUserId(), null, null, null, null, null, null, null, client, HttpStatus.SC_NOT_FOUND, 1);
    }

    @Test(groups = {"parallel"})
    public void listGames_OpponentWithNoGame_200EmptyArray() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchGames(user1, user2.getUserId(), null, null, null, null, null, null, null, client, HttpStatus.SC_OK, 0);
    }

    @Test(groups = {"parallel"})
    public void listGames_StartInPast_200GamesFromPastOn() throws Exception, InterruptedException {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        User user4 = AccountUtils.addUser(test, "04", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user3, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user3, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user4, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user4, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        Date start = new Date();
        Thread.sleep(2000);
        GameUtils.requestGame(user1, user3, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user3, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user4, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user4, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchGames(user1, null, start, null, null, null, null, null, null, client, HttpStatus.SC_OK, 2);
    }

    @Test(groups = {"parallel"})
    public void listGames_StartInCurrent_200EmptyArray() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        Date start = new Date();
        GameUtils.searchGames(user1, null, start, null, null, null, null, null, null, client, HttpStatus.SC_OK, 0);
    }

    @Test(groups = {"parallel"})
    public void listGames_StartInFuture_400() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        GameUtils.searchGames(user, null, calendar.getTime(), null, null, null, null, null, null, client, HttpStatus.SC_BAD_REQUEST, 0);
    }

    @Test(groups = {"parallel"})
    public void listGames_EndTimeNormal_200GamesBeforeTime() throws Exception, InterruptedException {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        User user4 = AccountUtils.addUser(test, "04", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user3, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user3, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user4, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user4, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        Date end = new Date();
        Thread.sleep(2000);
        GameUtils.requestGame(user1, user3, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user3, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user4, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user4, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchGames(user1, null, null, end, null, null, null, null, null, client, HttpStatus.SC_OK, 1);
    }

    @Test(groups = {"parallel"})
    public void listGames_EndTimeBeforeAll_200EmptyArray() throws Exception, InterruptedException {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        Date end = new Date();
        Thread.sleep(2000);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchGames(user1, null, null, end, null, null, null, null, null, client, HttpStatus.SC_OK, 0);
    }

    @Test(groups = {"parallel"})
    public void listGames_EndTimeInFuture_400() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        GameUtils.searchGames(user, null, null, calendar.getTime(), null, null, null, null, null, client, HttpStatus.SC_BAD_REQUEST, 0);
    }

    @Test(groups = {"parallel"})
    public void listGames_StartAndEnd_200GamesBetween() throws Exception, InterruptedException {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        User user4 = AccountUtils.addUser(test, "04", "password", client, HttpStatus.SC_NO_CONTENT);
        User user5 = AccountUtils.addUser(test, "05", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user3, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user3, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user4, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user4, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user5, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user5, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        Date start = new Date();
        Thread.sleep(2000);
        GameUtils.requestGame(user1, user3, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user3, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user4, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user4, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        Date end = new Date();
        Thread.sleep(2000);
        GameUtils.requestGame(user1, user5, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user5, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchGames(user1, null, start, end, null, null, null, null, null, client, HttpStatus.SC_OK, 2);
    }

    @Test(groups = {"parallel"})
    public void listGames_StartAfterEnd_400() throws Exception, InterruptedException {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        Date end = new Date();
        Thread.sleep(2000);
        Date start = new Date();
        GameUtils.searchGames(user1, null, start, end, null, null, null, null, null, client, HttpStatus.SC_BAD_REQUEST, 0);
    }

    @Test(groups = {"parallel"})
    public void listGames_CompleteGames_200OnlyComplete() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        User user4 = AccountUtils.addUser(test, "04", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user3, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user3, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user4, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user4, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        playSimpleGame(user1, user3, "WHITE", "WHITE");
        playSimpleGame(user1, user4, "WHITE", "WHITE");
        GameUtils.searchGames(user1, null, null, null, "COMPLETE", null, null, null, null, client, HttpStatus.SC_OK, 2);
    }

    @Test(groups = {"parallel"})
    public void listGames_IncompleteGames_200OnlyIncomplete() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        User user4 = AccountUtils.addUser(test, "04", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user3, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user3, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user4, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user4, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        playSimpleGame(user1, user3, "WHITE", "WHITE");
        playSimpleGame(user1, user4, "WHITE", "WHITE");
        GameUtils.searchGames(user1, null, null, null, "INCOMPLETE", null, null, null, null, client, HttpStatus.SC_OK, 1);
    }

    @Test(groups = {"parallel"})
    public void listGames_InvalidCompleteGames_400() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchGames(user, null, null, null, "YES", null, null, null, null, client, HttpStatus.SC_BAD_REQUEST, 0);
    }

    @Test(groups = {"parallel"})
    public void listGames_PendingGames_200OnlyPending() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        User user4 = AccountUtils.addUser(test, "04", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user3, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user3, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user4, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user4, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user3, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user3, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user4, 5, "WHITE", "BLACK", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user4, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchGames(user1, null, null, null, null, "PENDING", null, null, null, client, HttpStatus.SC_OK, 2);
    }

    @Test(groups = {"parallel"})
    public void listGames_NotPendingGames_200OnlyNotPending() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        User user4 = AccountUtils.addUser(test, "04", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user3, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user3, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user4, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user4, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user3, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user3, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user4, 5, "WHITE", "BLACK", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user4, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchGames(user1, null, null, null, null, "NOT_PENDING", null, null, null, client, HttpStatus.SC_OK, 1);
    }

    @Test(groups = {"parallel"})
    public void listGames_InvalidPendingGames_400() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchGames(user, null, null, null, null, "YES", null, null, null, client, HttpStatus.SC_BAD_REQUEST, 0);
    }

    @Test(groups = {"parallel"})
    public void listGames_WinnerGames_200OnlyWinner() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        User user4 = AccountUtils.addUser(test, "04", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user3, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user3, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user4, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user4, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        playSimpleGame(user1, user2, "WHITE", "WHITE");
        playSimpleGame(user1, user3, "WHITE", "WHITE");
        playSimpleGame(user4, user1, "WHITE", "WHITE");
        GameUtils.searchGames(user1, null, null, null, null, null, null, "ME", null, client, HttpStatus.SC_OK, 2);
    }

    @Test(groups = {"parallel"})
    public void listGames_NotWinnerGames_200OnlyNotWinner() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        User user4 = AccountUtils.addUser(test, "04", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user3, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user3, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user4, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user4, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        playSimpleGame(user1, user2, "WHITE", "WHITE");
        playSimpleGame(user1, user3, "WHITE", "WHITE");
        playSimpleGame(user4, user1, "WHITE", "WHITE");
        GameUtils.searchGames(user1, null, null, null, null, null, null, "THEM", null, client, HttpStatus.SC_OK, 1);
    }

    @Test(groups = {"parallel"})
    public void listGames_InvalidWinnerGames_400() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchGames(user, null, null, null, null, null, null, "NO_ONE", null, client, HttpStatus.SC_BAD_REQUEST, 0);
    }

    @Test(groups = {"parallel"})
    public void listGames_WhiteGames_200OnlyWhite() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        User user4 = AccountUtils.addUser(test, "04", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user3, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user3, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user4, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user4, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user3, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user3, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user4, 5, "BLACK", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user4, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchGames(user1, null, null, null, null, null, null, null, "WHITE", client, HttpStatus.SC_OK, 2);
    }

    @Test(groups = {"parallel"})
    public void listGames_BlackGames_200OnlyBlack() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        User user4 = AccountUtils.addUser(test, "04", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user3, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user3, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user4, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user4, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user3, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user3, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user4, 5, "BLACK", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user4, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchGames(user1, null, null, null, null, null, null, null, "BLACK", client, HttpStatus.SC_OK, 1);
    }

    @Test(groups = {"parallel"})
    public void listGames_InvalidColorGames_400() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchGames(user, null, null, null, null, null, null, null, "GRAY", client, HttpStatus.SC_BAD_REQUEST, 0);
    }

    @Test(groups = {"parallel"})
    public void listGames_OneSize_200GamesWithSize() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        User user4 = AccountUtils.addUser(test, "04", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user3, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user3, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user4, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user4, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user3, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user3, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user4, 3, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user4, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchGames(user1, null, null, null, null, null, "5", null, null, client, HttpStatus.SC_OK, 2);
    }

    @Test(groups = {"parallel"})
    public void listGames_MultipleSizes_200GamesWithAllSize() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        User user4 = AccountUtils.addUser(test, "04", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user3, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user3, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user4, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user4, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user3, 4, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user3, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user4, 3, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user4, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchGames(user1, null, null, null, null, null, "5,4", null, null, client, HttpStatus.SC_OK, 2);
    }

    @Test(groups = {"parallel"})
    public void listGames_SizeBadNumber_400() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchGames(user, null, null, null, null, null, "7", null, null, client, HttpStatus.SC_BAD_REQUEST, 0);
    }

    @Test(groups = {"parallel"})
    public void listGames_GoodAndBadSizes_400() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.searchGames(user, null, null, null, null, null, "7,5", null, null, client, HttpStatus.SC_BAD_REQUEST, 0);
    }

    @Test(groups = {"parallel"})
    public void listGames_InvalidUser_403() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = new User(test + "02", "password");
        GameUtils.searchAllGames(user2, client, HttpStatus.SC_UNAUTHORIZED, 0);
    }

    @Test(groups = {"parallel"})
    public void listGames_InvalidCredentials_403() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        user.setPassword("drowssap");
        GameUtils.searchAllGames(user, client, HttpStatus.SC_UNAUTHORIZED, 0);
    }

    //Get Info on a Game
    @Test(groups = {"parallel"})
    public void getGame_ValidGame_200GameInfo() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        String gameId = GameUtils.searchAllGames(user1, client, HttpStatus.SC_OK, 1).getJSONObject(0).getString("gameId");
        String turn = "ps a1";
        GameUtils.playTurn(user1, gameId, turn, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.getGame(user1, gameId, client, HttpStatus.SC_OK, user1, user2, new String[]{turn});
    }

    @Test(groups = {"parallel"})
    public void getGame_NotYourGame_404() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        String gameId = GameUtils.searchAllGames(user1, client, HttpStatus.SC_OK, 1).getJSONObject(0).getString("gameId");
        GameUtils.getGame(user3, gameId, client, HttpStatus.SC_NOT_FOUND, null, null, null);
    }

    @Test(groups = {"parallel"})
    public void getGame_NotRealGame_404() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.getGame(user1, ZERO_ID, client, HttpStatus.SC_NOT_FOUND, null, null, null);
    }

    @Test(groups = {"parallel"})
    public void getGame_InvalidUser_401() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = new User(test + "03", "password");
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        String gameId = GameUtils.searchAllGames(user1, client, HttpStatus.SC_OK, 1).getJSONObject(0).getString("gameId");
        String turn = "ps a1";
        GameUtils.playTurn(user1, gameId, turn, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.getGame(user3, gameId, client, HttpStatus.SC_UNAUTHORIZED, null, null, null);
    }

    @Test(groups = {"parallel"})
    public void getGame_InvalidCredentials_401() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        String gameId = GameUtils.searchAllGames(user1, client, HttpStatus.SC_OK, 1).getJSONObject(0).getString("gameId");
        String turn = "ps a1";
        GameUtils.playTurn(user1, gameId, turn, client, HttpStatus.SC_NO_CONTENT);
        user1.setPassword("drowssap");
        GameUtils.getGame(user1, gameId, client, HttpStatus.SC_UNAUTHORIZED, null, null, null);
    }

    @Test(groups = {"parallel"})
    public void getGame_TestFullState_200() throws Exception, TakEngineException {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        String gameId = GameUtils.searchAllGames(user1, client, HttpStatus.SC_OK, 1).getJSONObject(0).getString("gameId");
        String[] turns = new String[]{"ps a1", "ps a2", "pc c1", "pw c2", "ms c1 g1 1"};
        GameState state = new GameState(Player.WHITE, 5);
        boolean user1Turn = true;
        for(String turn : turns) {
            state.executeTurn(TurnUtils.turnFromString(turn));
            if(user1Turn) {
                GameUtils.playTurn(user1, gameId, turn, client, HttpStatus.SC_NO_CONTENT);
            }
            else {
                GameUtils.playTurn(user2, gameId, turn, client, HttpStatus.SC_NO_CONTENT);
            }
            user1Turn = !user1Turn;
        }
        JSONObject json = GameUtils.getGame(user1, gameId, true, client, HttpStatus.SC_OK, user1, user2, turns);
        JSONObject full = json.getJSONObject("fullState");
        GameState generated = new GameState(full);
        Assert.assertEquals(generated, state);
    }

    //Send game message
    @Test(groups = {"parallel"})
    public void sendGameMessage_Normal_204() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        String gameId = GameUtils.searchAllGames(user1, client, HttpStatus.SC_OK, 1).getJSONObject(0).getString("gameId");
        GameUtils.sendGameMessage(user1, gameId, "Message 1", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.sendGameMessage(user1, gameId, "Message 2", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.sendGameMessage(user2, gameId, "Message 3", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.sendGameMessage(user1, gameId, "Message 4", client, HttpStatus.SC_NO_CONTENT);
        JSONObject json = GameUtils.getGame(user1, gameId, true, client, HttpStatus.SC_OK, user1, user2, new String[]{});
        for(int i = 0; i < json.getJSONArray("messages").length(); i++) {
            Assert.assertEquals(json.getJSONArray("messages").getJSONObject(i).getString("message"), "Message " + (i + 1));
        }
    }

    @Test(groups = {"parallel"})
    public void sendGameMessage_SendMessageAfterGame_403() throws Exception, TakEngineException {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        String gameId = playSimpleGame(user1, user2, "WHITE", "WHITE");
        GameUtils.sendGameMessage(user1, gameId, "Message 1", client, HttpStatus.SC_FORBIDDEN);
        JSONObject json = GameUtils.getGame(user1, gameId, true, client, HttpStatus.SC_OK, user1, user2, null);
        Assert.assertEquals(json.getJSONArray("messages").length(), 0);
    }

    @Test(groups = {"parallel"})
    public void sendGameMessage_NotYourGame_404() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        String gameId = GameUtils.searchAllGames(user1, client, HttpStatus.SC_OK, 1).getJSONObject(0).getString("gameId");
        GameUtils.sendGameMessage(user3, gameId, "Message 1", client, HttpStatus.SC_NOT_FOUND);
        JSONObject json = GameUtils.getGame(user1, gameId, true, client, HttpStatus.SC_OK, user1, user2, new String[]{});
        Assert.assertEquals(json.getJSONArray("messages").length(), 0);
    }

    //Get Possible Next Turns For Game
    @Test(groups = {"parallel"})
    public void getPossibleTurns_YourTurn_200PossibleTurns() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        String gameId = GameUtils.searchAllGames(user1, client, HttpStatus.SC_OK, 1).getJSONObject(0).getString("gameId");
        GameUtils.getPossibleMoves(user1, gameId, client, HttpStatus.SC_OK, 25);
    }

    @Test(groups = {"parallel"})
    public void getPossibleTurns_TheirTurn_403() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        String gameId = GameUtils.searchAllGames(user1, client, HttpStatus.SC_OK, 1).getJSONObject(0).getString("gameId");
        GameUtils.getPossibleMoves(user2, gameId, client, HttpStatus.SC_FORBIDDEN, 0);
    }

    @Test(groups = {"parallel"})
    public void getPossibleTurns_FinishedGame_200Empty() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        String gameId = playSimpleGame(user1, user2, "WHITE", "WHITE");
        GameUtils.getPossibleMoves(user1, gameId, client, HttpStatus.SC_OK, 0);
        GameUtils.getPossibleMoves(user2, gameId, client, HttpStatus.SC_OK, 0);
    }

    @Test(groups = {"parallel"})
    public void getPossibleTurns_NotYourGame_404() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        String gameId = GameUtils.searchAllGames(user1, client, HttpStatus.SC_OK, 1).getJSONObject(0).getString("gameId");
        GameUtils.getPossibleMoves(user3, gameId, client, HttpStatus.SC_NOT_FOUND, 0);
    }

    @Test(groups = {"parallel"})
    public void getPossibleTurns_InvalidGame_404() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.getPossibleMoves(user1, ZERO_ID, client, HttpStatus.SC_NOT_FOUND, 0);
    }

    @Test(groups = {"parallel"})
    public void getPossibleTurns_InvalidUser_401() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = new User(test + "03", "password");
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        String gameId = GameUtils.searchAllGames(user1, client, HttpStatus.SC_OK, 1).getJSONObject(0).getString("gameId");
        GameUtils.getPossibleMoves(user3, gameId, client, HttpStatus.SC_UNAUTHORIZED, 0);
    }

    @Test(groups = {"parallel"})
    public void getPossibleTurns_InvalidCredential_401() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        String gameId = GameUtils.searchAllGames(user1, client, HttpStatus.SC_OK, 1).getJSONObject(0).getString("gameId");
        user1.setPassword("drowssap");
        GameUtils.getPossibleMoves(user1, gameId, client, HttpStatus.SC_UNAUTHORIZED, 0);
    }

    //Play Turn
    @Test(groups = {"parallel"})
    public void playTurn_YourTurn_204TurnMadeConfirmationOfTurn() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        String gameId = GameUtils.searchGames(user1, user2.getUserId(), null, null, "INCOMPLETE", null, null, null, null, client, HttpStatus.SC_OK, 1).getJSONObject(0).getString("gameId");
        String turn = "ps b1";
        GameUtils.playTurn(user1, gameId, turn, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.getGame(user1, gameId, client, HttpStatus.SC_OK, user1, user2, new String[]{turn});
    }

    @Test(groups = {"parallel"})
    public void playTurn_NotYourTurn_403TurnNotMade() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        String gameId = GameUtils.searchGames(user1, user2.getUserId(), null, null, "INCOMPLETE", null, null, null, null, client, HttpStatus.SC_OK, 1).getJSONObject(0).getString("gameId");
        String turn = "ps b1";
        GameUtils.playTurn(user2, gameId, turn, client, HttpStatus.SC_FORBIDDEN);
        GameUtils.getGame(user1, gameId, client, HttpStatus.SC_OK, user1, user2, new String[]{});
    }

    @Test(groups = {"parallel"})
    public void playTurn_NotYourGame_404TurnNotMade() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        String gameId = GameUtils.searchGames(user1, user2.getUserId(), null, null, "INCOMPLETE", null, null, null, null, client, HttpStatus.SC_OK, 1).getJSONObject(0).getString("gameId");
        String turn = "ps b1";
        GameUtils.playTurn(user3, gameId, turn, client, HttpStatus.SC_NOT_FOUND);
        GameUtils.getGame(user1, gameId, client, HttpStatus.SC_OK, user1, user2, new String[]{});
    }

    @Test(groups = {"parallel"})
    public void playTurn_InvalidGame_404() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.playTurn(user, ZERO_ID, "ps b1", client, HttpStatus.SC_NOT_FOUND);
    }

    @Test(groups = {"parallel"})
    public void playTurn_IllegalTurn_403TurnNotMade() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        String gameId = GameUtils.searchGames(user1, user2.getUserId(), null, null, "INCOMPLETE", null, null, null, null, client, HttpStatus.SC_OK, 1).getJSONObject(0).getString("gameId");
        String turn = "ps b1";
        GameUtils.playTurn(user1, gameId, turn, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.playTurn(user2, gameId, turn, client, HttpStatus.SC_FORBIDDEN);
        GameUtils.getGame(user1, gameId, client, HttpStatus.SC_OK, user1, user2, new String[]{turn});
    }

    @Test(groups = {"parallel"})
    public void playTurn_IllFormattedTurn_400TurnNotMade() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        String gameId = GameUtils.searchGames(user1, user2.getUserId(), null, null, "INCOMPLETE", null, null, null, null, client, HttpStatus.SC_OK, 1).getJSONObject(0).getString("gameId");
        String turn = "ps b1";
        GameUtils.playTurn(user1, gameId, turn, client, HttpStatus.SC_NO_CONTENT);
        GameUtils.playTurn(user2, gameId, "win game", client, HttpStatus.SC_BAD_REQUEST);
        GameUtils.getGame(user1, gameId, client, HttpStatus.SC_OK, user1, user2, new String[]{turn});
    }

    @Test(groups = {"parallel"})
    public void playTurn_WinGame_204GameMarkedWin() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        playSimpleGame(user1, user2, "WHITE", "WHITE");
        GameUtils.searchGames(user1, null, null, null, "COMPLETE", null, null, null, null, client, HttpStatus.SC_OK, 1);
    }

    @Test(groups = {"parallel"})
    public void playTurn_InvalidUser_401TurnNotMade() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = new User(test + "03", "password");
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        String gameId = GameUtils.searchGames(user1, user2.getUserId(), null, null, "INCOMPLETE", null, null, null, null, client, HttpStatus.SC_OK, 1).getJSONObject(0).getString("gameId");
        String turn = "ps b1";
        GameUtils.playTurn(user3, gameId, turn, client, HttpStatus.SC_UNAUTHORIZED);
        GameUtils.getGame(user1, gameId, client, HttpStatus.SC_OK, user1, user2, new String[]{});
    }

    @Test(groups = {"parallel"})
    public void playTurn_InvalidCredentials_401TurnNotMade() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        String gameId = GameUtils.searchGames(user1, user2.getUserId(), null, null, "INCOMPLETE", null, null, null, null, client, HttpStatus.SC_OK, 1).getJSONObject(0).getString("gameId");
        String turn = "ps b1";
        user1.setPassword("drowssap");
        GameUtils.playTurn(user1, gameId, turn, client, HttpStatus.SC_UNAUTHORIZED);
        user1.setPassword("password");
        GameUtils.getGame(user1, gameId, client, HttpStatus.SC_OK, user1, user2, new String[]{});
    }

    @Test(groups = {"parallel"})
    public void playTurn_CheckRatingAfterGame_200RatingsAccurate() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        Assert.assertEquals(AccountUtils.seachUsers(user1.getUsername(), null, client, HttpStatus.SC_OK).getRating(), 1000);
        Assert.assertEquals(AccountUtils.seachUsers(user2.getUsername(), null, client, HttpStatus.SC_OK).getRating(), 1000);
        playSimpleGame(user1, user2, "WHITE", "WHITE");
        Assert.assertEquals(AccountUtils.seachUsers(user1.getUsername(), null, client, HttpStatus.SC_OK).getRating(), 1010);
        Assert.assertEquals(AccountUtils.seachUsers(user2.getUsername(), null, client, HttpStatus.SC_OK).getRating(), 990);
        playSimpleGame(user1, user2, "WHITE", "WHITE");
        Assert.assertEquals(AccountUtils.seachUsers(user1.getUsername(), null, client, HttpStatus.SC_OK).getRating(), 1019);
        Assert.assertEquals(AccountUtils.seachUsers(user2.getUsername(), null, client, HttpStatus.SC_OK).getRating(), 981);
        playSimpleGame(user2, user1, "WHITE", "WHITE");
        Assert.assertEquals(AccountUtils.seachUsers(user1.getUsername(), null, client, HttpStatus.SC_OK).getRating(), 1008);
        Assert.assertEquals(AccountUtils.seachUsers(user2.getUsername(), null, client, HttpStatus.SC_OK).getRating(), 992);
    }

    @Test(groups = {"parallel"})
    public void playTurn_PlayAiYourTurnFirst_204NormalGame() throws Exception, InterruptedException {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user, new User("AI", "AI", "AI"), 3, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        JSONArray games = GameUtils.searchGames(user, "AI", null, null, "INCOMPLETE", null, null, null, null, client, HttpStatus.SC_OK, 1);
        Assert.assertEquals(games.length(), 1);
        String gameId = games.getJSONObject(0).getString("gameId");
        GameUtils.playTurn(user, gameId, "ps a1", client, HttpStatus.SC_NO_CONTENT);
        waitForAi(user);
        GameUtils.checkGameNotifications(user, client, HttpStatus.SC_OK, 0, 1);
        JSONObject game = GameUtils.getGame(user, gameId, false, client, HttpStatus.SC_OK, user, new User("AI", "AI", "AI"), null);
        Assert.assertEquals(game.getJSONArray("turns").length(), 2);
    }

    @Test(groups = {"parallel"})
    public void playTurn_PlayAiTheirTurnFirst_204NormalGame() throws Exception, InterruptedException {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user, new User("AI", "AI", "AI"), 3, "WHITE", "BLACK", client, HttpStatus.SC_NO_CONTENT);
        JSONArray games = GameUtils.searchGames(user, "AI", null, null, "INCOMPLETE", null, null, null, null, client, HttpStatus.SC_OK, 1);
        Assert.assertEquals(games.length(), 1);
        String gameId = games.getJSONObject(0).getString("gameId");
        waitForAi(user);
        GameUtils.checkGameNotifications(user, client, HttpStatus.SC_OK, 0, 1);
        JSONObject game = GameUtils.getGame(user, gameId, false, client, HttpStatus.SC_OK, user, new User("AI", "AI", "AI"), null);
        if(game.getJSONArray("turns").getString(0).equalsIgnoreCase("ps a1")) {
            GameUtils.playTurn(user, gameId, "ps a2", client, HttpStatus.SC_NO_CONTENT);
        }
        else {
            GameUtils.playTurn(user, gameId, "ps a1", client, HttpStatus.SC_NO_CONTENT);
        }
        waitForAi(user);
        GameUtils.checkGameNotifications(user, client, HttpStatus.SC_OK, 0, 1);
        game = GameUtils.getGame(user, gameId, false, client, HttpStatus.SC_OK, user, new User("AI", "AI", "AI"), null);
        Assert.assertEquals(game.getJSONArray("turns").length(), 3);
    }

    private boolean waitForAi(User user) throws InterruptedException, Exception {
        //Wait for up to 20 seconds
        for(int i = 0; i < 40; i++) {
            Thread.sleep(500);
            if(GameUtils.searchGames(user, "AI", null, null, "INCOMPLETE", "PENDING", null, null, null, client, HttpStatus.SC_OK, -1).length() == 1) {
                return true;
            }
        }

        return false;
    }

    //Get Notifications
    @Test(groups = {"parallel"})
    public void getNotifications_NoRequests_200RequestsFieldZero() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.checkGameNotifications(user1, client, HttpStatus.SC_OK, 0, 0);
    }

    @Test(groups = {"parallel"})
    public void getNotifications_NonZeroRequests_200RequestsFieldMoreThanZero() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.checkGameNotifications(user2, client, HttpStatus.SC_OK, 1, 0);
    }

    @Test(groups = {"parallel"})
    public void getNotifications_NoGames_200YourTurnFieldZero() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.checkGameNotifications(user2, client, HttpStatus.SC_OK, 0, 0);
    }

    @Test(groups = {"parallel"})
    public void getNotifications_GamesNoneYourTurn_200YourTurnFieldZero() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.checkGameNotifications(user2, client, HttpStatus.SC_OK, 0, 0);
    }

    @Test(groups = {"parallel"})
    public void getNotifications_GamesSomeYourTurn_200YourTurnFieldOnlyYourTurn() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = AccountUtils.addUser(test, "02", "password", client, HttpStatus.SC_NO_CONTENT);
        User user3 = AccountUtils.addUser(test, "03", "password", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user2, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user2, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.requestFriend(user1, user3, client, HttpStatus.SC_NO_CONTENT);
        SocialUtils.respondToRequest(user3, user1, "accept", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user1, user2, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user2, user1, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.requestGame(user3, user1, 5, "WHITE", "WHITE", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.respondToGameRequest(user1, user3, "ACCEPT", client, HttpStatus.SC_NO_CONTENT);
        GameUtils.checkGameNotifications(user1, client, HttpStatus.SC_OK, 0, 1);
    }

    @Test(groups = {"parallel"})
    public void getNotifications_InvalidUser_401() throws Exception {
        String test = getTest();
        User user1 = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        User user2 = new User(test + "02", "password");
        GameUtils.checkGameNotifications(user2, client, HttpStatus.SC_UNAUTHORIZED, 0, 0);
    }

    @Test(groups = {"parallel"})
    public void getNotifications_InvalidCredential_401() throws Exception {
        String test = getTest();
        User user = AccountUtils.addUser(test, "01", "password", client, HttpStatus.SC_NO_CONTENT);
        user.setPassword("drowssap");
        GameUtils.checkGameNotifications(user, client, HttpStatus.SC_UNAUTHORIZED, 0, 0);
    }
}
