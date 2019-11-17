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

    public void validateUsersUnrelated(User requester, User other) {
        validateUsersNotSame(requester.getId(), other.getId());
        validateNotBlocking(requester, other.getId());
        validateNotBlocked(requester, other.getId());
        validateNotRequesting(requester, other.getId());
        validateNotRequested(requester, other.getId());
        validateNotFriends(requester, other.getId());
    }

    public void validateBlockable(User requester, UUID other) {
        validateNotBlocking(requester, other);
        validateUsersNotSame(requester.getId(), other);
    }

    public void validateUsersNotSame(UUID requester, UUID other) {
        if(requester.equals(other)) {
            throw new GameServerException(ErrorCode.REFERENCING_SELF);
        }
    }

    public void validateNotBlocked(User requester, UUID other) {
        if(requester.isBlocked(other)) {
            throw new GameServerException(ErrorCode.BLOCKED);
        }
    }

    public void validateNotBlocking(User requester, UUID other) {
        if(requester.isBlocking(other)) {
            throw new GameServerException(ErrorCode.BLOCKING);
        }
    }

    public void validateBlocking(User requester, UUID other) {
        if(!requester.isBlocking(other)) {
            throw new GameServerException(ErrorCode.NOT_BLOCKING);
        }
    }

    public void validateNotRequesting(User requester, UUID other) {
        if(requester.isRequestingUser(other)) {
            throw new GameServerException(ErrorCode.ALREADY_REQUESTING);
        }
    }

    public void validateRequesting(User requester, UUID other) {
        if(!requester.isRequestingUser(other)) {
            throw new GameServerException(ErrorCode.REQUEST_NOT_FOUND);
        }
    }

    public void validateNotRequested(User requester, UUID other) {
        if(requester.isRequestedByUser(other)) {
            throw new GameServerException(ErrorCode.ALREADY_BEING_REQUESTED);
        }
    }

    public void validateNotFriends(User requester, UUID other) {
        if(requester.isFriend(other)) {
            throw new GameServerException(ErrorCode.ALREADY_FRIENDS);
        }
    }

    public void validateFriends(User requester, UUID other) {
        if(!requester.isFriend(other)) {
            throw new GameServerException(ErrorCode.FRIEND_NOT_FOUND);
        }
    }
}
