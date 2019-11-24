package io.joshatron.bgt.server.validation;

import io.joshatron.bgt.server.database.AccountDAO;
import io.joshatron.bgt.server.database.model.User;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.request.Auth;
import io.joshatron.bgt.server.request.NewPassword;
import io.joshatron.bgt.server.request.NewUser;
import io.joshatron.bgt.server.request.NewUsername;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class AccountValidator {

    @Autowired
    private AccountDAO accountDAO;

    public User verifyCredentials(String authString) {
        Auth auth = DTOValidator.validateAuthString(authString);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }

        return accountDAO.getUserFromUsername(auth.getUsername());
    }

    public User verifyUserId(String idString) {
        UUID id = UUID.fromString(idString);
        try {
            return accountDAO.getUserFromId(id);
        } catch(GameServerException e) {
            throw new GameServerException(ErrorCode.USER_NOT_FOUND);
        }
    }

    public List<User> verifyUserIds(List<String> ids) {
        return ids.stream().map(this::verifyUserId).collect(Collectors.toList());
    }

    public void verifyRegistration(NewUser newUser) {
        DTOValidator.validateNewUser(newUser);
        if(accountDAO.usernameExists(newUser.getUsername())) {
            throw new GameServerException(ErrorCode.USERNAME_TAKEN);
        }
    }

    public String verifyPassChange(User user, NewPassword passChange) {
        if(passChange == null || passChange.getNewPassword() == null || passChange.getNewPassword().isEmpty()) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }

        Auth testAuth = new Auth(user.getUsername(), passChange.getNewPassword());
        if(accountDAO.isAuthenticated(testAuth)) {
            throw new GameServerException(ErrorCode.SAME_PASSWORD);
        }

        return passChange.getNewPassword();
    }

    public String verifyUsernameChange(User user, NewUsername userChange) {
        if(userChange == null || userChange.getNewUsername() == null || userChange.getNewUsername().isEmpty()) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }

        if(user.getUsername().equals(userChange.getNewUsername())) {
            throw new GameServerException(ErrorCode.SAME_USERNAME);
        }
        if(accountDAO.usernameExists(userChange.getNewUsername())) {
            throw new GameServerException(ErrorCode.USERNAME_TAKEN);
        }

        return userChange.getNewUsername();
    }
}
