package io.joshatron.bgt.server.database;

import io.joshatron.bgt.server.database.model.User;
import io.joshatron.bgt.server.database.model.Message;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.request.From;
import io.joshatron.bgt.server.request.Read;
import io.joshatron.bgt.server.request.RecipientType;
import io.joshatron.bgt.server.response.SocialNotifications;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class SocialDAO {
    @Autowired
    private SessionFactory sessionFactory;
    @Autowired
    private AccountDAO accountDAO;

    public boolean friendRequestExists(UUID requester, UUID other) throws GameServerException {
        try {
            return accountDAO.getUserFromId(requester).getIncomingFriendRequests().parallelStream().anyMatch(u -> u.getId().equals(other));
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public boolean areFriends(UUID user1, UUID user2) throws GameServerException {
        try {
            User user = accountDAO.getUserFromId(user1);
            return user.getFriends().parallelStream().anyMatch(u -> u.getId().equals(user2)) ||
                   user.getFriended().parallelStream().anyMatch(u -> u.getId().equals(user2));
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
        try {
            Session session = sessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();
            Query<Message> query = session.createQuery("from Message m where m.id=:id", Message.class);
            query.setParameter("id", id);
            List<Message> messages = query.list();
            if(messages.size() > 1) {
                throw new GameServerException(ErrorCode.DATABASE_ERROR);
            }
            if(messages.isEmpty()) {
                throw new GameServerException(ErrorCode.USER_NOT_FOUND);
            }
            Message message = messages.get(0);
            message.setOpened(true);
            transaction.commit();
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public List<Message> listMessages(UUID userId, String[] users, Date start, Date end, Read read, From from, RecipientType recipient) throws GameServerException {
        return new ArrayList<>();
    }

    public SocialNotifications getSocialNotifications(UUID userId) throws GameServerException {
        try {
            User user = accountDAO.getUserFromId(userId);
            List<Message> messages = listMessages(userId, null, null, null, Read.NOT_READ, From.THEM, null);
            return new SocialNotifications(user.getIncomingFriendRequests().size(), messages.size());
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }
}
