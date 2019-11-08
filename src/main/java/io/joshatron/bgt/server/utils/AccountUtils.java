package io.joshatron.bgt.server.utils;

import io.joshatron.bgt.server.request.Auth;
import io.joshatron.bgt.server.request.NewPassword;
import io.joshatron.bgt.server.request.NewUsername;
import io.joshatron.bgt.server.database.AccountDAO;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.response.State;
import io.joshatron.bgt.server.response.UserInfo;
import io.joshatron.bgt.server.validation.RequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountUtils {

    @Autowired
    private AccountDAO accountDAO;

    public boolean isAuthenticated(Auth auth) throws GameServerException {
        RequestValidator.validateAuth(auth);

        return accountDAO.isAuthenticated(auth);
    }

    public void registerUser(Auth auth) throws GameServerException {
        RequestValidator.validateAuth(auth);
        if(accountDAO.usernameExists(auth.getUsername())) {
            throw new GameServerException(ErrorCode.USERNAME_TAKEN);
        }

        accountDAO.createUser(auth);
    }

    public void updatePassword(Auth auth, NewPassword change) throws GameServerException {
        RequestValidator.validateAuth(auth);
        RequestValidator.validateNewPassword(change);
        RequestValidator.validatePassword(change.getNewPassword());
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        Auth testAuth = new Auth(auth.getUsername(), change.getNewPassword());
        if(accountDAO.isAuthenticated(testAuth)) {
            throw new GameServerException(ErrorCode.SAME_PASSWORD);
        }

        accountDAO.updatePassword(accountDAO.getUserFromUsername(auth.getUsername()).getId(), change.getNewPassword());
    }

    public void updateUsername(Auth auth, NewUsername change) throws GameServerException {
        RequestValidator.validateAuth(auth);
        RequestValidator.validateNewUsername(change);
        RequestValidator.validateUsername(change.getNewUsername());
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        if(auth.getUsername().equals(change.getNewUsername())) {
            throw new GameServerException(ErrorCode.SAME_USERNAME);
        }
        if(!auth.getUsername().equalsIgnoreCase(change.getNewUsername()) &&  accountDAO.usernameExists(change.getNewUsername())) {
            throw new GameServerException(ErrorCode.USERNAME_TAKEN);
        }

        accountDAO.updateUsername(accountDAO.getUserFromUsername(auth.getUsername()).getId(), change.getNewUsername());
    }

    public UserInfo getUserFromId(String id) throws GameServerException {
        if(AiUtils.isAi(id)) {
            return new UserInfo(id.toUpperCase(), id.toUpperCase(), 0, State.NORMAL);
        }
        else {
            RequestValidator.validateId(id);
            return new UserInfo(accountDAO.getUserFromId(UUID.fromString(id)));
        }
    }

    public UserInfo getUserFromUsername(String username) throws GameServerException {
        if(AiUtils.isAi(username)) {
            return new UserInfo(username.toUpperCase(), username.toUpperCase(), 0, State.NORMAL);
        }
        else {
            RequestValidator.validateUsername(username);
            return new UserInfo(accountDAO.getUserFromUsername(username));
        }
    }
}
