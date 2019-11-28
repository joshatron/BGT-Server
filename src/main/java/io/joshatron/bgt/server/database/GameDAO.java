package io.joshatron.bgt.server.database;

import io.joshatron.bgt.engine.state.GameState;
import io.joshatron.bgt.server.database.model.GameRequest;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.request.Complete;
import io.joshatron.bgt.server.request.Pending;
import io.joshatron.bgt.server.request.RequestGame;
import io.joshatron.bgt.server.response.GameInfo;
import io.joshatron.bgt.server.response.GameNotifications;
import io.joshatron.bgt.server.response.RandomRequestInfo;
import io.joshatron.bgt.server.response.RequestInfo;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Component
public class GameDAO {
    @Autowired
    private SessionFactory sessionFactory;

    public void createGameRequest(RequestGame gameRequest) {
        try {
            Session session = sessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();

            GameRequest request = new GameRequest();

            session.save(request);
            Logger.getAnonymousLogger().info(request.getId().toString());
            transaction.commit();
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public GameRequest getGameRequestInfo(UUID request) {
        try {
            Session session = sessionFactory.getCurrentSession();
            Query<GameRequest> query = session.createQuery("from User u where u.id=:id", GameRequest.class);
            query.setParameter("id", request);
            List<GameRequest> requests = query.list();
            if(requests.size() > 1) {
                throw new GameServerException(ErrorCode.DATABASE_ERROR);
            }
            if(requests.isEmpty()) {
                throw new GameServerException(ErrorCode.USER_NOT_FOUND);
            }
            return requests.get(0);
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public void deleteGameRequest(UUID gameRequest) {

    }

    public void createRandomGameRequest(UUID user) {

    }

    public void deleteRandomGameRequest(UUID user) {

    }

    public void startGame(UUID gameRequest) {

    }
    public void updateState(UUID gameId, GameState state) {

    }

    public void finishGame(UUID gameId) {

    }

    public boolean playingGame(UUID user, UUID other) {
        return false;
    }

    public boolean gameRequestExists(UUID request) {
        return false;
    }

    public boolean randomGameRequestExists(String user) {
        return false;
    }

    public boolean gameExists(String gameId) {
        return false;
    }

    public boolean userAuthorizedForGame(String user, String gameId) {
        return false;
    }

    public boolean isYourTurn(String user, String gameId) {
        return false;
    }

    public RequestInfo[] getIncomingGameRequests(String user) {
        return null;
    }

    public RequestInfo[] getOutgoingGameRequests(String user) {
        return null;
    }

    public int getOutgoingRandomRequestSize(String user) {
        return 0;
    }

    public RandomRequestInfo[] getRandomGameRequests() {
        return null;
    }

    public GameInfo getGameInfo(String gameId) {
        return null;
    }

    public GameInfo[] listGames(String userId, String[] opponents, Date start, Date end, Complete complete, Pending pending) {
        return null;
    }

    public GameNotifications getGameNotifications(String userId) {
        return null;
    }
}
