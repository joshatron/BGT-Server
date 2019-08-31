package io.joshatron.bgt.server.database;

import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import org.springframework.context.annotation.Bean;

import java.sql.*;

public class SqliteManager {

    private SqliteManager() {
        throw new IllegalStateException("This is a utility class");
    }

    @Bean
    public static Connection getConnection() throws GameServerException {
        String database = "server.db";

        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + database);
            initializeDatabase(conn);

            return conn;
        }
        catch(SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    private static void initializeDatabase(Connection conn) throws GameServerException {
        String userTable = "CREATE TABLE IF NOT EXISTS users (" +
                //ID of the user, alphanumeric
                "id text PRIMARY KEY COLLATE NOCASE," +
                //username, alphanumeric
                "username text UNIQUE NOT NULL COLLATE NOCASE," +
                //hashed password
                "auth text NOT NULL," +
                //current elo ranking
                "rating integer NOT NULL," +
                //Number of invalid login attempts since last successful
                "failed integer NOT NULL," +
                //State of the user, either normal, locked, or banned
                "state text NOT NULL," +
                //time when the user did something last
                "last integer NOT NULL);";

        String friendTable = "CREATE TABLE IF NOT EXISTS friends (" +
                //ID of friend mapping, for internal use
                "id integer PRIMARY KEY AUTOINCREMENT," +
                //ID of the user requesting
                "requester text NOT NULL," +
                //ID of the user who accepted
                "acceptor text NOT NULL);";

        String blockedTable = "CREATE TABLE IF NOT EXISTS blocked (" +
                //ID of the blocking mapping, for internal use
                "id integer PRIMARY KEY AUTOINCREMENT," +
                //ID of the user who requests it
                "requester text NOT NULL," +
                //ID of the user who is blocked
                "blocked text NOT NULL);";

        String friendRequestsTable = "CREATE TABLE IF NOT EXISTS friend_requests (" +
                //ID of the friend request, for internal use
                "id integer PRIMARY KEY AUTOINCREMENT," +
                //ID of the user who requested
                "requester text NOT NULL," +
                //ID of the user who will accept of deny
                "acceptor text NOT NULL);";

        String messagesTable = "CREATE TABLE IF NOT EXISTS messages(" +
                //ID of the message alphanumeric
                "id text PRIMARY KEY," +
                //ID of the user who sent it
                "sender text NOT NULL," +
                //ID of the the recipient
                "recipient text NOT NULL," +
                //What type the message was sent to, either PLAYER or GAME
                "recipientType text NOT NULL," +
                //Content of the message
                "message text NOT NULL," +
                //Timestamp when it was sent
                "time integer NOT NULL," +
                //Whether the message has been marked read, 0 for unread, 1 for read
                "opened integer NOT NULL);";

        String randomRequestsTable = "CREATE TABLE IF NOT EXISTS random_requests (" +
                //ID of the user requesting the game
                "requester text PRIMARY KEY," +
                //Size of the game board to play on
                "size integer NOT NULL);";

        String gameRequestsTable = "CREATE TABLE IF NOT EXISTS game_requests (" +
                //ID of the game request, for internal use
                "id integer PRIMARY KEY AUTOINCREMENT," +
                //ID of the user requesting
                "requester text NOT NULL," +
                //ID of the user to accept or deny the game
                "acceptor text NOT NULL," +
                //Size of the game board
                "size integer NOT NULL," +
                //Color of the requesting user, either WHITE or BLACK
                "requester_color text NOT NULL," +
                //Color of the user who will go first, either WHITE or BLACK
                "first text NOT NULL);";

        String gamesTable = "CREATE TABLE IF NOT EXISTS games (" +
                //Game ID
                "id text PRIMARY KEY," +
                //User ID of white player
                "white text NOT NULL," +
                //User ID of black player
                "black text NOT NULL," +
                //Board size
                "size integer NOT NULL," +
                //The color who goes first, either WHITE or BLACK
                "first text NOT NULL," +
                //The color who's turn it is, either WHITE or BLACK
                "current text NOT NULL," +
                //How many turns have happened
                "turns integer NOT NULL," +
                //The timestamp of when the game started
                "start integer NOT NULL," +
                //The timestamp of when a move was last made
                "last integer NOT NULL," +
                //The timestamp of when the game ended, 0 if not done
                "end integer," +
                //The color who's won the game, either WHITE, BLACK, or NONE for tie or unfinished game
                "winner text NOT NULL," +
                //Whether the game is done or not, 0 for not done, 1 for done
                "done integer NOT NULL);";

        String turnsTable = "CREATE TABLE IF NOT EXISTS turns (" +
                //ID of the turn, for internal use
                "id integer PRIMARY KEY AUTOINCREMENT," +
                //ID of the game
                "game_id text NOT NULL," +
                //Order the turn is in, starting at 0
                "turn_order integer NOT NULL," +
                //String representation of the turn
                "turn text NOT NULL);";

        String configTable = "CREATE TABLE IF NOT EXISTS config (" +
                //Field name for the config
                "field text PRIMARY KEY," +
                //Value of the config
                "value text NOT NULL);";

        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(userTable);
            stmt.executeUpdate(friendTable);
            stmt.executeUpdate(blockedTable);
            stmt.executeUpdate(friendRequestsTable);
            stmt.executeUpdate(messagesTable);
            stmt.executeUpdate(randomRequestsTable);
            stmt.executeUpdate(gameRequestsTable);
            stmt.executeUpdate(gamesTable);
            stmt.executeUpdate(turnsTable);
            stmt.executeUpdate(configTable);
            stmt.close();

        } catch (SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        }
    }

    public static void closeStatement(PreparedStatement stmt) throws GameServerException {
        if(stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                throw new GameServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    public static void closeResultSet(ResultSet rs) throws GameServerException {
        if(rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new GameServerException(ErrorCode.DATABASE_ERROR);
            }
        }
    }
}
