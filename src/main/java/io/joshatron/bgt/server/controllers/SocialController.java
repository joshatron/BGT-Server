package io.joshatron.bgt.server.controllers;

import io.joshatron.bgt.server.request.Auth;
import io.joshatron.bgt.server.request.Text;
import io.joshatron.bgt.server.response.UserMessage;
import io.joshatron.bgt.server.response.SocialNotifications;
import io.joshatron.bgt.server.response.UserInfo;
import io.joshatron.bgt.server.utils.SocialUtils;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/social")
public class SocialController {

    @Autowired
    private SocialUtils socialUtils;
    private Logger logger = LoggerFactory.getLogger(SocialController.class);

    @PostMapping(value = "/request/create/{id}", produces = "application/json")
    public ResponseEntity requestFriend(@RequestHeader(value="Authorization") String auth, @PathVariable("id") String other) {
        try {
            logger.info("Creating friend request");
            socialUtils.createFriendRequest(new Auth(auth), other);
            logger.info("Successfully created friend request");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @DeleteMapping(value = "/request/cancel/{id}", produces = "application/json")
    public ResponseEntity cancelFriendRequest(@RequestHeader(value="Authorization") String auth, @PathVariable("id") String other) {
        try {
            logger.info("Deleting friend request");
            socialUtils.deleteFriendRequest(new Auth(auth), other);
            logger.info("Successfully deleted friend request");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/request/respond/{id}", produces = "application/json")
    public ResponseEntity respondToRequest(@RequestHeader(value="Authorization") String auth, @PathVariable("id") String id, @RequestBody Text friendResponse) {
        try {
            logger.info("Responding to friend request");
            socialUtils.respondToFriendRequest(new Auth(auth), id, friendResponse);
            logger.info("Successfully responded to request");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @GetMapping(value = "/request/incoming", produces = "application/json")
    public ResponseEntity checkIncomingRequests(@RequestHeader(value="Authorization") String auth) {
        try {
            logger.info("Requesting incoming friend requests");
            UserInfo[] incoming = socialUtils.listIncomingFriendRequests(new Auth(auth));
            logger.info("Returning requests");
            return new ResponseEntity<>(incoming, HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @GetMapping(value = "/request/outgoing", produces = "application/json")
    public ResponseEntity checkOutgoingRequests(@RequestHeader(value="Authorization") String auth) {
        try {
            logger.info("Requesting outgoing friend requests");
            UserInfo[] outgoing = socialUtils.listOutgoingFriendRequests(new Auth(auth));
            logger.info("Returning requests");
            return new ResponseEntity<>(outgoing, HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @DeleteMapping(value = "/user/{id}/unfriend", produces = "application/json")
    public ResponseEntity unfriend(@RequestHeader(value="Authorization") String auth, @PathVariable("id") String other) {
        try {
            logger.info("Unfriending user");
            socialUtils.unfriend(new Auth(auth), other);
            logger.info("User successfully unfriended");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/user/{id}/block", produces = "application/json")
    public ResponseEntity blockUser(@RequestHeader(value="Authorization") String auth, @PathVariable("id") String block) {
        try {
            logger.info("Blocking user");
            socialUtils.blockUser(new Auth(auth), block);
            logger.info("User successfully blocked");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @DeleteMapping(value = "/user/{id}/unblock", produces = "application/json")
    public ResponseEntity unblockUser(@RequestHeader(value="Authorization") String auth, @PathVariable("id") String unblock) {
        try {
            logger.info("Unblocking user");
            socialUtils.unblockUser(new Auth(auth), unblock);
            logger.info("User successfully unblocked");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @GetMapping(value = "/user/{id}/blocked", produces = "application/json")
    public ResponseEntity isBlocked(@RequestHeader(value="Authorization") String auth, @PathVariable("id") String isBlocked) {
        try {
            logger.info("Checking if user is blocked");
            if(socialUtils.isBlocked(new Auth(auth), isBlocked)) {
                logger.info("The user is blocked");
                throw new GameServerException(ErrorCode.BLOCKED);
            }
            else {
                logger.info("The user is not blocked");
                return new ResponseEntity(HttpStatus.NO_CONTENT);
            }
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }


    @GetMapping(value = "/user/friends", produces = "application/json")
    public ResponseEntity listFriends(@RequestHeader(value="Authorization") String auth) {
        try {
            logger.info("Getting list of friends");
            UserInfo[] friends = socialUtils.listFriends(new Auth(auth));
            logger.info("Returning friend list");
            return new ResponseEntity<>(friends, HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @GetMapping(value = "/user/blocking", produces = "application/json")
    public ResponseEntity listBlocked(@RequestHeader(value="Authorization") String auth) {
        try {
            logger.info("Getting list blocking users");
            UserInfo[] blocked = socialUtils.listBlocked(new Auth(auth));
            logger.info("Returning blocking list");
            return new ResponseEntity<>(blocked, HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/message/send/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity sendMessage(@RequestHeader(value="Authorization") String auth, @PathVariable("id") String id, @RequestBody Text sendMessage) {
        try {
            logger.info("Sending a message");
            socialUtils.sendMessage(new Auth(auth), id, sendMessage);
            logger.info("Message successfully sent");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @GetMapping(value = "/message/search", produces = "application/json")
    public ResponseEntity readMessages(@RequestHeader(value="Authorization") String auth, @RequestParam(value = "others", required = false) String senders,
                                       @RequestParam(value = "start", required = false) Long start, @RequestParam(value = "end", required = false) Long end,
                                       @RequestParam(value = "read", required = false) String read, @RequestParam(value = "from", required = false) String from) {
        try {
            logger.info("Reading messages");
            UserMessage[] messages = socialUtils.listMessages(new Auth(auth), senders, start, end, read, from);
            logger.info("Messages found, returning");
            return new ResponseEntity<>(messages, HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @GetMapping(value = "/notifications", produces = "application/json")
    public ResponseEntity getNotifications(@RequestHeader(value="Authorization") String auth) {
        try {
            logger.info("Getting social notifications");
            SocialNotifications notifications = socialUtils.getNotifications(new Auth(auth));
            logger.info("Social notifications found, returning");
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }
}
