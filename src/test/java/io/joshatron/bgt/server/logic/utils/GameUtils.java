package io.joshatron.bgt.server.logic.utils;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;

import java.util.Date;

public class GameUtils {

    public static void requestGame(User requester, User other, Integer size, String requesterColor, String first, HttpClient client, int expected) throws Exception {
        Response response = HttpUtils.requestGame(requester.getUsername(), requester.getPassword(), other.getUserId(), size, requesterColor, first, client);
        Assert.assertEquals(response.getStatus(), expected);
    }

    public static void deleteGameRequest(User requester, User other, HttpClient client, int expected) throws Exception {
        Response response = HttpUtils.deleteGameRequest(requester.getUsername(), requester.getPassword(), other.getUserId(), client);
        Assert.assertEquals(response.getStatus(), expected);
    }

    public static void respondToGameRequest(User requester, User other, String answer, HttpClient client, int expected) throws Exception {
        Response response = HttpUtils.respondToGameRequest(requester.getUsername(), requester.getPassword(), other.getUserId(), answer, client);
        Assert.assertEquals(response.getStatus(), expected);
    }

    public static void checkIncoming(User user, HttpClient client, int expected, User[] included, User[] excluded) throws Exception {
        Response response = HttpUtils.checkIncomingGameRequests(user.getUsername(), user.getPassword(), client);
        Assert.assertEquals(response.getStatus(), expected);
        if(response.getStatus() == HttpStatus.SC_OK) {
            String contents = response.getContents();
            if (included != null && included.length > 0) {
                for (User include : included) {
                    //Makes sure there is just one occurrence
                    Assert.assertEquals((contents.length() - contents.replace(include.getUserId(), "").length()) / 36, 1);
                }
            }
            if (excluded != null && excluded.length > 0) {
                for (User exclude : excluded) {
                    Assert.assertFalse(contents.contains(exclude.getUserId()));
                }
            }
        }
    }

    public static void checkOutgoing(User user, HttpClient client, int expected, User[] included, User[] excluded) throws Exception {
        Response response = HttpUtils.checkOutgoingGameRequests(user.getUsername(), user.getPassword(), client);
        Assert.assertEquals(response.getStatus(), expected);
        if(response.getStatus() == HttpStatus.SC_OK) {
            String contents = response.getContents();
            if (included != null && included.length > 0) {
                for (User include : included) {
                    //Makes sure there is just one occurrence
                    Assert.assertEquals((contents.length() - contents.replace(include.getUserId(), "").length()) / 36, 1);
                }
            }
            if (excluded != null && excluded.length > 0) {
                for (User exclude : excluded) {
                    Assert.assertFalse(contents.contains(exclude.getUserId()));
                }
            }
        }
    }

    public static void requestRandomGame(User requester, int size, HttpClient client, int expected) throws Exception {
        Response response = HttpUtils.requestRandomGame(requester.getUsername(), requester.getPassword(), size, client);
        Assert.assertEquals(response.getStatus(), expected);
    }

    public static void deleteRandomGameRequest(User requester, HttpClient client, int expected) throws Exception {
        Response response = HttpUtils.deleteRandomGameRequest(requester.getUsername(), requester.getPassword(), client);
        Assert.assertEquals(response.getStatus(), expected);
    }

    public static void rawDeleteRandomGameRequest(User requester, HttpClient client) throws Exception {
        HttpUtils.deleteRandomGameRequest(requester.getUsername(), requester.getPassword(), client);
    }

    public static void getRandomRequestSize(User requester, HttpClient client, int expected, int expectedSize) throws Exception {
        Response response = HttpUtils.getRandomRequestSize(requester.getUsername(), requester.getPassword(), client);
        Assert.assertEquals(response.getStatus(), expected);
        if(response.getStatus() == HttpStatus.SC_OK) {
            String contents = response.getContents();
            Assert.assertTrue(contents.contains(Integer.toString(expectedSize)));
        }
    }

    public static JSONArray searchGames(User requester, String opponents, Date start, Date end, String complete, String pending, String sizes, String winner, String color, HttpClient client, int expected, int numExpected) throws Exception {
        Response response = HttpUtils.searchGames(requester.getUsername(), requester.getPassword(), opponents, start, end, complete, pending, sizes, winner, color, client);
        Assert.assertEquals(response.getStatus(), expected);
        if(response.getStatus() == HttpStatus.SC_OK) {
            String contents = response.getContents();
            if(numExpected >= 0) {
                JSONArray array = new JSONArray(contents);
                Assert.assertEquals(array.length(), numExpected);
            }

            return new JSONArray(contents);
        }

        return null;
    }

    public static JSONArray searchAllGames(User requester, HttpClient client, int expected, int numExpected) throws Exception {
        return searchGames(requester, null, null, null, null, null, null, null, null,
                           client, expected, numExpected);
    }

    public static void getGame(User requester, String gameId, HttpClient client, int expected, User expectedWhite, User expectedBlack, String[] expectedTurns) throws Exception {
        getGame(requester, gameId, false, client, expected, expectedWhite, expectedBlack, expectedTurns);
    }

    public static JSONObject getGame(User requester, String gameId, boolean full, HttpClient client, int expected, User expectedWhite, User expectedBlack, String[] expectedTurns) throws Exception {
        Response response = HttpUtils.getGame(requester.getUsername(), gameId, requester.getPassword(), full, client);
        Assert.assertEquals(response.getStatus(), expected);
        if(response.getStatus() == HttpStatus.SC_OK) {
            JSONObject json = new JSONObject(response.getContents());
            Assert.assertEquals(json.getString("white"), expectedWhite.getUserId());
            Assert.assertEquals(json.getString("black"), expectedBlack.getUserId());
            if(expectedTurns != null) {
                JSONArray array = json.getJSONArray("turns");
                Assert.assertEquals(array.length(), expectedTurns.length);
                for (int i = 0; i < expectedTurns.length; i++) {
                    Assert.assertEquals(array.getString(i), expectedTurns[i]);
                }
            }

            return json;
        }

        return null;
    }

    public static void getPossibleMoves(User requester, String gameId, HttpClient client, int expected, int numExpected) throws Exception {
        Response response = HttpUtils.getPossibleMoves(requester.getUsername(), gameId, requester.getPassword(), client);
        Assert.assertEquals(response.getStatus(), expected);
        if(response.getStatus() == HttpStatus.SC_OK) {
            if(numExpected >= 0) {
                JSONArray array = new JSONArray(response.getContents());
                Assert.assertEquals(array.length(), numExpected);
            }
        }
    }

    public static void playTurn(User requester, String gameId, String turn, HttpClient client, int expected) throws Exception {
        Response response = HttpUtils.playTurn(requester.getUsername(), gameId, turn, requester.getPassword(), client);
        Assert.assertEquals(response.getStatus(), expected);
    }

    public static void checkGameNotifications(User user, HttpClient client, int expected, int expectedRequests, int expectedGames) throws Exception {
        Response response = HttpUtils.getGameNotifications(user.getUsername(), user.getPassword(), client);
        Assert.assertEquals(expected, response.getStatus());
        if(response.getStatus() == HttpStatus.SC_OK) {
            JSONObject json = new JSONObject(response.getContents());
            Assert.assertEquals(json.getInt("incomingRequests"), expectedRequests);
            Assert.assertEquals(json.getInt("pendingGames"), expectedGames);
        }
    }

    public static void sendGameMessage(User requester, String gameId, String message, HttpClient client, int expected) throws Exception {
        Response response = HttpUtils.sendGameMessage(requester.getUsername(), gameId, message, requester.getPassword(), client);
        Assert.assertEquals(response.getStatus(), expected);
    }
}
