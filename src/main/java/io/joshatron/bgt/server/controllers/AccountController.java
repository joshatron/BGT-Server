package io.joshatron.bgt.server.controllers;

import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.request.Auth;
import io.joshatron.bgt.server.request.NewPassword;
import io.joshatron.bgt.server.request.NewUsername;
import io.joshatron.bgt.server.utils.AccountUtils;
import io.joshatron.bgt.server.response.UserInfo;
import io.joshatron.bgt.server.validation.AccountValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/account", produces = "application/json")
public class AccountController {

    @Autowired
    private AccountUtils accountUtils;
    @Autowired
    private AccountValidator accountValidator;
    private Logger logger = LoggerFactory.getLogger(AccountController.class);

    @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
    public ResponseEntity register(@RequestBody Auth auth) {
        try {
            logger.info("Registering user");
            accountValidator.validateRegistration(auth);
            accountUtils.registerUser(auth);
            logger.info("User successfully registered");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/change-pass", consumes = "application/json", produces = "application/json")
    public ResponseEntity changePassword(@RequestHeader(value="Authorization") String auth, @RequestBody NewPassword passChange) {
        try {
            logger.info("Changing password");
            accountValidator.validatePasswordChange(auth, passChange);
            accountUtils.updatePassword(new Auth(auth), passChange);
            logger.info("Password successfully changed");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/change-name", consumes = "application/json", produces = "application/json")
    public ResponseEntity changeUsername(@RequestHeader(value="Authorization") String auth, @RequestBody NewUsername userChange) {
        try {
            logger.info("Changing username");
            accountValidator.validateUsernameChange(auth, userChange);
            accountUtils.updateUsername(new Auth(auth), userChange);
            logger.info("Username successfully changed");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @GetMapping(value = "/authenticate", produces = "application/json")
    public ResponseEntity authenticate(@RequestHeader(value="Authorization") String auth) {
        try {
            logger.info("Authenticating");
            accountValidator.validateAuthenticate(auth);
            if(accountUtils.isAuthenticated(new Auth(auth))) {
                logger.info("User successfully authenticated");
                return new ResponseEntity(HttpStatus.NO_CONTENT);
            }
            else {
                throw new GameServerException(ErrorCode.INCORRECT_AUTH);
            }
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @GetMapping(value = "/user", produces = "application/json")
    public ResponseEntity findUser(@RequestParam(value = "user", required = false) String username, @RequestParam(value = "id", required = false) String id) {
        try {
            logger.info("Finding user info");
            accountValidator.validateFindUser(username, id);
            UserInfo user = accountUtils.getUserFromUsernameOrId(username, id);
            logger.info("User found, returning info");
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }
}
