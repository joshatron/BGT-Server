package io.joshatron.bgt.server.database;

import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.request.Auth;

public interface AdminDAO {

    boolean isInitialized() throws GameServerException;
    boolean isAuthenticated(Auth auth) throws GameServerException;
    void updatePassword(String newPass) throws GameServerException;
}
