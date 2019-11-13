package io.joshatron.bgt.server.logic.utils;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.json.JSONObject;
import org.testng.Assert;

public class AccountUtils {

    public final static int TRIES_TO_LOCK = 5;

    public static User addUser(String test, String user, String password, HttpClient client, int expected) throws Exception {
        Response response;
        if(test == null || user == null) {
            response = HttpUtils.createUser(null, password, client);
        }
        else {
            response = HttpUtils.createUser(test + user, password, client);
        }
        Assert.assertEquals(response.getStatus(), expected);
        if(expected == HttpStatus.SC_OK) {
            UserInfo info = seachUsers(test + user, null, client, HttpStatus.SC_OK);
            User u = new User(test + user, password, info.getUserId());
            authenticate(u, client, HttpStatus.SC_NO_CONTENT);

            return u;
        }
        else {
            return null;
        }
    }

    public static void changeUsername(User user, String newName, HttpClient client, int expected) throws Exception {
        Response response;
        if(user != null) {
            response = HttpUtils.changeUsername(user.getUsername(), user.getPassword(), newName, client);
        }
        else {
            response = HttpUtils.changeUsername(null, null, newName, client);
        }
        Assert.assertEquals(response.getStatus(), expected);
        if(expected == HttpStatus.SC_NO_CONTENT) {
            user.setUsername(newName);
            authenticate(user, client, HttpStatus.SC_NO_CONTENT);
        }
    }

    public static void changePassword(User user, String newPass, HttpClient client, int expected) throws Exception {
        Response response;
        if(user != null) {
            response = HttpUtils.changePassword(user.getUsername(), user.getPassword(), newPass, client);
        }
        else {
            response = HttpUtils.changePassword(null, null, newPass, client);
        }
        Assert.assertEquals(response.getStatus(), expected);
        if(expected == HttpStatus.SC_NO_CONTENT) {
            user.setPassword(newPass);
            authenticate(user, client, HttpStatus.SC_NO_CONTENT);
        }
    }

    public static void authenticate(User user, HttpClient client, int expected) throws Exception {
        Response response;
        if(user != null) {
            response = HttpUtils.authenticate(user.getUsername(), user.getPassword(), client);
        }
        else {
            response = HttpUtils.authenticate(null, null, client);
        }
        Assert.assertEquals(response.getStatus(), expected);
    }

    public static void authenticate(User user, HttpClient client, int expected, String exception) throws Exception {
        Response response;
        if(user != null) {
            response = HttpUtils.authenticate(user.getUsername(), user.getPassword(), client);
        }
        else {
            response = HttpUtils.authenticate(null, null, client);
        }
        Assert.assertEquals(response.getStatus(), expected);
        Assert.assertTrue(response.getContents().contains(exception));
    }

    public static UserInfo seachUsers(String username, String userId, HttpClient client, int expected) throws Exception {
        Response response;
        response = HttpUtils.searchUser(username, userId, client);
        Assert.assertEquals(response.getStatus(), expected);

        if(response.getStatus() == HttpStatus.SC_OK) {
            JSONObject json = new JSONObject(response.getContents());

            UserInfo info = new UserInfo(json.getString("username"), json.getString("userId"), json.getInt("rating"));

            if(username != null) {
                Assert.assertEquals(info.getUsername(), username);
            }
            else {
                Assert.assertNotNull(info.getUsername());
            }

            if(userId != null) {
                Assert.assertEquals(info.getUserId(), userId);
            }
            else {
                Assert.assertEquals(info.getUserId().length(), 36);
            }

            return info;
        }
        else {
            return null;
        }
    }
}
