package io.joshatron.tak.server.database;

import io.joshatron.tak.engine.game.Player;
import io.joshatron.tak.server.exceptions.ErrorCode;
import io.joshatron.tak.server.exceptions.GameServerException;
import io.joshatron.tak.server.request.Complete;
import io.joshatron.tak.server.request.Pending;
import io.joshatron.tak.server.request.Winner;
import io.joshatron.tak.server.response.GameInfo;
import io.joshatron.tak.server.response.GameNotifications;
import io.joshatron.tak.server.response.RandomRequestInfo;
import io.joshatron.tak.server.response.RequestInfo;
import io.joshatron.tak.server.utils.GameUtils;
import io.joshatron.tak.server.utils.IdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

@Component
public class GameDAOSqlite implements GameDAO {

    @Autowired
    private Connection conn;

    @Override
    public void createGameRequest(String requester, String other, int size, Player requesterColor, Player first) throws GameServerException {
        PreparedStatement stmt = null;

        String insertRequest = "INSERT INTO game_requests (requester, acceptor, size, requester_color, first) " +
                "VALUES (?,?,?,?,?);";

        try {
            stmt = conn.prepareStatement(insertRequest);
            stmt.setString(1, requester);
            stmt.setString(2, other);
            stmt.setInt(3, size);
            stmt.setString(4, requesterColor.name());
            stmt.setString(5, first.name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
        }
    }

    @Override
    public void deleteGameRequest(String requester, String other) throws GameServerException {
        PreparedStatement stmt = null;

        String deleteRequest = "DELETE FROM game_requests " +
                "WHERE requester = ? AND acceptor = ?;";

        try {
            stmt = conn.prepareStatement(deleteRequest);
            stmt.setString(1, requester);
            stmt.setString(2, other);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
        }
    }

    @Override
    public void createRandomGameRequest(String user, int size) throws GameServerException {
        PreparedStatement stmt = null;

        String insertRequest = "INSERT INTO random_requests (requester, size) " +
                "VALUES (?,?);";

        try {
            stmt = conn.prepareStatement(insertRequest);
            stmt.setString(1, user);
            stmt.setInt(2, size);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
        }
    }

    @Override
    public void deleteRandomGameRequest(String user) throws GameServerException {
        PreparedStatement stmt = null;

        String deleteRequest = "DELETE FROM random_requests " +
                "WHERE requester = ?;";

        try {
            stmt = conn.prepareStatement(deleteRequest);
            stmt.setString(1, user);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
        }
    }

    @Override
    public void startGame(String requester, String other, int size, Player requesterColor, Player first) throws GameServerException {
        PreparedStatement stmt = null;

        String insertGame = "INSERT INTO games (id, white, black, size, first, current, turns, start, last, end, winner, done) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?);";

        try {
            stmt = conn.prepareStatement(insertGame);
            stmt.setString(1, IdUtils.generateId());
            if(requesterColor == Player.WHITE) {
                stmt.setString(2, requester);
                stmt.setString(3, other);
            }
            else {
                stmt.setString(2, other);
                stmt.setString(3, requester);
            }
            stmt.setInt(4, size);
            stmt.setString(5, first.name());
            stmt.setString(6, first.name());
            stmt.setInt(7, 0);
            stmt.setLong(8, Instant.now().toEpochMilli());
            stmt.setLong(9, Instant.now().toEpochMilli());
            stmt.setInt(10, 0);
            stmt.setString(11, "NONE");
            stmt.setInt(12, 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
        }
    }

    @Override
    public void addTurn(String gameId, String text) throws GameServerException {
        PreparedStatement stmt = null;

        String insertTurn = "INSERT INTO turns (game_id, turn_order, turn) " +
                "VALUES (?,?,?);";
        String updateTurns = "UPDATE games " +
                "SET turns = ?, current = ?, last = ? " +
                "WHERE id = ?;";

        GameInfo currentGameState = getGameInfo(gameId);
        Player next = currentGameState.getCurrent() == Player.WHITE ? Player.BLACK : Player.WHITE;

        try {
            stmt = conn.prepareStatement(insertTurn);
            stmt.setString(1, gameId);
            stmt.setInt(2, currentGameState.getTurns().length);
            stmt.setString(3, text);
            stmt.executeUpdate();
            stmt = conn.prepareStatement(updateTurns);
            stmt.setInt(1, currentGameState.getTurns().length + 1);
            stmt.setString(2, next.name());
            stmt.setLong(3, Instant.now().toEpochMilli());
            stmt.setString(4, gameId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
        }
    }

    @Override
    public void finishGame(String gameId, Player winner) throws GameServerException {
        PreparedStatement stmt = null;

        String updateTurns = "UPDATE games " +
                "SET winner = ?, end = ?, done = 1, current = 'NONE' " +
                "WHERE id = ?;";

        try {
            stmt = conn.prepareStatement(updateTurns);
            stmt.setString(1, winner.name());
            stmt.setLong(2, Instant.now().toEpochMilli());
            stmt.setString(3, gameId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
        }
    }

    @Override
    public boolean playingGame(String user, String other) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String getPlaying = "SELECT done " +
                "FROM games " +
                "WHERE white = ? AND black = ?;";

        try {
            stmt = conn.prepareStatement(getPlaying);
            stmt.setString(1, user);
            stmt.setString(2, other);
            rs = stmt.executeQuery();

            while(rs.next()) {
                if(rs.getInt("done") == 0) {
                    return true;
                }
            }

            return false;
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }

    @Override
    public boolean gameRequestExists(String requester, String other) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String getRequesting = "SELECT id " +
                "FROM game_requests " +
                "WHERE requester = ? AND acceptor = ?;";

        try {
            stmt = conn.prepareStatement(getRequesting);
            stmt.setString(1, requester);
            stmt.setString(2, other);
            rs = stmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }

    @Override
    public boolean randomGameRequestExists(String user) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String getRequesting = "SELECT requester " +
                "FROM random_requests " +
                "WHERE requester = ?;";

        try {
            stmt = conn.prepareStatement(getRequesting);
            stmt.setString(1, user);
            rs = stmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }

    @Override
    public boolean gameExists(String gameId) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String getGame = "SELECT id " +
                "FROM games " +
                "WHERE id = ?;";

        try {
            stmt = conn.prepareStatement(getGame);
            stmt.setString(1, gameId);
            rs = stmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }

    @Override
    public boolean userAuthorizedForGame(String user, String gameId) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String getGame = "SELECT white, black " +
                "FROM games " +
                "WHERE id = ?;";

        try {
            stmt = conn.prepareStatement(getGame);
            stmt.setString(1, gameId);
            rs = stmt.executeQuery();

            return (rs.next() && (rs.getString("white").equalsIgnoreCase(user) || rs.getString("black").equalsIgnoreCase(user)));
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }

    @Override
    public boolean isYourTurn(String user, String gameId) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String getGame = "SELECT white, black, current " +
                "FROM games " +
                "WHERE id = ?;";

        try {
            stmt = conn.prepareStatement(getGame);
            stmt.setString(1, gameId);
            rs = stmt.executeQuery();

            if(rs.next()) {
                Player current = rs.getString("current").equalsIgnoreCase("WHITE") ? Player.WHITE : Player.BLACK;
                return ((rs.getString("white").equalsIgnoreCase(user) && current == Player.WHITE) ||
                        (rs.getString("black").equalsIgnoreCase(user) && current == Player.BLACK));
            }

            return false;
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }

    @Override
    public RequestInfo getGameRequestInfo(String requester, String other) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String getRequesting = "SELECT size, requester_color, first " +
                "FROM game_requests " +
                "WHERE requester = ? AND acceptor = ?;";

        try {
            stmt = conn.prepareStatement(getRequesting);
            stmt.setString(1, requester);
            stmt.setString(2, other);
            rs = stmt.executeQuery();

            if(rs.next()) {
                Player requesterColor = rs.getString("requester_color").equalsIgnoreCase("WHITE") ? Player.WHITE : Player.BLACK;
                Player first = rs.getString("first").equalsIgnoreCase("WHITE") ? Player.WHITE : Player.BLACK;
                return new RequestInfo(requester, other, requesterColor, first, rs.getInt("size"));
            }

            return null;
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }

    @Override
    public RequestInfo[] getIncomingGameRequests(String user) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String getRequesting = "SELECT requester, size, requester_color, first " +
                "FROM game_requests " +
                "WHERE acceptor = ?;";

        try {
            stmt = conn.prepareStatement(getRequesting);
            stmt.setString(1, user);
            rs = stmt.executeQuery();

            ArrayList<RequestInfo> requests = new ArrayList<>();
            while(rs.next()) {
                Player requesterColor = rs.getString("requester_color").equalsIgnoreCase("WHITE") ? Player.WHITE : Player.BLACK;
                Player first = rs.getString("first").equalsIgnoreCase("WHITE") ? Player.WHITE : Player.BLACK;
                requests.add(new RequestInfo(rs.getString("requester"), user, requesterColor, first, rs.getInt("size")));
            }

            return requests.toArray(new RequestInfo[0]);
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }

    @Override
    public RequestInfo[] getOutgoingGameRequests(String user) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String getRequesting = "SELECT acceptor, size, requester_color, first " +
                "FROM game_requests " +
                "WHERE requester = ?;";

        try {
            stmt = conn.prepareStatement(getRequesting);
            stmt.setString(1, user);
            rs = stmt.executeQuery();

            ArrayList<RequestInfo> requests = new ArrayList<>();
            while(rs.next()) {
                Player requesterColor = rs.getString("requester_color").equalsIgnoreCase("WHITE") ? Player.WHITE : Player.BLACK;
                Player first = rs.getString("first").equalsIgnoreCase("WHITE") ? Player.WHITE : Player.BLACK;
                requests.add(new RequestInfo(user, rs.getString("acceptor"), requesterColor, first, rs.getInt("size")));
            }

            return requests.toArray(new RequestInfo[0]);
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }

    @Override
    public int getOutgoingRandomRequestSize(String user) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String getRequesting = "SELECT size " +
                "FROM random_requests " +
                "WHERE requester = ?;";

        try {
            stmt = conn.prepareStatement(getRequesting);
            stmt.setString(1, user);
            rs = stmt.executeQuery();

            if(rs.next()) {
                return rs.getInt("size");
            }

            throw new GameServerException(ErrorCode.REQUEST_NOT_FOUND);
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }

    @Override
    public RandomRequestInfo[] getRandomGameRequests() throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String getRequest = "SELECT requester, size " +
                "FROM random_requests;";

        try {
            stmt = conn.prepareStatement(getRequest);
            rs = stmt.executeQuery();

            ArrayList<RandomRequestInfo> requests = new ArrayList<>();
            while(rs.next()) {
                requests.add(new RandomRequestInfo(rs.getString("requester"), rs.getInt("size")));
            }

            return requests.toArray(new RandomRequestInfo[0]);
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }

    @Override
    public GameInfo getGameInfo(String gameId) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String getGame = "SELECT * " +
                "FROM games " +
                "WHERE id = ?;";

        try {
            stmt = conn.prepareStatement(getGame);
            stmt.setString(1, gameId);
            rs = stmt.executeQuery();

            if(rs.next()) {
                Player first = rs.getString("first").equalsIgnoreCase("WHITE") ? Player.WHITE : Player.BLACK;
                Player current = rs.getString("current").equalsIgnoreCase("WHITE") ? Player.WHITE : Player.BLACK;
                Player winner = rs.getString("winner").equalsIgnoreCase("WHITE") ? Player.WHITE : Player.BLACK;
                boolean done = rs.getInt("done") == 1;
                String[] turns = getTurnsForGame(gameId);
                return new GameInfo(rs.getString("id"), rs.getString("white"), rs.getString("black"), rs.getInt("size"), first,
                                    current, rs.getLong("start"), rs.getLong("last"), rs.getLong("end"), winner, done, turns, null, null);
            }

            return null;
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }

    private String[] getTurnsForGame(String gameId) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String getTurns = "SELECT turn " +
                "FROM turns " +
                "WHERE game_id = ? " +
                "ORDER BY turn_order ASC;";

        try {
            stmt = conn.prepareStatement(getTurns);
            stmt.setString(1, gameId);
            rs = stmt.executeQuery();

            ArrayList<String> turns = new ArrayList<>();
            while(rs.next()) {
                turns.add(rs.getString("turn"));
            }

            return turns.toArray(new String[0]);
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }

    @Override
    public GameInfo[] listGames(String userId, String[] opponents, Date start, Date end, Complete complete, Pending pending, int[] sizes, Winner winner, Player color) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            ArrayList<GameInfo> games = new ArrayList<>();

            stmt = conn.prepareStatement(generateGameQuery(opponents, start, end, complete, pending, sizes, winner, color));
            int i = 1;
            stmt.setString(i, userId);
            i++;
            if(color == null || color == Player.NONE) {
                stmt.setString(i, userId);
                i++;
            }
            if(opponents != null && opponents.length > 0) {
                for (String opponent : opponents) {
                    stmt.setString(i, opponent);
                    i++;
                    if(color == Player.NONE) {
                        stmt.setString(i, opponent);
                        i++;
                    }
                }
            }
            if (start != null) {
                stmt.setLong(i, start.getTime());
                i++;
            }
            if (end != null) {
                stmt.setLong(i, end.getTime());
                i++;
            }
            if (pending != null && pending != Pending.BOTH) {
                stmt.setString(i, userId);
                i++;
                stmt.setString(i, userId);
                i++;
            }
            if(sizes != null && sizes.length > 0) {
                for(int size : sizes) {
                    stmt.setInt(i, size);
                    i++;
                }
            }
            if(pending != null && winner != Winner.BOTH) {
                stmt.setString(i, userId);
                i++;
                stmt.setString(i, userId);
                i++;
            }
            rs = stmt.executeQuery();

            while(rs.next()) {
                Player first = rs.getString("first").equalsIgnoreCase("WHITE") ? Player.WHITE : Player.BLACK;
                Player current = rs.getString("current").equalsIgnoreCase("WHITE") ? Player.WHITE : Player.BLACK;
                Player win = rs.getString("winner").equalsIgnoreCase("WHITE") ? Player.WHITE : Player.BLACK;
                boolean done = rs.getInt("done") == 1;
                String[] turns = getTurnsForGame(rs.getString("id"));
                games.add(new GameInfo(rs.getString("id"), rs.getString("white"), rs.getString("black"), rs.getInt("size"), first, current,
                                       rs.getLong("start"), rs.getLong("last"), rs.getLong("end"), win, done, turns, null, null));
            }

            return games.toArray(new GameInfo[0]);
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }

    private String generateGameQuery(String[] opponents, Date start, Date end, Complete complete, Pending pending, int[] sizes, Winner winner, Player color) {
        StringBuilder getGames = new StringBuilder();
        getGames.append("SELECT * ");
        getGames.append("FROM games ");
        if(color == Player.WHITE) {
            getGames.append("WHERE white = ?");
        }
        else if(color == Player.BLACK) {
            getGames.append("WHERE black = ?");
        }
        else {
            getGames.append("WHERE (white = ? OR black = ?)");
        }

        if(opponents != null && opponents.length > 0) {
            getGames.append(" AND (");
            boolean first = true;
            for (int i = 0; i < opponents.length; i++) {
               if (first) {
                   if(color == Player.WHITE) {
                       getGames.append("black = ? ");
                   }
                   else if(color == Player.BLACK) {
                       getGames.append("white = ? ");
                   }
                   else {
                       getGames.append("(white = ? OR black = ?)");
                   }
                   first = false;
               } else {
                   if(color == Player.WHITE) {
                       getGames.append(" OR black = ?");
                   }
                   else if(color == Player.BLACK) {
                       getGames.append(" OR white = ?");
                   }
                   else {
                       getGames.append(" OR (white = ? OR black = ?)");
                   }
               }
           }
           getGames.append(")");
        }

        if (start != null) {
            getGames.append(" AND start > ?");
        }

        if (end != null) {
            getGames.append(" AND start < ?");
        }

        if (complete != Complete.BOTH) {
            if (complete == Complete.COMPLETE) {
                getGames.append(" AND done = 1");
            } else {
                getGames.append(" AND done = 0");
            }
        }

        if (pending != null && pending != Pending.BOTH) {
            if(pending == Pending.PENDING) {
                getGames.append(" AND ((white = ? AND current = 'WHITE') OR (black = ? AND current = 'BLACK'))");
            }
            else {
                getGames.append(" AND ((white = ? AND current = 'BLACK') OR (black = ? AND current = 'WHITE'))");
            }
        }

        if(sizes != null && sizes.length > 0) {
            getGames.append(" AND (");
            boolean first = true;
            for (int i = 0; i < sizes.length; i++) {
               if (first) {
                   getGames.append("size = ?");
                   first = false;
               } else {
                   getGames.append(" OR size = ?");
               }
           }
           getGames.append(")");
        }

        if (winner != null && winner != Winner.BOTH) {
            if (winner == Winner.ME) {
                getGames.append(" AND ((winner = 'WHITE' AND white = ?) OR (winner = 'BLACK' AND black = ?))");
            } else {
                getGames.append(" AND ((winner = 'WHITE' AND black = ?) OR (winner = 'BLACK' AND white = ?))");
            }
        }

        getGames.append(";");

        return getGames.toString();
    }

    @Override
    public GameNotifications getGameNotifications(String userId) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String countRequests = "SELECT COUNT(*) as total " +
                "FROM game_requests " +
                "WHERE acceptor = ?;";
        String countGames = "SELECT COUNT(*) as total " +
                "FROM games " +
                "WHERE done = 0 AND (" +
                "(white = ? AND current = 'WHITE') OR " +
                "(black = ? AND current = 'BLACK'));";

        try {
            stmt = conn.prepareStatement(countRequests);
            stmt.setString(1, userId);
            rs = stmt.executeQuery();
            int requests = rs.getInt("total");
            rs.close();

            stmt = conn.prepareStatement(countGames);
            stmt.setString(1, userId);
            stmt.setString(2, userId);
            rs = stmt.executeQuery();
            int games = rs.getInt("total");

            return new GameNotifications(requests, games);
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }
}
