package io.joshatron.bgt.server.database;

import io.joshatron.bgt.server.database.model.Config;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.request.Auth;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminDAO {
    @Autowired
    private SessionFactory sessionFactory;
    private static final String ADMIN_PASS_FIELD = "admin-pass";

    public boolean isInitialized() throws GameServerException {
        try {
            Session session = sessionFactory.getCurrentSession();
            Query<Config> query = session.createQuery("from Config c where c.key=:key", Config.class);
            query.setParameter("key", ADMIN_PASS_FIELD);

            return !query.list().isEmpty();
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public boolean isAuthenticated(Auth auth) throws GameServerException {
        try {
            Session session = sessionFactory.getCurrentSession();
            Query<Config> query = session.createQuery("from Config c where c.key=:key", Config.class);
            query.setParameter("key", ADMIN_PASS_FIELD);
            List<Config> configs = query.list();
            if(configs.size() != 1) {
                return false;
            }

            return (auth.getUsername().equals("admin") && BCrypt.checkpw(auth.getPassword(), configs.get(0).getValue()));
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public void updatePassword(String newPass) throws GameServerException {
        try {
            Session session = sessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();
            Query<Config> query = session.createQuery("from Config c where c.key=:key", Config.class);
            query.setParameter("key", ADMIN_PASS_FIELD);
            List<Config> configs = query.list();
            if(configs.size() > 1) {
                throw new GameServerException(ErrorCode.DATABASE_ERROR);
            }
            if(configs.isEmpty()) {
                Config config = new Config(ADMIN_PASS_FIELD, BCrypt.hashpw(newPass, BCrypt.gensalt()));
                session.save(config);
            }
            else {
                configs.get(0).setValue(BCrypt.hashpw(newPass, BCrypt.gensalt()));
            }
            transaction.commit();
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }
}
