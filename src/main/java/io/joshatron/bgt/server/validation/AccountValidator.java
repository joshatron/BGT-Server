package io.joshatron.bgt.server.validation;

import io.joshatron.bgt.server.database.AccountDAO;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.request.Auth;
import io.joshatron.bgt.server.request.NewPassword;
import io.joshatron.bgt.server.request.NewUsername;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountValidator {

    @Autowired
    private AccountDAO accountDAO;

    public void validateRegistration(Auth auth) {
        DTOValidator.validateAuth(auth);
        if(accountDAO.usernameExists(auth.getUsername())) {
            throw new GameServerException(ErrorCode.USERNAME_TAKEN);
        }
    }

    public void validatePasswordChange(String authString, NewPassword passChange) {
        Auth auth = DTOValidator.validateAuthString(authString);
        DTOValidator.validateNewPassword(passChange);
        DTOValidator.validatePassword(passChange.getNewPassword());
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        Auth testAuth = new Auth(auth.getUsername(), passChange.getNewPassword());
        if(accountDAO.isAuthenticated(testAuth)) {
            throw new GameServerException(ErrorCode.SAME_PASSWORD);
        }
    }

    public void validateUsernameChange(String authString, NewUsername userChange) {
        Auth auth = DTOValidator.validateAuthString(authString);
        DTOValidator.validateNewUsername(userChange);
        DTOValidator.validateUsername(userChange.getNewUsername());
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        if(auth.getUsername().equals(userChange.getNewUsername())) {
            throw new GameServerException(ErrorCode.SAME_USERNAME);
        }
        if(!auth.getUsername().equalsIgnoreCase(userChange.getNewUsername()) &&  accountDAO.usernameExists(userChange.getNewUsername())) {
            throw new GameServerException(ErrorCode.USERNAME_TAKEN);
        }
    }

    public void validateAuthenticate(String authString) {
        DTOValidator.validateAuthString(authString);
    }

    public void validateFindUser(String username, String id) {
        if(username != null && id == null) {
            DTOValidator.validateUsername(username);
        }
        else if(id != null && username == null) {
            DTOValidator.validateId(id);
        }
        else {
            throw new GameServerException(ErrorCode.TOO_MANY_ARGUMENTS);
        }
    }
}
