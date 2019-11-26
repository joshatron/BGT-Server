package io.joshatron.bgt.server.validation;

import io.joshatron.bgt.engine.player.PlayerIndicator;
import io.joshatron.bgt.server.database.model.GameRequest;
import io.joshatron.bgt.server.database.model.User;
import io.joshatron.bgt.server.request.*;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;

import java.util.Date;
import java.util.UUID;

public class DTOValidator {

    private DTOValidator() {
        throw new IllegalStateException("This is a utility class");
    }

    public static Auth validateAuthString(String authString) {
        if(authString == null) {
            throw new GameServerException(ErrorCode.EMPTY_AUTH);
        }
        Auth auth = new Auth(authString);
        validateAuth(auth);

        return auth;
    }

    public static void validateAuth(Auth auth) {
        if(auth == null) {
            throw new GameServerException(ErrorCode.EMPTY_AUTH);
        }
        validateUsername(auth.getUsername());
        validatePassword(auth.getPassword());
    }

    public static void validateNewUser(NewUser newUser) {
        if(newUser == null) {
            throw new GameServerException(ErrorCode.EMPTY_AUTH);
        }
        validateUsername(newUser.getUsername());
        validatePassword(newUser.getPassword());
    }

    public static String validateText(Text text) {
        if(text == null || text.getText() == null || text.getText().isEmpty()) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }
        if(text.getText().length() > 5000) {
            throw new GameServerException(ErrorCode.MESSAGE_TOO_LONG);
        }

        return text.getText();
    }

    public static String validateNewPassword(NewPassword newPassword) {
        if(newPassword == null || newPassword.getNewPassword() == null || newPassword.getNewPassword().isEmpty()) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }

        return newPassword.getNewPassword();
    }

    public static void validateNewUsername(NewUsername newUsername) {
        if(newUsername == null || newUsername.getNewUsername() == null) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }
    }

    public static void validateUsername(String username) {
        if(username == null || username.length() == 0) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }

        if(username.matches("^.*[^a-zA-Z0-9 ].*$")) {
            throw new GameServerException(ErrorCode.ALPHANUMERIC_ONLY);
        }
    }

    public static void validatePassword(String password) {
        if(password == null || password.length() == 0) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }
    }

    public static UUID validateId(String id) {
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

    public static Answer validateFriendResponse(FriendResponse response) {
        if(response == null || response.getResponse() == null) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }

        return response.getResponse();
    }

    public static Answer validateAnswer(String response) {
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

    public static Read validateRead(String read) {
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

    public static PlayerIndicator validatePlayerIndicator(String playerIndicator) {
        if(playerIndicator == null || playerIndicator.length() == 0) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }

        //TODO: fix to use from string and handle exception
        return PlayerIndicator.valueOf(playerIndicator);
    }

    public static Complete validateComplete(String complete) {
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

    public static Winner validateWinner(String from) {
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

    public static Pending validatePending(String pending) {
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

    public static From validateFrom(String from) {
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

    public static void validateMarkRead(MarkRead markRead) {
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

    public static void validateDate(Date date) {
        Date now = new Date();

        if(now.before(date)) {
            throw new GameServerException(ErrorCode.INVALID_DATE);
        }
    }

    public static void validateGameRequest(RequestGame gameRequest) {
        //validateGameBoardSize(gameRequest.getSize());

//        if(!gameRequest.getFirst().equalsIgnoreCase("white") && !gameRequest.getFirst().equalsIgnoreCase("black")) {
//            throw new GameServerException(ErrorCode.ILLEGAL_COLOR);
//        }

//        if(!gameRequest.getFirst().equalsIgnoreCase("white") && !gameRequest.getFirst().equalsIgnoreCase("black")) {
//            throw new GameServerException(ErrorCode.ILLEGAL_COLOR);
//        }
    }

    public static void validateGameBoardSize(int size) {
        if(size != 3 && size != 4 && size != 5 && size != 6 && size != 8) {
            throw new GameServerException(ErrorCode.ILLEGAL_SIZE);
        }
    }

    public static void validateGameRequestAnswer(GameRequestAnswer response) {
        if(response == null || response.getAnswer() == null || response.getPlayerIndicator() == null) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }
    }

    public static void validateUserNotRespondedYet(User user, GameRequest request) {
        if(request.getPlayers().get(user) != PlayerIndicator.NONE) {
            throw new GameServerException(ErrorCode.ALREADY_RESPONDED);
        }
    }
}
