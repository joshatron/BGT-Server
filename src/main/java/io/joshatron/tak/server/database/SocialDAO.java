package io.joshatron.tak.server.database;

import io.joshatron.tak.server.exceptions.GameServerException;
import io.joshatron.tak.server.request.From;
import io.joshatron.tak.server.request.Read;
import io.joshatron.tak.server.request.RecipientType;
import io.joshatron.tak.server.response.Message;
import io.joshatron.tak.server.response.SocialNotifications;
import io.joshatron.tak.server.response.User;

import java.util.Date;

public interface SocialDAO {

    boolean friendRequestExists(String requester, String other) throws GameServerException;
    boolean areFriends(String user1, String user2) throws GameServerException;
    boolean isBlocked(String requester, String other) throws GameServerException;
    void createFriendRequest(String requester, String other) throws GameServerException;
    void deleteFriendRequest(String requester, String other) throws GameServerException;
    void makeFriends(String user1, String user2) throws GameServerException;
    void unfriend(String requester, String other) throws GameServerException;
    void block(String requester, String other) throws GameServerException;
    void unblock(String requester, String other) throws GameServerException;
    void sendMessage(String requester, String other, String text, RecipientType recipientType) throws GameServerException;
    void markMessageRead(String id) throws GameServerException;
    User[] getIncomingFriendRequests(String user) throws GameServerException;
    User[] getOutgoingFriendRequests(String user) throws GameServerException;
    User[] getFriends(String user) throws GameServerException;
    User[] getBlocking(String user) throws GameServerException;
    Message[] listMessages(String userId, String[] users, Date start, Date end, Read read, From from, RecipientType recipient) throws GameServerException;
    SocialNotifications getSocialNotifications(String userId) throws GameServerException;
}
