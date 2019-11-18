package io.joshatron.bgt.server.utils;

import io.joshatron.bgt.server.database.model.User;
import io.joshatron.bgt.server.database.model.Message;
import io.joshatron.bgt.server.request.*;
import io.joshatron.bgt.server.database.AccountDAO;
import io.joshatron.bgt.server.database.SocialDAO;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.response.MessageInfo;
import io.joshatron.bgt.server.response.SocialNotifications;
import io.joshatron.bgt.server.response.UserInfo;
import io.joshatron.bgt.server.validation.AccountValidator;
import io.joshatron.bgt.server.validation.DTOValidator;
import io.joshatron.bgt.server.validation.SocialValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Component
public class SocialUtils {

    @Autowired
    private SocialDAO socialDAO;
    @Autowired
    private AccountDAO accountDAO;
    @Autowired
    private AccountValidator accountValidator;
    @Autowired
    private SocialValidator socialValidator;

    public void createFriendRequest(String authString, String otherId) {
        User user = accountValidator.verifyCredentials(authString);
        User other = accountValidator.verifyUserId(otherId);

        socialValidator.validateUsersUnrelated(user, other);

        socialDAO.createFriendRequest(user.getId(), other.getId());
    }

    public void deleteFriendRequest(String authString, String otherId) {
        User user = accountValidator.verifyCredentials(authString);
        User other = accountValidator.verifyUserId(otherId);

        socialValidator.validateRequesting(user, other.getId());

        socialDAO.deleteFriendRequest(user.getId(), other.getId());
    }

    public void respondToFriendRequest(String authString, String otherId, FriendResponse response) {
        User user = accountValidator.verifyCredentials(authString);
        User other = accountValidator.verifyUserId(otherId);
        Answer answer = DTOValidator.validateFriendResponse(response);

        socialValidator.validateRequesting(other, user.getId());

        if(answer == Answer.ACCEPT) {
            socialDAO.makeFriends(other.getId(), user.getId());
        }
        socialDAO.deleteFriendRequest(other.getId(), user.getId());
    }

    public UserInfo[] listIncomingFriendRequests(String authString) {
        User user = accountValidator.verifyCredentials(authString);

        return user.getIncomingFriendRequests().parallelStream().map(UserInfo::new).toArray(UserInfo[]::new);
    }

    public UserInfo[] listOutgoingFriendRequests(String authString) {
        User user = accountValidator.verifyCredentials(authString);

        return user.getOutgoingFriendRequests().parallelStream().map(UserInfo::new).toArray(UserInfo[]::new);
    }

    public void unfriend(String authString, String otherId) {
        User user = accountValidator.verifyCredentials(authString);
        User other = accountValidator.verifyUserId(otherId);

        socialValidator.validateFriends(user, other.getId());

        socialDAO.unfriend(user.getId(), other.getId());
    }

    public void blockUser(String authString, String otherId) {
        User user = accountValidator.verifyCredentials(authString);
        User other = accountValidator.verifyUserId(otherId);

        socialValidator.validateBlockable(user, other.getId());

        socialDAO.block(user.getId(), other.getId());
        socialDAO.unfriend(user.getId(), other.getId());
        socialDAO.deleteFriendRequest(user.getId(), other.getId());
        socialDAO.deleteFriendRequest(other.getId(), user.getId());
    }

    public void unblockUser(String authString, String otherId) {
        User user = accountValidator.verifyCredentials(authString);
        User other = accountValidator.verifyUserId(otherId);

        socialValidator.validateBlocking(user, other.getId());

        socialDAO.unblock(user.getId(), other.getId());
    }

    public boolean isBlocked(String authString, String otherId) {
        User user = accountValidator.verifyCredentials(authString);
        User other = accountValidator.verifyUserId(otherId);

        return user.getBlocked()
                .parallelStream()
                .anyMatch(u -> u.getId().equals(other.getId()));
    }

    public UserInfo[] listFriends(String authString) {
        User user = accountValidator.verifyCredentials(authString);

        return Stream
                .concat(
                        user.getFriends().stream(),
                        user.getFriended().stream())
                .parallel()
                .map(UserInfo::new)
                .toArray(UserInfo[]::new);
    }

    public UserInfo[] listBlocked(String authString) {
        User user = accountValidator.verifyCredentials(authString);

        return user.getBlocking()
                .parallelStream()
                .map(UserInfo::new)
                .toArray(UserInfo[]::new);
    }

    public void sendMessage(String authString, String otherId, Text sendMessage) {
        User user = accountValidator.verifyCredentials(authString);
        User other = accountValidator.verifyUserId(otherId);
        String messageBody = DTOValidator.validateText(sendMessage);

        socialValidator.validateNotBlocked(user, other.getId());

        socialDAO.sendMessage(user.getId(), other.getId(), messageBody, RecipientType.PLAYER);
    }

    public MessageInfo[] listMessages(String authString, String senders, Long startTime, Long endTime, String read, String from) {
        User user = accountValidator.verifyCredentials(authString);
        Timestamp start = null;
        if(startTime != null) {
            start = new Timestamp(startTime);
        }
        Timestamp end = null;
        if(endTime != null) {
            end = new Timestamp(endTime);
        }
        List<UUID> users = new ArrayList<>();
        if(senders != null && senders.length() > 0) {
            for (String u : senders.split(",")) {
                users.add(DTOValidator.validateId(u));
            }
        }
        if(start != null && end != null && start.after(end)) {
            throw new GameServerException(ErrorCode.INVALID_DATE);
        }
        Read rd = DTOValidator.validateRead(read);
        From frm = DTOValidator.validateFrom(from);

        List<Message> messages = socialDAO.listMessages(user.getId(), users, start, end, rd, frm, RecipientType.PLAYER);
        for(Message message : messages) {
            if(!message.getSender().getId().equals(user.getId())) {
                socialDAO.markMessageRead(message.getId());
                message.setOpened(true);
            }
        }

        return messages.parallelStream().map(MessageInfo::new).toArray(MessageInfo[]::new);
    }

    public SocialNotifications getNotifications(String authString) {
        User user = accountValidator.verifyCredentials(authString);

        return socialDAO.getSocialNotifications(user.getId());
    }
}
