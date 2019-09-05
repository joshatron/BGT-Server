package io.joshatron.bgt.server.logic.utils;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;

import java.util.Date;

public class SocialUtils {

    public static void requestFriend(User requester, User other, HttpClient client, int expected) throws Exception {
        Response response = HttpUtils.requestFriend(requester.getUsername(), requester.getPassword(), other.getUserId(), client);
        Assert.assertEquals(response.getStatus(), expected);
    }

    public static void cancelRequest(User requester, User other, HttpClient client, int expected) throws Exception {
        Response response = HttpUtils.cancelFriendRequest(requester.getUsername(), requester.getPassword(), other.getUserId(), client);
        Assert.assertEquals(response.getStatus(), expected);
    }

    public static void respondToRequest(User user, User other, String answer, HttpClient client, int expected) throws Exception {
        Response response = HttpUtils.respondToRequest(user.getUsername(), user.getPassword(), other.getUserId(), answer, client);
        Assert.assertEquals(response.getStatus(), expected);
    }

    public static void checkIncoming(User user, HttpClient client, int expected, User[] included, User[] excluded) throws Exception {
        Response response = HttpUtils.checkIncomingFriendRequests(user.getUsername(), user.getPassword(), client);
        Assert.assertEquals(response.getStatus(), expected);
        if(response.getStatus() == HttpStatus.SC_OK) {
            String contents = response.getContents();
            if (included != null && included.length > 0) {
                for (User include : included) {
                    //Makes sure there is just one occurrence
                    Assert.assertEquals((contents.length() - contents.replace(include.getUsername(), "").length()) / include.getUsername().length(), 1);
                }
            }
            if (excluded != null && excluded.length > 0) {
                for (User exclude : excluded) {
                    Assert.assertFalse(contents.contains(exclude.getUsername()));
                }
            }
        }
    }

    public static void checkOutgoing(User user, HttpClient client, int expected, User[] included, User[] excluded) throws Exception {
        Response response = HttpUtils.checkOutgoingFriendRequests(user.getUsername(), user.getPassword(), client);
        Assert.assertEquals(response.getStatus(), expected);
        if(response.getStatus() == HttpStatus.SC_OK) {
            String contents = response.getContents();
            if (included != null && included.length > 0) {
                for (User include : included) {
                    //Makes sure there is just one occurrence
                    Assert.assertEquals((contents.length() - contents.replace(include.getUsername(), "").length()) / include.getUsername().length(), 1);
                }
            }
            if (excluded != null && excluded.length > 0) {
                for (User exclude : excluded) {
                    Assert.assertFalse(contents.contains(exclude.getUsername()));
                }
            }
        }
    }

    public static void unfriendUser(User requester, User other, HttpClient client, int expected) throws Exception {
        Response response = HttpUtils.unfriendUser(requester.getUsername(), requester.getPassword(), other.getUserId(), client);
        Assert.assertEquals(response.getStatus(), expected);
    }

    public static void blockUser(User requester, User other, HttpClient client, int expected) throws Exception {
        Response response = HttpUtils.blockUser(requester.getUsername(), requester.getPassword(), other.getUserId(), client);
        Assert.assertEquals(response.getStatus(), expected);
    }

    public static void unblockUser(User requester, User other, HttpClient client, int expected) throws Exception {
        Response response = HttpUtils.unblockUser(requester.getUsername(), requester.getPassword(), other.getUserId(), client);
        Assert.assertEquals(response.getStatus(), expected);
    }

    public static void checkIfBlocked(User requester, User other, HttpClient client, int expected) throws Exception {
        Response response = HttpUtils.checkIfBlocked(requester.getUsername(), requester.getPassword(), other.getUserId(), client);
        Assert.assertEquals(response.getStatus(), expected);
    }

    public static void checkFriends(User user, HttpClient client, int expected, User[] included, User[] excluded) throws Exception {
        Response response = HttpUtils.getFriends(user.getUsername(), user.getPassword(), client);
        Assert.assertEquals(response.getStatus(), expected);
        if(response.getStatus() == HttpStatus.SC_OK) {
            String contents = response.getContents();
            if (included != null && included.length > 0) {
                for (User include : included) {
                    //Makes sure there is just one occurrence
                    Assert.assertEquals((contents.length() - contents.replace(include.getUsername(), "").length()) / include.getUsername().length(), 1);
                }
            }
            if (excluded != null && excluded.length > 0) {
                for (User exclude : excluded) {
                    Assert.assertFalse(contents.contains(exclude.getUsername()));
                }
            }
        }
    }

    public static void checkBlocking(User user, HttpClient client, int expected, User[] included, User[] excluded) throws Exception {
        Response response = HttpUtils.getBlocked(user.getUsername(), user.getPassword(), client);
        Assert.assertEquals(response.getStatus(), expected);
        if(response.getStatus() == HttpStatus.SC_OK) {
            String contents = response.getContents();
            if (included != null && included.length > 0) {
                for (User include : included) {
                    //Makes sure there is just one occurrence
                    Assert.assertEquals((contents.length() - contents.replace(include.getUsername(), "").length()) / include.getUsername().length(), 1);
                }
            }
            if (excluded != null && excluded.length > 0) {
                for (User exclude : excluded) {
                    Assert.assertFalse(contents.contains(exclude.getUsername()));
                }
            }
        }
    }

    public static void sendMessage(User sender, User reciever, String message, HttpClient client, int expected) throws Exception {
        Response response = HttpUtils.sendMessage(sender.getUsername(), sender.getPassword(), reciever.getUserId(), message, client);
        Assert.assertEquals(response.getStatus(), expected);
    }

    public static JSONArray searchMessages(User user, String senders, Date start, Date end, String read, String from, HttpClient client, int expected, int numExpected) throws Exception {
        Response response = HttpUtils.searchMessages(user.getUsername(), user.getPassword(), senders, start, end, read, from, client);
        Assert.assertEquals(response.getStatus(), expected);
        if(response.getStatus() == HttpStatus.SC_OK) {
            if(numExpected >= 0) {
                JSONArray array = new JSONArray(response.getContents());
                Assert.assertEquals(array.length(), numExpected);
                return array;
            }
        }

        return null;
    }

    public static JSONArray searchAllMessages(User user, HttpClient client, int expected, int numExpected) throws Exception {
        return searchMessages(user, null, null, null, null, null, client, expected, numExpected);
    }

    public static void checkSocialNotifications(User user, HttpClient client, int expected, int expectedRequests, int expectedMessages) throws Exception {
        Response response = HttpUtils.getSocialNotifications(user.getUsername(), user.getPassword(), client);
        Assert.assertEquals(response.getStatus(), expected);
        if(response.getStatus() == HttpStatus.SC_OK) {
            JSONObject json = new JSONObject(response.getContents());
            Assert.assertEquals(json.getInt("incomingRequests"), expectedRequests);
            Assert.assertEquals(json.getInt("incomingMessages"), expectedMessages);
        }
    }
}
