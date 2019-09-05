package io.joshatron.bgt.server.database;

import io.joshatron.bgt.server.database.model.User;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.request.Auth;
import io.joshatron.bgt.server.response.State;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountDAO {
    @Autowired
    private SessionFactory sessionFactory;
    @Value("${user.bcrypt-rounds:10}")
    private Integer bcryptRounds;
    @Value("${user.login-attempts:0}")
    private Integer maxFailed;

    public boolean isAuthenticated(Auth auth) throws GameServerException {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        User user = getUserFromId(session, auth.getUsername());

        if(user.getState() == State.LOCKED) {
            throw new GameServerException(ErrorCode.LOCKED_OUT);
        }
        else if(user.getState() == State.BANNED) {
            throw new GameServerException(ErrorCode.BANNED);
        }

        boolean authorized = (auth.getUsername().equals(user.getUsername())) &&
                BCrypt.checkpw(auth.getPassword(), user.getPassword());

        if(authorized) {
            user.updateLastActivity();
            user.setLoginsFailed(0);
        }
        else {
            if(maxFailed > 0 && user.getLoginsFailed() + 1 >= maxFailed) {
                user.setState(State.LOCKED);
            }
            user.incrementFailed();
        }
        transaction.commit();

        return authorized;
    }

    public void createUser(Auth auth) throws GameServerException {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();

        User user = new User();
        user.setUsername(auth.getUsername());
        user.setPassword(BCrypt.hashpw(auth.getPassword(), BCrypt.gensalt(bcryptRounds)));

        session.save(user);
        transaction.commit();
    }

    public void updatePassword(String userId, String password) throws GameServerException {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        User user = getUserFromId(session, userId);
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(bcryptRounds)));
        transaction.commit();
    }

    public void updateUsername(String userId, String username) throws GameServerException {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        User user = getUserFromId(session, userId);
        user.setUsername(username);
        transaction.commit();
    }

    public void updateRating(String userId, int rating) throws GameServerException {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        User user = getUserFromId(session, userId);
        user.setRating(rating);
        transaction.commit();
    }

    public void updateState(String userId, State state) throws GameServerException {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        User user = getUserFromId(session, userId);
        user.setState(state);
        transaction.commit();
    }

    public boolean userExists(String userId) throws GameServerException {
        try {
            getUserFromId(userId);
            return true;
        } catch(GameServerException e) {
            return false;
        }
    }

    public boolean usernameExists(String username) throws GameServerException {
        try {
            getUserFromUsername(username);
            return true;
        } catch(GameServerException e) {
            return false;
        }
    }

    public User getUserFromId(String id) throws GameServerException {
        Session session = sessionFactory.getCurrentSession();
        return getUserFromId(session, id);
    }

    private User getUserFromId(Session session, String userId) throws GameServerException {
        Query<User> query = session.createQuery("from User u where u.id=:id", User.class);
        query.setParameter("id", userId);
        List<User> users = query.list();
        if(users.size() != 1) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
        return users.get(0);
    }

    public User getUserFromUsername(String username) throws GameServerException {
        Session session = sessionFactory.getCurrentSession();
        return getUserFromUsername(session, username);
    }

    private User getUserFromUsername(Session session, String username) throws GameServerException {
        Query<User> query = session.createQuery("from User u where u.username=:username", User.class);
        query.setParameter("username", username);
        List<User> users = query.list();
        if(users.size() != 1) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
        return users.get(0);
    }
}
