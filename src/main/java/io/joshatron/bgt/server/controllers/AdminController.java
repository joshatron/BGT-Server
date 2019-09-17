package io.joshatron.bgt.server.controllers;

import io.joshatron.bgt.server.request.Auth;
import io.joshatron.bgt.server.request.Text;
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
    public ResponseEntity changePassword(@RequestHeader(value="Authorization") String auth, @RequestBody Text passChange) {
        try {
            logger.info("Changing admin password");
            adminUtils.changePassword(new Auth(auth), passChange);
            logger.info("Admin password successfully changed");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/user/{id}/reset", produces = "application/json")
    public ResponseEntity resetUserPassword(@RequestHeader(value="Authorization") String auth, @PathVariable("id") String userToChange) {
        try {
            logger.info("Resetting user password");
            String newPass = adminUtils.resetUserPassword(new Auth(auth), userToChange);
            logger.info("User password successfully reset");
            return new ResponseEntity<>(new Text(newPass), HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/user/{id}/ban", produces = "application/json")
    public ResponseEntity banUser(@RequestHeader(value="Authorization") String auth, @PathVariable("id") String userToBan) {
        try {
            logger.info("Banning user");
            adminUtils.banUser(new Auth(auth), userToBan);
            logger.info("User successfully banned");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/user/{id}/unban", produces = "application/json")
    public ResponseEntity unbanUser(@RequestHeader(value="Authorization") String auth, @PathVariable("id") String userToUnban) {
        try {
            logger.info("Unbanning user");
            adminUtils.unbanUser(new Auth(auth), userToUnban);
            logger.info("User successfully unbanned");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/user/{id}/unlock", produces = "application/json")
    public ResponseEntity unlockUser(@RequestHeader(value="Authorization") String auth, @PathVariable("id") String userToUnlock) {
        try {
            logger.info("Unlocking user");
            adminUtils.unlockUser(new Auth(auth), userToUnlock);
            logger.info("User successfully unlocked");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }
}
