package io.joshatron.bgt.server.database;

import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.request.Auth;
import io.joshatron.bgt.server.response.State;
import io.joshatron.bgt.server.response.UserInfo;

public interface AccountDAO {

    boolean isAuthenticated(Auth auth) throws GameServerException;
    void addUser(Auth auth) throws GameServerException;
    void updatePassword(String username, String password) throws GameServerException;
    void updateUsername(String oldUsername, String newUsername) throws GameServerException;
    void updateRating(String userId, int newRating) throws GameServerException;
    void updateState(String userId, State state) throws GameServerException;
    boolean userExists(String userId) throws GameServerException;
    boolean usernameExists(String username) throws GameServerException;
    UserInfo getUserFromId(String id) throws GameServerException;
    UserInfo getUserFromUsername(String username) throws GameServerException;
}
