package io.joshatron.bgt.server.controllers;

import io.joshatron.bgt.server.request.NewPassword;
import io.joshatron.bgt.server.request.Text;
import io.joshatron.bgt.server.response.State;
import io.joshatron.bgt.server.utils.AdminUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/admin", produces = "application/json")
public class AdminController {

    @Autowired
    private AdminUtils adminUtils;
    private Logger logger = LoggerFactory.getLogger(AdminController.class);

    @PostMapping(value = "/initialize")
    public ResponseEntity initialize() {
        try {
            logger.info("Creating admin password");
            String pass = adminUtils.initializeAccount();
            logger.info("Admin password successfully created: {}", pass);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/change-pass", consumes = "application/json")
    public ResponseEntity changePassword(@RequestHeader(value="Authorization") String authString, @RequestBody NewPassword passChange) {
        try {
            logger.info("Changing admin password");
            adminUtils.changePassword(authString, passChange);
            logger.info("Admin password successfully changed");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/user/{id}/reset", produces = "application/json")
    public ResponseEntity resetUserPassword(@RequestHeader(value="Authorization") String authString, @PathVariable("id") String userToChange) {
        try {
            logger.info("Resetting user password");
            String newPass = adminUtils.resetUserPassword(authString, userToChange);
            logger.info("User password successfully reset");
            return new ResponseEntity<>(new Text(newPass), HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/user/{id}/ban", produces = "application/json")
    public ResponseEntity banUser(@RequestHeader(value="Authorization") String authString, @PathVariable("id") String userToBan) {
        try {
            logger.info("Banning user");
            adminUtils.setUserState(authString, userToBan, State.BANNED);
            logger.info("User successfully banned");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/user/{id}/unban", produces = "application/json")
    public ResponseEntity unbanUser(@RequestHeader(value="Authorization") String authString, @PathVariable("id") String userToUnban) {
        try {
            logger.info("Unbanning user");
            adminUtils.setUserState(authString, userToUnban, State.NORMAL);
            logger.info("User successfully unbanned");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/user/{id}/unlock", produces = "application/json")
    public ResponseEntity unlockUser(@RequestHeader(value="Authorization") String authString, @PathVariable("id") String userToUnlock) {
        try {
            logger.info("Unlocking user");
            adminUtils.setUserState(authString, userToUnlock, State.NORMAL);
            logger.info("User successfully unlocked");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }
}
