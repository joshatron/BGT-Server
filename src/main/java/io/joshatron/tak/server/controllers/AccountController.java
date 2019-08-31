package io.joshatron.tak.server.controllers;

import io.joshatron.tak.server.exceptions.*;
import io.joshatron.tak.server.request.Auth;
import io.joshatron.tak.server.request.Text;
import io.joshatron.tak.server.response.User;
import io.joshatron.tak.server.utils.AccountUtils;
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
    private Logger logger = LoggerFactory.getLogger(AccountController.class);

    @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
    public ResponseEntity register(@RequestBody Auth auth) {
        try {
            logger.info("Registering user");
            accountUtils.registerUser(auth);
            logger.info("User successfully registered");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/change-pass", consumes = "application/json", produces = "application/json")
    public ResponseEntity changePassword(@RequestHeader(value="Authorization") String auth, @RequestBody Text passChange) {
        try {
            logger.info("Changing password");
            accountUtils.updatePassword(new Auth(auth), passChange);
            logger.info("Password successfully changed");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/change-name", consumes = "application/json", produces = "application/json")
    public ResponseEntity changeUsername(@RequestHeader(value="Authorization") String auth, @RequestBody Text userChange) {
        try {
            logger.info("Changing username");
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
            User user;
            if(username == null && id != null) {
                logger.info("Finding user by ID");
                user = accountUtils.getUserFromId(id);
            }
            else if(id == null && username != null) {
                logger.info("Finding user by username");
                user = accountUtils.getUserFromUsername(username);
            }
            else {
                throw new GameServerException(ErrorCode.TOO_MANY_ARGUMENTS);
            }
            logger.info("User found, returning info");
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }
}
