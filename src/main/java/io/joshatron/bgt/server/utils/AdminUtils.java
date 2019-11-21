package io.joshatron.bgt.server.utils;

import io.joshatron.bgt.server.database.model.User;
import io.joshatron.bgt.server.request.NewPassword;
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

    public void setUserState(String authString, String userToBan, State state) {
        adminValidator.verifyCredentials(authString);
        User user = accountValidator.verifyUserId(userToBan);

        if(user.getState() == state) {
            throw new GameServerException(ErrorCode.ALREADY_IN_STATE);
        }

        accountDAO.updateState(user.getId(), state);
    }
}
