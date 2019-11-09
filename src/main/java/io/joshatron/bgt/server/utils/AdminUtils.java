package io.joshatron.bgt.server.utils;

import io.joshatron.bgt.server.request.Auth;
import io.joshatron.bgt.server.request.Text;
import io.joshatron.bgt.server.database.AccountDAO;
import io.joshatron.bgt.server.database.AdminDAO;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.response.State;
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

    @Value("${admin.initial-password:}")
    private String initialPassword;

    public String initializeAccount() throws GameServerException {
        if(adminDAO.isInitialized()) {
            throw new GameServerException(ErrorCode.ADMIN_PASSWORD_INITIALIZED);
        }

        String pass = initialPassword != null && !initialPassword.isEmpty() ? initialPassword : RandomStringUtils.randomAlphanumeric(30);

        adminDAO.updatePassword(pass);

        return pass;
    }

    public void changePassword(Auth auth, Text passChange) throws GameServerException {
        DTOValidator.validateAuth(auth);
        DTOValidator.validateText(passChange);
        DTOValidator.validatePassword(passChange.getText());
        if(!adminDAO.isInitialized()) {
            throw new GameServerException(ErrorCode.ADMIN_PASSWORD_NOT_INITIALIZED);
        }
        if(!adminDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }

        adminDAO.updatePassword(passChange.getText());
    }

    public String resetUserPassword(Auth auth, String userToChange) throws GameServerException {
        DTOValidator.validateAuth(auth);
        UUID userId = DTOValidator.validateId(userToChange);
        if(!adminDAO.isInitialized()) {
            throw new GameServerException(ErrorCode.ADMIN_PASSWORD_NOT_INITIALIZED);
        }
        if(!adminDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        if(!accountDAO.userExists(userId)) {
            throw new GameServerException(ErrorCode.USER_NOT_FOUND);
        }

        String newPass = RandomStringUtils.randomAlphanumeric(30);
        accountDAO.updatePassword(userId, newPass);
        accountDAO.updateState(userId, State.NORMAL);

        return newPass;
    }

    public void banUser(Auth auth, String userToBan) throws GameServerException {
        DTOValidator.validateAuth(auth);
        UUID userId = DTOValidator.validateId(userToBan);
        if(!adminDAO.isInitialized()) {
            throw new GameServerException(ErrorCode.ADMIN_PASSWORD_NOT_INITIALIZED);
        }
        if(!adminDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        if(accountDAO.getUserFromId(userId).getState() == State.BANNED) {
            throw new GameServerException(ErrorCode.ALREADY_BANNED);
        }

        accountDAO.updateState(userId, State.BANNED);
    }

    public void unbanUser(Auth auth, String userToSet) throws GameServerException {
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

    public void unlockUser(Auth auth, String userToSet) throws GameServerException {
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
