package io.joshatron.bgt.server.utils;

import io.joshatron.bgt.server.database.model.User;
import io.joshatron.bgt.server.request.NewPassword;
import io.joshatron.bgt.server.request.Text;
import io.joshatron.bgt.server.database.AccountDAO;
import io.joshatron.bgt.server.database.AdminDAO;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.response.State;
import io.joshatron.bgt.server.validation.AccountValidator;
import io.joshatron.bgt.server.validation.AdminValidator;
import io.joshatron.bgt.server.validation.DTOValidator;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AdminUtils {

    @Autowired
    private AdminDAO adminDAO;
    @Autowired
    private AccountDAO accountDAO;
    @Autowired
    private AdminValidator adminValidator;
    @Autowired
    private AccountValidator accountValidator;

    @Value("${admin.initial-password:}")
    private String initialPassword;

    public String initializeAccount() {
        if(adminDAO.isInitialized()) {
            throw new GameServerException(ErrorCode.ADMIN_PASSWORD_INITIALIZED);
        }

        String pass = initialPassword != null && !initialPassword.isEmpty() ? initialPassword : RandomStringUtils.randomAlphanumeric(30);

        adminDAO.updatePassword(pass);

        return pass;
    }

    public void changePassword(String authString, NewPassword passChange) {
        adminValidator.verifyCredentials(authString);
        String newPass = DTOValidator.validateNewPassword(passChange);

        adminDAO.updatePassword(newPass);
    }

    public String resetUserPassword(String authString, String userToChange) {
        adminValidator.verifyCredentials(authString);
        User user = accountValidator.verifyUserId(userToChange);

        String newPass = RandomStringUtils.randomAlphanumeric(30);
        accountDAO.updatePassword(user.getId(), newPass);
        accountDAO.updateState(user.getId(), State.NORMAL);

        return newPass;
    }

    public void banUser(String authString, String userToBan) {
        adminValidator.verifyCredentials(authString);
        User user = accountValidator.verifyUserId(userToBan);

        if(accountDAO.getUserFromId(user.getId()).getState() == State.BANNED) {
            throw new GameServerException(ErrorCode.ALREADY_BANNED);
        }

        accountDAO.updateState(user.getId(), State.BANNED);
    }

    public void unbanUser(String authString, String userToSet) {
        DTOValidator.validateAuth(auth);
        UUID userId = DTOValidator.validateId(userToSet);
        if(!adminDAO.isInitialized()) {
            throw new GameServerException(ErrorCode.ADMIN_PASSWORD_NOT_INITIALIZED);
        }
        if(!adminDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        if(!accountDAO.userExists(userId)) {
            throw new GameServerException(ErrorCode.USER_NOT_FOUND);
        }
        if(accountDAO.getUserFromId(userId).getState() != State.BANNED) {
            throw new GameServerException(ErrorCode.USER_NOT_BANNED);
        }

        accountDAO.updateState(userId, State.NORMAL);
    }

    public void unlockUser(String authString, String userToSet) {
        DTOValidator.validateAuth(auth);
        UUID userId = DTOValidator.validateId(userToSet);
        if(!adminDAO.isInitialized()) {
            throw new GameServerException(ErrorCode.ADMIN_PASSWORD_NOT_INITIALIZED);
        }
        if(!adminDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        if(!accountDAO.userExists(userId)) {
            throw new GameServerException(ErrorCode.USER_NOT_FOUND);
        }
        if(accountDAO.getUserFromId(userId).getState() != State.LOCKED) {
            throw new GameServerException(ErrorCode.USER_NOT_LOCKED);
        }

        accountDAO.updateState(userId, State.NORMAL);
    }
}
