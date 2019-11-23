package io.joshatron.bgt.server.controllers;

import io.joshatron.bgt.server.request.*;
import io.joshatron.bgt.server.response.GameInfo;
import io.joshatron.bgt.server.response.GameNotifications;
import io.joshatron.bgt.server.response.RequestInfo;
import io.joshatron.bgt.server.utils.GameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/games")
public class GameController {

    @Autowired
    private GameUtils gameUtils;
    private Logger logger = LoggerFactory.getLogger(GameController.class);

    @PostMapping(value = "/request/create", produces = "application/json")
    public ResponseEntity requestGame(@RequestHeader(value="Authorization") String authString, @RequestBody GameRequest gameRequest) {
        try {
            logger.info("Requesting new game");
            gameUtils.requestGame(authString, gameRequest);
            logger.info("Request successfully made");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @GetMapping(value = "/request/{id}", produces = "application/json")
    public ResponseEntity getGameRequest(@RequestHeader(value="Authorization") String auth, @PathVariable("id") String request) {
        try {
            logger.info("Getting game request");
            RequestInfo gameRequest = gameUtils.getRequest(new Auth(auth), request);
            logger.info("Successfully grabbed request");
            return new ResponseEntity<>(gameRequest, HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @DeleteMapping(value = "/request/{id}/cancel", produces = "application/json")
    public ResponseEntity cancelGameRequest(@RequestHeader(value="Authorization") String auth, @PathVariable("id") String request) {
        try {
            logger.info("Deleting game request");
            gameUtils.deleteRequest(new Auth(auth), request);
            logger.info("Successfully deleted request");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/request/{id}/respond", produces = "application/json")
    public ResponseEntity respondToGameRequest(@RequestHeader(value="Authorization") String auth, @PathVariable("id") String request, @RequestBody GameRequestAnswer answer) {
        try {
            logger.info("Responding to game request");
            gameUtils.respondToGame(new Auth(auth), request, answer);
            logger.info("Successfully responded to game request");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @GetMapping(value = "/request/incoming", produces = "application/json")
    public ResponseEntity checkIncomingGames(@RequestHeader(value="Authorization") String auth) {
        try {
            logger.info("Requesting incoming games");
            RequestInfo[] games = gameUtils.checkIncomingRequests(new Auth(auth));
            logger.info("Incoming games found");
            return new ResponseEntity<>(games, HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @GetMapping(value = "/request/outgoing", produces = "application/json")
    public ResponseEntity checkOutgoingGames(@RequestHeader(value="Authorization") String auth) {
        try {
            logger.info("Requesting outgoing games");
            RequestInfo[] games = gameUtils.checkOutgoingRequests(new Auth(auth));
            logger.info("Outgoing games found");
            return new ResponseEntity<>(games, HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/request/random/create", produces = "application/json")
    public ResponseEntity requestRandomGame(@RequestHeader(value="Authorization") String auth, @RequestBody RandomGameRequest request) {
        try {
            logger.info("Requesting a random game");
            gameUtils.requestRandomGame(new Auth(auth), request);
            logger.info("Random game successfully made");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @DeleteMapping(value = "/request/random/cancel", produces = "application/json")
    public ResponseEntity cancelRandomGameRequest(@RequestHeader(value="Authorization") String auth) {
        try {
            logger.info("Deleting random game request");
            gameUtils.deleteRandomRequest(new Auth(auth));
            logger.info("Successfully deleted random request");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @GetMapping(value = "/request/random/exists", produces = "application/json")
    public ResponseEntity getOutgoingRandomRequests(@RequestHeader(value="Authorization") String auth) {
        try {
            logger.info("Getting outgoing random request sizes");
            if(gameUtils.randomRequestExists(new Auth(auth))) {
                logger.info("Random request found");
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            else {
                logger.info("Random request not found");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @GetMapping(value = "/game/search", produces = "application/json")
    public ResponseEntity findGames(@RequestHeader(value="Authorization") String auth, @RequestParam(value = "opponents", required = false) String opponents,
                                    @RequestParam(value = "start", required = false) Long start, @RequestParam(value = "end", required = false) Long end,
                                    @RequestParam(value = "complete", required = false) String complete, @RequestParam(value = "pending", required = false) String pending) {
        try {
            logger.info("Searching for games");
            GameInfo[] games = gameUtils.findGames(new Auth(auth), opponents, start, end, complete, pending);
            logger.info("Games found");
            return new ResponseEntity<>(games, HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @GetMapping(value = "/game/{id}", produces = "application/json")
    public ResponseEntity getGameInfo(@RequestHeader(value="Authorization") String auth, @PathVariable("id") String gameId) {
        try {
            logger.info("Getting game info");
            GameInfo info = gameUtils.getGameInfo(new Auth(auth), gameId);
            logger.info("Game info found");
            return new ResponseEntity<>(info, HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/game/{id}/send-message", produces = "application/json")
    public ResponseEntity sendGameMessage(@RequestHeader(value="Authorization") String auth, @PathVariable("id") String gameId, @RequestBody Message message) {
        try {
            logger.info("Sending message to game");
            gameUtils.sendGameMessage(new Auth(auth), gameId, message);
            logger.info("Game message sent");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @GetMapping(value = "/game/{id}/possible", produces = "application/json")
    public ResponseEntity getPossibleTurns(@RequestHeader(value="Authorization") String auth, @PathVariable("id") String gameId) {
        try {
            logger.info("Getting all possible turns for a game");
            String[] turns = gameUtils.getPossibleTurns(new Auth(auth), gameId);
            logger.info("Turns found");
            return new ResponseEntity<>(turns, HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @PostMapping(value = "/game/{id}/play", produces = "application/json")
    public ResponseEntity playTurn(@RequestHeader(value="Authorization") String auth, @PathVariable("id") String gameId, @RequestBody Move move) {
        try {
            logger.info("Trying to play a turn");
            gameUtils.playTurn(new Auth(auth), gameId, move);
            logger.info("Turn successfully made");
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }

    @GetMapping(value = "/notifications", produces = "application/json")
    public ResponseEntity getNotifications(@RequestHeader(value="Authorization") String auth) {
        try {
            logger.info("Getting game notifications");
            GameNotifications gameNotifications = gameUtils.getNotifications(new Auth(auth));
            logger.info("Game notifications found");
            return new ResponseEntity<>(gameNotifications, HttpStatus.OK);
        } catch (Exception e) {
            return ControllerUtils.handleExceptions(e, logger);
        }
    }
}
