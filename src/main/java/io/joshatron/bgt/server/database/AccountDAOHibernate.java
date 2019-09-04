package io.joshatron.bgt.server.database;

import io.joshatron.bgt.server.database.model.User;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.request.Auth;
import io.joshatron.bgt.server.response.State;
import io.joshatron.bgt.server.response.UserInfo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class AccountDAOHibernate implements AccountDAO {
    @Autowired
    private SessionFactory sessionFactory;
    @Value("${user.bcrypt-rounds:10}")
    private Integer bcryptRounds;
    @Value("${user.login-attempts:0}")
    private Integer maxFailed;

    @Override
    public boolean isAuthenticated(Auth auth) throws GameServerException {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        User user = getUser(session, auth.getUsername());

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

    private User getUser(Session session, String username) throws GameServerException {
        Query<User> query = session.createQuery("from User u where u.username=:username", User.class);
        query.setParameter("username", username);
        List<User> users = query.list();
        if(users.size() != 1) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
        return users.get(0);
    }

    @Override
    public void addUser(Auth auth) throws GameServerException {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();

        User user = new User();
        user.setUsername(auth.getUsername());
        user.setPassword(BCrypt.hashpw(auth.getPassword(), BCrypt.gensalt(bcryptRounds)));

        session.save(user);
        transaction.commit();
    }

    @Override
    public void updatePassword(String username, String password) throws GameServerException {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        User user = getUser(session, username);
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(bcryptRounds)));
        transaction.commit();
    }

    @Override
    public void updateUsername(String oldUsername, String newUsername) throws GameServerException {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        User user = getUser(session, oldUsername);
        user.setUsername(newUsername);
        transaction.commit();
    }

    @Override
    public void updateRating(String userId, int newRating) throws GameServerException {
    }

    @Override
    public void updateState(String userId, State state) throws GameServerException {

    }

    @Override
    public boolean userExists(String userId) throws GameServerException {
        return false;
    }

    @Override
    public boolean usernameExists(String username) throws GameServerException {
        return false;
    }

    @Override
    public UserInfo getUserFromId(String id) throws GameServerException {
        return null;
    }

    @Override
    public UserInfo getUserFromUsername(String username) throws GameServerException {
        return null;
    }
}
