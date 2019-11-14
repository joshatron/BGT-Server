package io.joshatron.bgt.server.controllers;

import io.joshatron.bgt.server.request.NewPassword;
import io.joshatron.bgt.server.request.NewUser;
import io.joshatron.bgt.server.request.NewUsername;
import io.joshatron.bgt.server.utils.AccountUtils;
import io.joshatron.bgt.server.response.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/account", produces = "application/json")
public class AccountController {

    @Autowired
    private AccountUtils accountUtils;
    private Logger logger = LoggerFactory.getLogger(AccountController.class);

    @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
    public ResponseEntity register(@RequestBody NewUser newUser) {
        try {
            logger.info("Registering user");
            UUID newUserId = accountUtils.registerUser(newUser);
            logger.info("User successfully registered");
            return new ResponseEntity<>(newUserId.toString(), HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/change-pass", consumes = "application/json", produces = "application/json")
    public ResponseEntity changePassword(@RequestHeader(value="Authorization") String authString, @RequestBody NewPassword passChange) {
        try {
            logger.info("Changing password");
            accountUtils.updatePassword(authString, passChange);
            logger.info("Password successfully changed");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/change-name", consumes = "application/json", produces = "application/json")
    public ResponseEntity changeUsername(@RequestHeader(value="Authorization") String authString, @RequestBody NewUsername userChange) {
        try {
            logger.info("Changing username");
            accountUtils.updateUsername(authString, userChange);
            logger.info("Username successfully changed");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @GetMapping(value = "/authenticate", produces = "application/json")
    public ResponseEntity authenticate(@RequestHeader(value="Authorization") String authString) {
        try {
            logger.info("Authenticating");
            accountUtils.isAuthenticated(authString);
            logger.info("User successfully authenticated");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @GetMapping(value = "/user", produces = "application/json")
    public ResponseEntity findUser(@RequestParam(value = "user", required = false) String username, @RequestParam(value = "id", required = false) String id) {
        try {
            logger.info("Finding user info");
            UserInfo user = accountUtils.getUserFromUsernameOrId(username, id);
            logger.info("User found, returning info");
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }
}
