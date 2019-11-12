package io.joshatron.bgt.server.utils;

import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.request.Auth;
import io.joshatron.bgt.server.request.NewPassword;
import io.joshatron.bgt.server.request.NewUsername;
import io.joshatron.bgt.server.database.AccountDAO;
import io.joshatron.bgt.server.response.State;
import io.joshatron.bgt.server.response.UserInfo;
import io.joshatron.bgt.server.validation.AccountValidator;
import io.joshatron.bgt.server.validation.DTOValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountUtils {

    @Autowired
    private AccountDAO accountDAO;
    @Autowired
    private AccountValidator accountValidator;

    public boolean isAuthenticated(String authString) {
        try {
            accountValidator.verifyCredentials(authString);
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    public void registerUser(Auth rawAuth) {
        Auth auth = accountValidator.verifyRegistration(rawAuth);
        accountDAO.createUser(auth);
    }

    public void updatePassword(String authString, NewPassword change) {
        UUID user = accountValidator.verifyCredentials(authString);
        String newPass = accountValidator.verifyPassChange(user, change);
        accountDAO.updatePassword(user, newPass);
    }

    public void updateUsername(String authString, NewUsername change) {
        UUID user = accountValidator.verifyCredentials(authString);
        String newUsername = accountValidator.verifyUsernameChange(user, change);
        accountDAO.updateUsername(user, newUsername);
    }

    public UserInfo getUserFromUsernameOrId(String username, String id) {
        if(username != null && id == null) {
            return getUserFromUsername(username);
        }
        else if(id != null && username == null) {
            return getUserFromId(id);
        }
        else {
            throw new GameServerException(ErrorCode.TOO_MANY_ARGUMENTS);
        }
    }

    public UserInfo getUserFromId(String id) {
        if(id.length() == 0) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }

        try {
            return new UserInfo(accountDAO.getUserFromId(UUID.fromString(id)));
        }
        catch(IllegalArgumentException e) {
            throw new GameServerException(ErrorCode.INVALID_FORMATTING);
        }
    }

    public UserInfo getUserFromUsername(String username) {
        if(username.length() == 0) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }
        if(username.matches("^.*[^a-zA-Z0-9 ].*$")) {
            throw new GameServerException(ErrorCode.ALPHANUMERIC_ONLY);
        }

        if(AiUtils.isAi(username)) {
            return new UserInfo(username.toUpperCase(), username.toUpperCase(), 0, State.NORMAL);
        }
        else {
            DTOValidator.validateUsername(username);
            return new UserInfo(accountDAO.getUserFromUsername(username));
        }
    }
}
