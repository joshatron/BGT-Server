package io.joshatron.bgt.server.database;

import io.joshatron.bgt.server.database.model.User;
import io.joshatron.bgt.server.database.model.Message;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.request.From;
import io.joshatron.bgt.server.request.Read;
import io.joshatron.bgt.server.request.RecipientType;
import io.joshatron.bgt.server.response.SocialNotifications;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.sql.Timestamp;
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

    public void createFriendRequest(UUID requester, UUID other) throws GameServerException {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        User r = accountDAO.getUserFromId(requester);
        User o = accountDAO.getUserFromId(other);
        r.getOutgoingFriendRequests().add(o);
        transaction.commit();
    }

    public void deleteFriendRequest(UUID requester, UUID other) throws GameServerException {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        User r = accountDAO.getUserFromId(requester);
        User o = accountDAO.getUserFromId(other);
        r.getOutgoingFriendRequests().remove(o);
        transaction.commit();
    }

    public void makeFriends(UUID user1, UUID user2) throws GameServerException {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        User first = accountDAO.getUserFromId(user1);
        User second = accountDAO.getUserFromId(user2);
        first.getFriends().add(second);
        transaction.commit();
    }

    public void unfriend(UUID user1, UUID user2) throws GameServerException {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        User first = accountDAO.getUserFromId(user1);
        User second = accountDAO.getUserFromId(user2);
        first.getFriends().remove(second);
        second.getFriends().remove(first);
        transaction.commit();
    }

    public void block(UUID requester, UUID other) throws GameServerException {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        User r = accountDAO.getUserFromId(requester);
        User o = accountDAO.getUserFromId(other);
        r.getBlocking().add(o);
        transaction.commit();
    }

    public void unblock(UUID requester, UUID other) throws GameServerException {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        User r = accountDAO.getUserFromId(requester);
        User o = accountDAO.getUserFromId(other);
        r.getBlocking().remove(o);
        transaction.commit();
    }

    public void sendMessage(UUID requester, UUID other, String text, RecipientType recipientType) throws GameServerException {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        User r = accountDAO.getUserFromId(requester);
        User o = accountDAO.getUserFromId(other);
        Message message = new Message();
        message.setSender(r);
        message.setRecipient(o);
        message.setBody(text);
        session.save(message);
        transaction.commit();
    }

    public void markMessageRead(UUID id) throws GameServerException {
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

    public List<Message> listMessages(UUID userId, List<UUID> users, Timestamp start, Timestamp end, Read read, From from, RecipientType recipient) throws GameServerException {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Message> criteria = builder.createQuery(Message.class);
        Root<Message> root = criteria.from(Message.class);

        List<Predicate> predicates = new ArrayList<>();
        if(start != null) {
            predicates.add(builder.greaterThanOrEqualTo(root.get("sent"), start));
        }
        if(end != null) {
            predicates.add(builder.lessThanOrEqualTo(root.get("sent"), end));
        }
        switch(read) {
            case READ:
                predicates.add(builder.equal(root.get("opened"), true));
                break;
            case NOT_READ:
                predicates.add(builder.equal(root.get("opened"), false));
                break;
        }
        switch(from) {
            case ME:
                predicates.add(builder.equal(root.get("sender").get("id"), userId));
                if(users != null && !users.isEmpty()) {
                    predicates.add(getPredicateForUsers(users, builder, root, "recipient"));
                }
                break;
            case THEM:
                predicates.add(builder.equal(root.get("recipient").get("id"), userId));
                if(users != null && !users.isEmpty()) {
                    predicates.add(getPredicateForUsers(users, builder, root, "sender"));
                }
                break;
            default:
                predicates.add(builder.or(builder.equal(root.get("sender").get("id"), userId),
                                          builder.equal(root.get("recipient").get("id"), userId)));
                if(users != null && !users.isEmpty()) {
                    predicates.add(builder.or(getPredicateForUsers(users, builder, root, "sender"),
                                              getPredicateForUsers(users, builder, root, "recipient")));
                }
                break;
        }

        criteria.select(root).where(predicates.toArray(new Predicate[0]));

        return session.createQuery(criteria).getResultList();
    }

    private Predicate getPredicateForUsers(List<UUID> users, CriteriaBuilder builder, Root root, String spot) {
        Predicate predicate = builder.equal(root.get(spot).get("id"), users.get(0));
        for(int i = 1; i < users.size(); i++) {
            predicate = builder.or(predicate, builder.equal(root.get(spot).get("id"), users.get(i)));
        }

        return predicate;
    }

    public SocialNotifications getSocialNotifications(UUID userId) throws GameServerException {
        User user = accountDAO.getUserFromId(userId);
        List<Message> messages = listMessages(userId, null, null, null, Read.NOT_READ, From.THEM, null);
        return new SocialNotifications(user.getIncomingFriendRequests().size(), messages.size());
    }
}
