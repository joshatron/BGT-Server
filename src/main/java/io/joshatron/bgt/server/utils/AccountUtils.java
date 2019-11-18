package io.joshatron.bgt.server.utils;

import io.joshatron.bgt.server.database.model.User;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.request.NewPassword;
import io.joshatron.bgt.server.request.NewUser;
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

    public void isAuthenticated(String authString) {
        accountValidator.verifyCredentials(authString);
    }

    public void registerUser(NewUser newUser) {
        accountValidator.verifyRegistration(newUser);

        accountDAO.createUser(newUser);
    }

    public void updatePassword(String authString, NewPassword change) {
        User user = accountValidator.verifyCredentials(authString);
        String newPass = accountValidator.verifyPassChange(user, change);
        accountDAO.updatePassword(user.getId(), newPass);
    }

    public void updateUsername(String authString, NewUsername change) {
        User user = accountValidator.verifyCredentials(authString);
        String newUsername = accountValidator.verifyUsernameChange(user, change);
        accountDAO.updateUsername(user.getId(), newUsername);
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

    public UserInfo getUserFromId(String idString) {
        UUID id = DTOValidator.validateId(idString);
        return new UserInfo(accountDAO.getUserFromId(id));
    }

    public UserInfo getUserFromUsername(String username) {
        DTOValidator.validateUsername(username);

        if(AiUtils.isAi(username)) {
            return new UserInfo(username.toUpperCase(), username.toUpperCase(), 0, State.NORMAL);
        }
        else {
            return new UserInfo(accountDAO.getUserFromUsername(username));
        }
    }
}
