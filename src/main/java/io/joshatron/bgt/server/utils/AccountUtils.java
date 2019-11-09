package io.joshatron.bgt.server.utils;

import io.joshatron.bgt.server.request.Auth;
import io.joshatron.bgt.server.request.NewPassword;
import io.joshatron.bgt.server.request.NewUsername;
import io.joshatron.bgt.server.database.AccountDAO;
import io.joshatron.bgt.server.response.State;
import io.joshatron.bgt.server.response.UserInfo;
import io.joshatron.bgt.server.validation.DTOValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountUtils {

    @Autowired
    private AccountDAO accountDAO;

    public boolean isAuthenticated(Auth auth) {
        DTOValidator.validateAuth(auth);

        return accountDAO.isAuthenticated(auth);
    }

    public void registerUser(Auth auth) {
        accountDAO.createUser(auth);
    }

    public void updatePassword(Auth auth, NewPassword change) {
        accountDAO.updatePassword(accountDAO.getUserFromUsername(auth.getUsername()).getId(), change.getNewPassword());
    }

    public void updateUsername(Auth auth, NewUsername change) {
        accountDAO.updateUsername(accountDAO.getUserFromUsername(auth.getUsername()).getId(), change.getNewUsername());
    }

    public UserInfo getUserFromUsernameOrId(String username, String id) {
        if(username != null) {
            return getUserFromUsername(username);
        }
        else {
            return getUserFromId(id);
        }
    }

    public UserInfo getUserFromId(String id) {
        if(AiUtils.isAi(id)) {
            return new UserInfo(id.toUpperCase(), id.toUpperCase(), 0, State.NORMAL);
        }
        else {
            DTOValidator.validateId(id);
            return new UserInfo(accountDAO.getUserFromId(UUID.fromString(id)));
        }
    }

    public UserInfo getUserFromUsername(String username) {
        if(AiUtils.isAi(username)) {
            return new UserInfo(username.toUpperCase(), username.toUpperCase(), 0, State.NORMAL);
        }
        else {
            DTOValidator.validateUsername(username);
            return new UserInfo(accountDAO.getUserFromUsername(username));
        }
    }
}
