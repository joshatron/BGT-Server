package io.joshatron.bgt.server.validation;

import io.joshatron.bgt.server.database.AccountDAO;
import io.joshatron.bgt.server.database.SocialDAO;
import io.joshatron.bgt.server.database.model.User;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SocialValidator {
    @Autowired
    private SocialDAO socialDAO;
    @Autowired
    private AccountDAO accountDAO;

    public void validateUsersUnrelated(UUID requester, UUID other) {
        validateUsersNotSame(requester, other);
        User user = accountDAO.getUserFromId(requester);
        validateNotBlocking(user, other);
        validateNotBlocked(user, other);
        validateNotRequesting(user, other);
        validateNotRequested(user, other);
        validateNotFriends(user, other);
    }

    public void validateUserRequesting(UUID requester, UUID other) {
        User user = accountDAO.getUserFromId(requester);
        validateRequesting(user, other);
    }

    private void validateUsersNotSame(UUID requester, UUID other) {
        if(requester.equals(other)) {
            throw new GameServerException(ErrorCode.REFERENCING_SELF);
        }
    }

    private void validateNotBlocked(User requester, UUID other) {
        if(requester.isBlocked(other)) {
            throw new GameServerException(ErrorCode.BLOCKED);
        }
    }

    private void validateNotBlocking(User requester, UUID other) {
        if(requester.isBlocking(other)) {
            throw new GameServerException(ErrorCode.BLOCKING);
        }
    }

    private void validateNotRequesting(User requester, UUID other) {
        if(requester.isRequestingUser(other)) {
            throw new GameServerException(ErrorCode.ALREADY_REQUESTING);
        }
    }

    private void validateRequesting(User requester, UUID other) {
        if(!requester.isRequestingUser(other)) {
            throw new GameServerException(ErrorCode.REQUEST_NOT_FOUND);
        }
    }

    private void validateNotRequested(User requester, UUID other) {
        if(requester.isRequestedByUser(other)) {
            throw new GameServerException(ErrorCode.ALREADY_BEING_REQUESTED);
        }
    }

    private void validateNotFriends(User requester, UUID other) {
        if(requester.isFriend(other)) {
            throw new GameServerException(ErrorCode.ALREADY_FRIENDS);
        }
    }
}
