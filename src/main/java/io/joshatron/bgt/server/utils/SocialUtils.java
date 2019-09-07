package io.joshatron.bgt.server.utils;

import io.joshatron.bgt.server.database.model.User;
import io.joshatron.bgt.server.database.model.Message;
import io.joshatron.bgt.server.request.*;
import io.joshatron.bgt.server.database.AccountDAO;
import io.joshatron.bgt.server.database.SocialDAO;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.response.UserMessage;
import io.joshatron.bgt.server.response.SocialNotifications;
import io.joshatron.bgt.server.response.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class SocialUtils {

    @Autowired
    private SocialDAO socialDAO;
    @Autowired
    private AccountDAO accountDAO;

    public void createFriendRequest(Auth auth, String other) throws GameServerException {
        Validator.validateAuth(auth);
        UUID otherId = Validator.validateId(other);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());
        if(!accountDAO.userExists(otherId)) {
            throw new GameServerException(ErrorCode.USER_NOT_FOUND);
        }
        if(socialDAO.isBlocked(user.getId(), otherId)) {
            throw new GameServerException(ErrorCode.BLOCKED);
        }
        if(socialDAO.isBlocked(otherId, user.getId())) {
            throw new GameServerException(ErrorCode.BLOCKING);
        }
        if(socialDAO.friendRequestExists(user.getId(), otherId)) {
            throw new GameServerException(ErrorCode.ALREADY_REQUESTING);
        }
        if(socialDAO.areFriends(user.getId(), otherId)) {
            throw new GameServerException(ErrorCode.ALREADY_FRIENDS);
        }
        if(user.getId().equals(otherId)) {
            throw new GameServerException(ErrorCode.REQUESTING_SELF);
        }

        socialDAO.createFriendRequest(user.getId(), otherId);
    }

    public void deleteFriendRequest(Auth auth, String other) throws GameServerException {
        Validator.validateAuth(auth);
        UUID otherId = Validator.validateId(other);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());
        if(!accountDAO.userExists(otherId)) {
            throw new GameServerException(ErrorCode.USER_NOT_FOUND);
        }
        if(!socialDAO.friendRequestExists(user.getId(), otherId)) {
            throw new GameServerException(ErrorCode.REQUEST_NOT_FOUND);
        }

        socialDAO.deleteFriendRequest(user.getId(), otherId);
    }

    public void respondToFriendRequest(Auth auth, String other, Text answer) throws GameServerException {
        Validator.validateAuth(auth);
        UUID otherId = Validator.validateId(other);
        Validator.validateText(answer);
        Answer response = Validator.validateAnswer(answer.getText());
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());
        if(!accountDAO.userExists(otherId)) {
            throw new GameServerException(ErrorCode.USER_NOT_FOUND);
        }
        if(!socialDAO.friendRequestExists(user.getId(), otherId)) {
            throw new GameServerException(ErrorCode.REQUEST_NOT_FOUND);
        }

        if(response == Answer.ACCEPT) {
            socialDAO.makeFriends(otherId, user.getId());
        }
        socialDAO.deleteFriendRequest(otherId, user.getId());
    }

    public UserInfo[] listIncomingFriendRequests(Auth auth) throws GameServerException {
        Validator.validateAuth(auth);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());

        return user.getIncomingFriendRequests().parallelStream().map(UserInfo::new).toArray(UserInfo[]::new);
    }

    public UserInfo[] listOutgoingFriendRequests(Auth auth) throws GameServerException {
        Validator.validateAuth(auth);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());

        return user.getOutgoingFriendRequests().parallelStream().map(UserInfo::new).toArray(UserInfo[]::new);
    }

    public void unfriend(Auth auth, String other) throws GameServerException {
        Validator.validateAuth(auth);
        UUID otherId = Validator.validateId(other);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());
        if(!accountDAO.userExists(otherId)) {
            throw new GameServerException(ErrorCode.USER_NOT_FOUND);
        }
        if(!socialDAO.areFriends(user.getId(), otherId)) {
            throw new GameServerException(ErrorCode.FRIEND_NOT_FOUND);
        }

        socialDAO.unfriend(user.getId(), otherId);
    }

    public void blockUser(Auth auth, String other) throws GameServerException {
        Validator.validateAuth(auth);
        UUID otherId = Validator.validateId(other);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());
        if(!accountDAO.userExists(otherId)) {
            throw new GameServerException(ErrorCode.USER_NOT_FOUND);
        }
        if(socialDAO.isBlocked(otherId, user.getId())) {
            throw new GameServerException(ErrorCode.ALREADY_BLOCKED);
        }
        if(user.getId().equals(otherId)) {
            throw new GameServerException(ErrorCode.BLOCKING_SELF);
        }

        socialDAO.block(user.getId(), otherId);
        if(socialDAO.areFriends(user.getId(), otherId)) {
            socialDAO.unfriend(user.getId(), otherId);
        }
        if(socialDAO.friendRequestExists(user.getId(), otherId)) {
            socialDAO.deleteFriendRequest(user.getId(), otherId);
        }
        if(socialDAO.friendRequestExists(otherId, user.getId())) {
            socialDAO.deleteFriendRequest(otherId, user.getId());
        }
    }

    public void unblockUser(Auth auth, String other) throws GameServerException {
        Validator.validateAuth(auth);
        UUID otherId = Validator.validateId(other);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());
        if(!accountDAO.userExists(otherId)) {
            throw new GameServerException(ErrorCode.USER_NOT_FOUND);
        }
        if(!socialDAO.isBlocked(otherId, user.getId())) {
            throw new GameServerException(ErrorCode.NOT_BLOCKED);
        }

        socialDAO.unblock(user.getId(), otherId);
    }

    public boolean isBlocked(Auth auth, String other) throws GameServerException {
        Validator.validateAuth(auth);
        UUID otherId = Validator.validateId(other);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());
        if(!accountDAO.userExists(otherId)) {
            throw new GameServerException(ErrorCode.USER_NOT_FOUND);
        }

        return socialDAO.isBlocked(user.getId(), otherId);
    }

    public UserInfo[] listFriends(Auth auth) throws GameServerException {
        Validator.validateAuth(auth);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());

        return user.getFriends().parallelStream().map(UserInfo::new).toArray(UserInfo[]::new);
    }

    public UserInfo[] listBlocked(Auth auth) throws GameServerException {
        Validator.validateAuth(auth);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());

        return user.getFriends().parallelStream().map(UserInfo::new).toArray(UserInfo[]::new);
    }

    public void sendMessage(Auth auth, String other, Text sendMessage) throws GameServerException {
        Validator.validateAuth(auth);
        UUID otherId = Validator.validateId(other);
        Validator.validateText(sendMessage);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());
        if(!accountDAO.userExists(otherId)) {
            throw new GameServerException(ErrorCode.USER_NOT_FOUND);
        }
        if(socialDAO.isBlocked(user.getId(), otherId)) {
            throw new GameServerException(ErrorCode.BLOCKED);
        }
        if(sendMessage.getText().length() == 0) {
            throw new GameServerException(ErrorCode.EMPTY_FIELD);
        }
        if(sendMessage.getText().length() > 5000) {
            throw new GameServerException(ErrorCode.MESSAGE_TOO_LONG);
        }

        socialDAO.sendMessage(user.getId(), otherId, sendMessage.getText(), RecipientType.PLAYER);
    }

    public UserMessage[] listMessages(Auth auth, String senders, Long startTime, Long endTime, String read, String from) throws GameServerException {
        Validator.validateAuth(auth);
        Date start = null;
        if(startTime != null) {
            start = new Date(startTime);
        }
        Date end = null;
        if(endTime != null) {
            end = new Date(endTime);
        }
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());
        Read rd = Validator.validateRead(read);
        String[] users = null;
        if(senders != null && senders.length() > 0) {
            users = senders.split(",");
            for (String u : users) {
                Validator.validateId(u);
            }
        }
        if(start != null && end != null && start.after(end)) {
            throw new GameServerException(ErrorCode.INVALID_DATE);
        }
        From frm = Validator.validateFrom(from);

        List<Message> messages = socialDAO.listMessages(user.getId(), users, start, end, rd, frm, RecipientType.PLAYER);
        for(Message message : messages) {
            if(!message.getSender().getId().equals(user.getId())) {
                socialDAO.markMessageRead(message.getId());
                message.setOpened(true);
            }
        }

        return messages.parallelStream().map(UserMessage::new).toArray(UserMessage[]::new);
    }

    public SocialNotifications getNotifications(Auth auth) throws GameServerException {
        Validator.validateAuth(auth);
        if(!accountDAO.isAuthenticated(auth)) {
            throw new GameServerException(ErrorCode.INCORRECT_AUTH);
        }
        User user = accountDAO.getUserFromUsername(auth.getUsername());

        return socialDAO.getSocialNotifications(user.getId());
    }
}
