package io.joshatron.bgt.server.database;

import io.joshatron.bgt.server.database.model.User;
import io.joshatron.bgt.server.database.model.UserMessage;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.request.From;
import io.joshatron.bgt.server.request.Read;
import io.joshatron.bgt.server.request.RecipientType;
import io.joshatron.bgt.server.response.Message;
import io.joshatron.bgt.server.response.SocialNotifications;
import io.joshatron.bgt.server.response.UserInfo;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SocialDAO {
    @Autowired
    private SessionFactory sessionFactory;
    @Autowired
    private AccountDAO accountDAO;

    public boolean friendRequestExists(UUID requester, UUID other) throws GameServerException {
        try {
            return accountDAO.getUserFromId(requester).getOutgoingFriendRequests().parallelStream().anyMatch(u -> u.getId().equals(other));
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public boolean areFriends(UUID user1, UUID user2) throws GameServerException {
        try {
            return accountDAO.getUserFromId(user1).getFriends().parallelStream().anyMatch(u -> u.getId().equals(user2));
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public boolean isBlocked(UUID requester, UUID other) throws GameServerException {
        try {
            return accountDAO.getUserFromId(other).getBlocking().parallelStream().anyMatch(u -> u.getId().equals(requester));
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public void createFriendRequest(UUID requester, UUID other) throws GameServerException {
        try {
            Session session = sessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();
            User r = accountDAO.getUserFromId(requester);
            User o = accountDAO.getUserFromId(other);
            r.getOutgoingFriendRequests().add(o);
            o.getIncomingFriendRequests().add(r);
            transaction.commit();
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public void deleteFriendRequest(UUID requester, UUID other) throws GameServerException {
        try {
            Session session = sessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();
            User r = accountDAO.getUserFromId(requester);
            User o = accountDAO.getUserFromId(other);
            r.getOutgoingFriendRequests().remove(o);
            o.getIncomingFriendRequests().remove(r);
            transaction.commit();
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public void makeFriends(UUID user1, UUID user2) throws GameServerException {
        try {
            Session session = sessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();
            User first = accountDAO.getUserFromId(user1);
            User second = accountDAO.getUserFromId(user2);
            first.getFriends().add(second);
            transaction.commit();
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public void unfriend(UUID user1, UUID user2) throws GameServerException {
        try {
            Session session = sessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();
            User first = accountDAO.getUserFromId(user1);
            User second = accountDAO.getUserFromId(user2);
            first.getFriends().remove(second);
            transaction.commit();
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public void block(UUID requester, UUID other) throws GameServerException {
        try {
            Session session = sessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();
            User r = accountDAO.getUserFromId(requester);
            User o = accountDAO.getUserFromId(other);
            r.getBlocking().add(o);
            transaction.commit();
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public void unblock(UUID requester, UUID other) throws GameServerException {
        try {
            Session session = sessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();
            User r = accountDAO.getUserFromId(requester);
            User o = accountDAO.getUserFromId(other);
            r.getBlocking().add(o);
            transaction.commit();
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public void sendMessage(UUID requester, UUID other, String text, RecipientType recipientType) throws GameServerException {

    }

    public void markMessageRead(UUID id) throws GameServerException {

    }

    public List<UserMessage> listMessages(UUID userId, String[] users, Date start, Date end, Read read, From from, RecipientType recipient) throws GameServerException {
        return null;
    }

    public SocialNotifications getSocialNotifications(UUID userId) throws GameServerException {
        return null;
    }
}
