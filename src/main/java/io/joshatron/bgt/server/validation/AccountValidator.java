package io.joshatron.bgt.server.validation;

import io.joshatron.bgt.server.database.AccountDAO;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.request.Auth;
import io.joshatron.bgt.server.request.NewPassword;
import io.joshatron.bgt.server.request.NewUser;
import io.joshatron.bgt.server.request.NewUsername;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountValidator {

    @Autowired
    private AccountDAO accountDAO;

    public UUID verifyCredentials(String authString) {
        Auth auth = DTOValidator.validateAuthString(authString);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }

        return accountDAO.getUserFromUsername(auth.getUsername()).getId();
    }

    public UUID verifyUserId(String idString) {
        UUID id = UUID.fromString(idString);
        if(!accountDAO.userExists(id)) {
            throw new GameServerException(ErrorCode.USER_NOT_FOUND);
        }

        return id;
    }

    public void verifyRegistration(NewUser newUser) {
        DTOValidator.validateNewUser(newUser);
        if(accountDAO.usernameExists(newUser.getUsername())) {
            throw new GameServerException(ErrorCode.USERNAME_TAKEN);
        }
    }

    public String verifyPassChange(UUID user, NewPassword passChange) {
        if(passChange == null || passChange.getNewPassword() == null || passChange.getNewPassword().isEmpty()) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }

        Auth testAuth = new Auth(accountDAO.getUserFromId(user).getUsername(), passChange.getNewPassword());
        if(accountDAO.isAuthenticated(testAuth)) {
            throw new GameServerException(ErrorCode.SAME_PASSWORD);
        }

        return passChange.getNewPassword();
    }

    public String verifyUsernameChange(UUID user, NewUsername userChange) {
        if(userChange == null || userChange.getNewUsername() == null || userChange.getNewUsername().isEmpty()) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }

        if(accountDAO.getUserFromId(user).getUsername().equals(userChange.getNewUsername())) {
            throw new GameServerException(ErrorCode.SAME_USERNAME);
        }
        if(accountDAO.usernameExists(userChange.getNewUsername())) {
            throw new GameServerException(ErrorCode.USERNAME_TAKEN);
        }

        return userChange.getNewUsername();
    }
}
