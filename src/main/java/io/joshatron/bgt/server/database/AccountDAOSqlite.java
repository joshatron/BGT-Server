package io.joshatron.bgt.server.database;

import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.request.Auth;
import io.joshatron.bgt.server.response.State;
import io.joshatron.bgt.server.response.UserInfo;
import io.joshatron.bgt.server.utils.IdUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

@Component
public class AccountDAOSqlite implements AccountDAO {

    @Autowired
    private Connection conn;

    @Value("${user.bcrypt-rounds:10}")
    private Integer bcryptRounds;
    @Value("${user.login-attempts:0}")
    private Integer maxFailed;

    @Override
    public boolean isAuthenticated(Auth auth) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String getAuth = "SELECT id, username, auth, failed, state " +
                "FROM users " +
                "WHERE username = ?;";

        try {
            stmt = conn.prepareStatement(getAuth);
            stmt.setString(1, auth.getUsername());
            rs = stmt.executeQuery();

            if(!rs.next()) {
                return false;
            }

            State state = State.valueOf(rs.getString("state"));

            if(state == State.LOCKED) {
                throw new GameServerException(ErrorCode.LOCKED_OUT);
            }
            else if(state == State.BANNED) {
                throw new GameServerException(ErrorCode.BANNED);
            }

            boolean authorized = (auth.getUsername().equals(rs.getString("username")) &&
                    BCrypt.checkpw(auth.getPassword(), rs.getString("auth")));

            if(authorized) {
                updateLast(rs.getString("id"));
                updateFailed(rs.getString("id"), 0);
            }
            else {
                if(maxFailed > 0 && rs.getInt("failed") + 1 >= maxFailed) {
                    updateState(rs.getString("id"), State.LOCKED);
                }
                updateFailed(rs.getString("id"), rs.getInt("failed") + 1);
            }

            return authorized;
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }

    public void updateState(String userId, State state) throws GameServerException {
        PreparedStatement stmt = null;

        String updateLast = "UPDATE users " +
                "SET state = ? " +
                "WHERE id = ?;";

        try {
            stmt = conn.prepareStatement(updateLast);
            stmt.setString(1, state.name());
            stmt.setString(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
        }
    }

    private void updateFailed(String userId, int num) throws GameServerException {
        PreparedStatement stmt = null;

        String updateLast = "UPDATE users " +
                "SET failed = ? " +
                "WHERE id = ?;";

        try {
            stmt = conn.prepareStatement(updateLast);
            stmt.setInt(1, num);
            stmt.setString(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
        }
    }

    private void updateLast(String userId) throws GameServerException {
        PreparedStatement stmt = null;

        String updateLast = "UPDATE users " +
                "SET last = ? " +
                "WHERE id = ?;";

        try {
            stmt = conn.prepareStatement(updateLast);
            stmt.setLong(1, Instant.now().toEpochMilli());
            stmt.setString(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
        }
    }

    @Override
    public void addUser(Auth auth) throws GameServerException {
        PreparedStatement stmt = null;

        //insert new user if it isn't
        String insertUser = "INSERT INTO  users (username, auth, id, last, rating, failed, state) " +
                "VALUES (?,?,?,?,1000,0,\"NORMAL\");";

        try {
            stmt = conn.prepareStatement(insertUser);
            stmt.setString(1, auth.getUsername());
            stmt.setString(2, BCrypt.hashpw(auth.getPassword(), BCrypt.gensalt(bcryptRounds)));
            stmt.setString(3, IdUtils.generateId());
            stmt.setLong(4, Instant.now().toEpochMilli());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
        }
    }

    @Override
    public void updatePassword(String username, String password) throws GameServerException {
        PreparedStatement stmt = null;

        String changePass = "UPDATE users " +
                "SET auth = ? " +
                "WHERE username = ?;";

        try {
            stmt = conn.prepareStatement(changePass);
            stmt.setString(1, BCrypt.hashpw(password, BCrypt.gensalt(bcryptRounds)));
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
        }
    }

    @Override
    public void updateUsername(String oldUsername, String newUsername) throws GameServerException {
        PreparedStatement stmt = null;

        String changeUser = "UPDATE users " +
                "SET username = ? " +
                "WHERE username = ?;";

        try {
            stmt = conn.prepareStatement(changeUser);
            stmt.setString(1, newUsername);
            stmt.setString(2, oldUsername);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
        }
    }

    @Override
    public void updateRating(String userId, int newRating) throws GameServerException {
        PreparedStatement stmt = null;

        String changeUser = "UPDATE users " +
                "SET rating = ? " +
                "WHERE id = ?;";

        try {
            stmt = conn.prepareStatement(changeUser);
            stmt.setInt(1, newRating);
            stmt.setString(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
        }
    }

    @Override
    public boolean userExists(String userId) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String checkUsername = "SELECT username " +
                "FROM users " +
                "WHERE id = ?;";

        try {
            stmt = conn.prepareStatement(checkUsername);
            stmt.setString(1, userId);
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
    public boolean usernameExists(String username) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String checkUsername = "SELECT username " +
                "FROM users " +
                "WHERE username = ?;";

        try {
            stmt = conn.prepareStatement(checkUsername);
            stmt.setString(1, username);
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
    public UserInfo getUserFromId(String id) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String checkUsername = "SELECT id, username, rating, state " +
                "FROM users " +
                "WHERE id = ?;";

        try {
            stmt = conn.prepareStatement(checkUsername);
            stmt.setString(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return new UserInfo(rs.getString("username"), rs.getString("id"), rs.getInt("rating"), State.valueOf(rs.getString("state")));
            }
            else {
                throw new GameServerException(ErrorCode.USER_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }

    @Override
    public UserInfo getUserFromUsername(String username) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String checkUsername = "SELECT id, username, rating, state " +
                "FROM users " +
                "WHERE username = ?;";

        try {
            stmt = conn.prepareStatement(checkUsername);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return new UserInfo(rs.getString("username"), rs.getString("id"), rs.getInt("rating"), State.valueOf(rs.getString("state")));
            }
            else {
                throw new GameServerException(ErrorCode.USER_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }

    public Connection getConnection() {
        return conn;
    }
}
