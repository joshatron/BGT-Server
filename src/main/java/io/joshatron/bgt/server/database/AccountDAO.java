package io.joshatron.bgt.server.database;

import io.joshatron.bgt.server.database.model.User;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.request.Auth;
import io.joshatron.bgt.server.request.NewUser;
import io.joshatron.bgt.server.response.State;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AccountDAO {
    @Autowired
    private SessionFactory sessionFactory;
    @Value("${user.bcrypt-rounds:10}")
    private Integer bcryptRounds;
    @Value("${user.login-attempts:0}")
    private Integer maxFailed;

    public boolean isAuthenticated(Auth auth) {
        try {
            Session session = sessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();
            User user;
            try {
                user = getUserFromUsername(session, auth.getUsername());
            } catch(GameServerException e) {
                return false;
            }

            if(user.getState() == State.LOCKED) {
                throw new GameServerException(ErrorCode.LOCKED_OUT);
            } else if(user.getState() == State.BANNED) {
                throw new GameServerException(ErrorCode.BANNED);
            }

            boolean authorized = (auth.getUsername().equals(user.getUsername())) &&
                    BCrypt.checkpw(auth.getPassword(), user.getPassword());

            if(authorized) {
                user.updateLastActivity();
                user.setLoginsFailed(0);
            } else {
                if(maxFailed > 0 && user.getLoginsFailed() + 1 >= maxFailed) {
                    user.setState(State.LOCKED);
                }
                user.incrementFailed();
            }
            transaction.commit();

            return authorized;
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public void createUser(NewUser newUser) {
        try {
            Session session = sessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();

            User user = new User();
            user.setUsername(newUser.getUsername());
            user.setPassword(BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt(bcryptRounds)));

            session.save(user);
            transaction.commit();
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public void updatePassword(UUID userId, String password) {
        try {
            Session session = sessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();
            User user = getUserFromId(session, userId);
            user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(bcryptRounds)));
            transaction.commit();
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public void updateUsername(UUID userId, String username) {
        try {
            Session session = sessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();
            User user = getUserFromId(session, userId);
            user.setUsername(username);
            transaction.commit();
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public void updateRating(UUID userId, int rating) {
        try {
            Session session = sessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();
            User user = getUserFromId(session, userId);
            user.setRating(rating);
            transaction.commit();
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public void updateState(UUID userId, State state) {
        try {
            Session session = sessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();
            User user = getUserFromId(session, userId);
            user.setState(state);
            transaction.commit();
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public boolean userExists(UUID userId) {
        try {
            getUserFromId(userId);
            return true;
        } catch(GameServerException e) {
            return false;
        }
    }

    public boolean usernameExists(String username) {
        try {
            getUserFromUsername(username);
            return true;
        } catch(GameServerException e) {
            return false;
        }
    }

    public User getUserFromId(UUID id) {
        try {
            Session session = sessionFactory.getCurrentSession();
            return getUserFromId(session, id);
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    private User getUserFromId(Session session, UUID userId) {
        Query<User> query = session.createQuery("from User u where u.id=:id", User.class);
        query.setParameter("id", userId);
        List<User> users = query.list();
        if(users.size() > 1) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
        if(users.isEmpty()) {
            throw new GameServerException(ErrorCode.USER_NOT_FOUND);
        }
        return users.get(0);
    }

    public User getUserFromUsername(String username) {
        try {
            Session session = sessionFactory.getCurrentSession();
            return getUserFromUsername(session, username);
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    private User getUserFromUsername(Session session, String username) {
        Query<User> query = session.createQuery("from User u where lower(u.username)=:username", User.class);
        query.setParameter("username", username.toLowerCase());
        List<User> users = query.list();
        if(users.size() > 1) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
        if(users.isEmpty()) {
            throw new GameServerException(ErrorCode.USER_NOT_FOUND);
        }
        return users.get(0);
    }
}
