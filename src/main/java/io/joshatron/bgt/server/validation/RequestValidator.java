package io.joshatron.bgt.server.validation;

import io.joshatron.bgt.server.request.*;
import io.joshatron.tak.engine.game.Player;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;

import java.util.Date;
import java.util.UUID;

public class RequestValidator {

    private RequestValidator() {
        throw new IllegalStateException("This is a utility class");
    }

    public static void validateAuth(Auth auth) throws GameServerException {
        if(auth == null) {
            throw new GameServerException(ErrorCode.EMPTY_AUTH);
        }
        validateUsername(auth.getUsername());
        validatePassword(auth.getPassword());
    }

    public static void validateText(Text text) throws GameServerException {
        if(text == null || text.getText() == null) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }
    }

    public static void validateNewPassword(NewPassword newPassword) throws GameServerException {
        if(newPassword == null || newPassword.getNewPassword() == null) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }
    }

    public static void validateNewUsername(NewUsername newUsername) throws GameServerException {
        if(newUsername == null || newUsername.getNewUsername() == null) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }
    }

    public static void validateUsername(String username) throws GameServerException {
        if(username == null || username.length() == 0) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }

        if(username.matches("^.*[^a-zA-Z0-9 ].*$")) {
            throw new GameServerException(ErrorCode.ALPHANUMERIC_ONLY);
        }
    }

    public static void validatePassword(String password) throws GameServerException {
        if(password == null || password.length() == 0) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }
    }

    public static UUID validateId(String id) throws GameServerException {
        if(id == null || id.length() == 0) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }

        try {
            return UUID.fromString(id);
        }
        catch(IllegalArgumentException e) {
            throw new GameServerException(ErrorCode.INVALID_FORMATTING);
        }
    }

    public static Answer validateAnswer(String response) throws GameServerException {
        if(response == null || response.length() == 0) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }

        if(response.equalsIgnoreCase("accept")) {
            return Answer.ACCEPT;
        }
        else if(response.equalsIgnoreCase("deny")) {
            return Answer.DENY;
        }

        throw new GameServerException(ErrorCode.INVALID_FORMATTING);
    }

    public static Read validateRead(String read) throws GameServerException {
        if(read == null || read.length() == 0) {
            return Read.BOTH;
        }

        if(read.equalsIgnoreCase("read")) {
            return Read.READ;
        }
        else if(read.equalsIgnoreCase("not_read")) {
            return Read.NOT_READ;
        }

        throw new GameServerException(ErrorCode.INVALID_FORMATTING);
    }

    public static Player validatePlayer(String player) throws GameServerException {
        if(player == null || player.length() == 0) {
            return Player.NONE;
        }

        if(player.equalsIgnoreCase("black")) {
            return Player.BLACK;
        }
        else if(player.equalsIgnoreCase("white")) {
            return Player.WHITE;
        }

        throw new GameServerException(ErrorCode.INVALID_FORMATTING);
    }

    public static Complete validateComplete(String complete) throws GameServerException {
        if(complete == null || complete.length() == 0) {
            return Complete.BOTH;
        }

        if(complete.equalsIgnoreCase("complete")) {
            return Complete.COMPLETE;
        }
        else if(complete.equalsIgnoreCase("incomplete")) {
            return Complete.INCOMPLETE;
        }

        throw new GameServerException(ErrorCode.INVALID_FORMATTING);
    }

    public static Winner validateWinner(String from) throws GameServerException {
        if(from == null || from.length() == 0) {
            return Winner.BOTH;
        }

        if(from.equalsIgnoreCase("me")) {
            return Winner.ME;
        }
        else if(from.equalsIgnoreCase("them")) {
            return Winner.THEM;
        }

        throw new GameServerException(ErrorCode.INVALID_FORMATTING);
    }

    public static Pending validatePending(String pending) throws GameServerException {
        if(pending == null || pending.length() == 0) {
            return Pending.BOTH;
        }

        if(pending.equalsIgnoreCase("pending")) {
            return Pending.PENDING;
        }
        else if(pending.equalsIgnoreCase("not_pending")) {
            return Pending.NOT_PENDING;
        }

        throw new GameServerException(ErrorCode.INVALID_FORMATTING);
    }

    public static From validateFrom(String from) throws GameServerException {
        if(from == null || from.length() == 0) {
            return From.BOTH;
        }

        if(from.equalsIgnoreCase("me")) {
            return From.ME;
        }
        else if(from.equalsIgnoreCase("them")) {
            return From.THEM;
        }

        throw new GameServerException(ErrorCode.INVALID_FORMATTING);
    }

    public static void validateMarkRead(MarkRead markRead) throws GameServerException {
        if(markRead == null) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }

        if(markRead.getSenders() != null) {
            for (String sender : markRead.getSenders()) {
                validateId(sender);
            }
        }

        if(markRead.getIds() != null) {
            for (String id : markRead.getIds()) {
                validateId(id);
            }
        }
    }

    public static void validateDate(Date date) throws GameServerException {
        Date now = new Date();

        if(now.before(date)) {
            throw new GameServerException(ErrorCode.INVALID_DATE);
        }
    }

    public static void validateGameRequest(GameRequest gameRequest) throws GameServerException {
        //validateGameBoardSize(gameRequest.getSize());

//        if(!gameRequest.getFirst().equalsIgnoreCase("white") && !gameRequest.getFirst().equalsIgnoreCase("black")) {
//            throw new GameServerException(ErrorCode.ILLEGAL_COLOR);
//        }

//        if(!gameRequest.getFirst().equalsIgnoreCase("white") && !gameRequest.getFirst().equalsIgnoreCase("black")) {
//            throw new GameServerException(ErrorCode.ILLEGAL_COLOR);
//        }
    }

    public static void validateGameBoardSize(int size) throws GameServerException {
        if(size != 3 && size != 4 && size != 5 && size != 6 && size != 8) {
            throw new GameServerException(ErrorCode.ILLEGAL_SIZE);
        }
    }
}
