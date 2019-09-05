package io.joshatron.bgt.server.logic.utils;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.json.JSONObject;
import org.testng.Assert;

public class AdminUtils {

    public static String initialize(HttpClient client, int expected) throws Exception {
        Response response = HttpUtils.initializeAdminAccount(client);
        Assert.assertEquals(response.getStatus(), expected);
        //This is from the server default password
        return "password";
    }

    public static void changePassword(User user, String newPass, HttpClient client, int expected) throws Exception {
        Response response;
        if(user != null) {
            response = HttpUtils.changeAdminPassword(user.getUsername(), user.getPassword(), newPass, client);
        }
        else {
            response = HttpUtils.changeAdminPassword(null, null, newPass, client);
        }
        Assert.assertEquals(response.getStatus(), expected);
        if(expected == HttpStatus.SC_NO_CONTENT) {
            user.setPassword(newPass);
        }
    }

    public static String resetUserPassword(User user, String toReset, HttpClient client, int expected) throws Exception {
        Response response;
        if(user != null) {
            response = HttpUtils.resetUserPassword(user.getUsername(), user.getPassword(), toReset, client);
        }
        else {
            response = HttpUtils.resetUserPassword(null, null, toReset, client);
        }
        Assert.assertEquals(response.getStatus(), expected);
        if(response.getStatus() == HttpStatus.SC_OK) {
            JSONObject json = new JSONObject(response.getContents());

            return json.getString("text");
        }

        return null;
    }

    public static void banUser(User user, String toBan, HttpClient client, int expected) throws Exception {
        Response response;
        if(user != null) {
            response = HttpUtils.banUser(user.getUsername(), user.getPassword(), toBan, client);
        }
        else {
            response = HttpUtils.banUser(null, null, toBan, client);
        }
        Assert.assertEquals(response.getStatus(), expected);
    }

    public static void unbanUser(User user, String toUnban, HttpClient client, int expected) throws Exception {
        Response response;
        if(user != null) {
            response = HttpUtils.unbanUser(user.getUsername(), user.getPassword(), toUnban, client);
        }
        else {
            response = HttpUtils.unbanUser(null, null, toUnban, client);
        }
        Assert.assertEquals(response.getStatus(), expected);
    }

    public static void unlockUser(User user, String toUnlock, HttpClient client, int expected) throws Exception {
        Response response;
        if(user != null) {
            response = HttpUtils.unlockUser(user.getUsername(), user.getPassword(), toUnlock, client);
        }
        else {
            response = HttpUtils.unlockUser(null, null, toUnlock, client);
        }
        Assert.assertEquals(response.getStatus(), expected);
    }
}
