package io.joshatron.bgt.server.database;

import io.joshatron.bgt.engine.player.PlayerIndicator;
import io.joshatron.bgt.engine.state.GameState;
import io.joshatron.bgt.server.database.model.GameRequest;
import io.joshatron.bgt.server.database.model.User;
import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.request.Complete;
import io.joshatron.bgt.server.request.Pending;
import io.joshatron.bgt.server.database.model.PlayerAndIndicator;
import io.joshatron.bgt.server.request.RequestGame;
import io.joshatron.bgt.server.response.GameInfo;
import io.joshatron.bgt.server.response.GameNotifications;
import io.joshatron.bgt.server.response.RandomRequestInfo;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GameDAO {
    @Autowired
    private SessionFactory sessionFactory;

    public void createGameRequest(User requester, List<User> opponents, RequestGame gameRequest) {
        try {
            Session session = sessionFactory.getCurrentSession();
            Transaction transaction = session.beginTransaction();

            GameRequest request = new GameRequest();
            request.setRequester(requester);
            request.setParameters(gameRequest.getParameters());
            List<PlayerAndIndicator> playerList = new ArrayList<>();
            playerList.add(new PlayerAndIndicator(requester.getId(), PlayerIndicator.valueOf(gameRequest.getPlayerIndicator())));
            opponents.forEach(o -> playerList.add(new PlayerAndIndicator(o.getId(), PlayerIndicator.NONE)));
            request.setPlayers(playerList);

            session.save(request);
            transaction.commit();
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public GameRequest getGameRequestInfo(UUID request) {
        try {
            Session session = sessionFactory.getCurrentSession();
            Query<GameRequest> query = session.createQuery("from GameRequest r where r.id=:id", GameRequest.class);
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

    public List<GameRequest> getIncomingGameRequests(UUID user) {
        try {
            Session session = sessionFactory.getCurrentSession();
            Query<GameRequest> query = session.createQuery("from GameRequest r where r.requester=:requester", GameRequest.class);
            query.setParameter("requester", user);
            return query.list();
        }
        catch(HibernateException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public List<GameRequest> getOutgoingGameRequests(User user) {
        try {
            Session session = sessionFactory.getCurrentSession();
            Query<GameRequest> query = session.createQuery("from GameRequest r where r.requester=:requester", GameRequest.class);
            query.setParameter("requester", user);
            return query.list();
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
