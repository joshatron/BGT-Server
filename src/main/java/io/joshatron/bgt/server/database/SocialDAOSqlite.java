package io.joshatron.bgt.server.database;

import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.request.From;
import io.joshatron.bgt.server.request.Read;
import io.joshatron.bgt.server.request.RecipientType;
import io.joshatron.bgt.server.response.Message;
import io.joshatron.bgt.server.response.SocialNotifications;
import io.joshatron.bgt.server.response.State;
import io.joshatron.bgt.server.response.UserInfo;
import io.joshatron.bgt.server.utils.IdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

@Component
public class SocialDAOSqlite {
    private Connection conn;

    public void sendMessage(String requester, String other, String text, RecipientType recipientType) throws GameServerException {
        PreparedStatement stmt = null;

        String insertRequest = "INSERT INTO messages (sender, recipient, recipientType, message, time, opened, id) " +
                "VALUES (?,?,?,?,?,0,?);";

        try {
            stmt = conn.prepareStatement(insertRequest);
            stmt.setString(1, requester);
            stmt.setString(2, other);
            stmt.setString(3, recipientType.name());
            stmt.setString(4, text);
            stmt.setLong(5, Instant.now().toEpochMilli());
            stmt.setString(6, IdUtils.generateId());
            stmt.executeUpdate();

        } catch(SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
        }
    }


    public void markMessageRead(String id) throws GameServerException {
        PreparedStatement stmt = null;

        String markRead = "UPDATE messages " +
                "SET opened = 1 " +
                "WHERE id = ?;";

        try {
            stmt = conn.prepareStatement(markRead);
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch(SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
        }
    }


    public UserInfo[] getIncomingFriendRequests(String user) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String getIncoming = "SELECT users.username as username, users.rating as rating, users.state as state, requester " +
                "FROM friend_requests " +
                "LEFT OUTER JOIN users on friend_requests.requester = users.id " +
                "WHERE acceptor = ?;";

        try {
            stmt = conn.prepareStatement(getIncoming);
            stmt.setString(1, user);
            rs = stmt.executeQuery();

            ArrayList<UserInfo> users = new ArrayList<>();
            while(rs.next()) {
                users.add(new UserInfo(rs.getString("username"), rs.getString("requester"), rs.getInt("rating"), State.valueOf(rs.getString("state"))));
            }

            return users.toArray(new UserInfo[0]);
        } catch(SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }


    public UserInfo[] getOutgoingFriendRequests(String user) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String getOutgoing = "SELECT users.username as username, users.rating as rating, users.state as state, acceptor " +
                "FROM friend_requests " +
                "LEFT OUTER JOIN users on friend_requests.acceptor = users.id " +
                "WHERE requester = ?;";

        try {
            stmt = conn.prepareStatement(getOutgoing);
            stmt.setString(1, user);
            rs = stmt.executeQuery();

            ArrayList<UserInfo> users = new ArrayList<>();
            while(rs.next()) {
                users.add(new UserInfo(rs.getString("username"), rs.getString("acceptor"), rs.getInt("rating"), State.valueOf(rs.getString("state"))));
            }

            return users.toArray(new UserInfo[0]);
        } catch(SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }


    public UserInfo[] getFriends(String user) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String getIncoming = "SELECT users.username as username, users.rating as rating, users.state as state, requester " +
                "FROM friends " +
                "LEFT OUTER JOIN users on friends.requester = users.id " +
                "WHERE acceptor = ?;";
        String getOutgoing = "SELECT users.username as username, users.rating as rating, users.state as state, acceptor " +
                "FROM friends " +
                "LEFT OUTER JOIN users on friends.acceptor = users.id " +
                "WHERE requester = ?;";

        try {
            ArrayList<UserInfo> users = new ArrayList<>();

            stmt = conn.prepareStatement(getIncoming);
            stmt.setString(1, user);
            rs = stmt.executeQuery();

            while(rs.next()) {
                users.add(new UserInfo(rs.getString("username"), rs.getString("requester"), rs.getInt("rating"), State.valueOf(rs.getString("state"))));
            }
            rs.close();

            stmt = conn.prepareStatement(getOutgoing);
            stmt.setString(1, user);
            rs = stmt.executeQuery();

            while(rs.next()) {
                users.add(new UserInfo(rs.getString("username"), rs.getString("acceptor"), rs.getInt("rating"), State.valueOf(rs.getString("state"))));
            }

            return users.toArray(new UserInfo[0]);
        } catch(SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }


    public UserInfo[] getBlocking(String user) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String getOutgoing = "SELECT users.username as username, users.rating as rating, users.state as state, blocked " +
                "FROM blocked " +
                "LEFT OUTER JOIN users on blocked.blocked = users.id " +
                "WHERE requester = ?;";

        try {
            ArrayList<UserInfo> users = new ArrayList<>();

            stmt = conn.prepareStatement(getOutgoing);
            stmt.setString(1, user);
            rs = stmt.executeQuery();

            while(rs.next()) {
                users.add(new UserInfo(rs.getString("username"), rs.getString("blocked"), rs.getInt("rating"), State.valueOf(rs.getString("state"))));
            }

            return users.toArray(new UserInfo[0]);
        } catch(SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }


    public Message[] listMessages(String userId, String[] users, Date start, Date end, Read read, From from, RecipientType recipient) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            ArrayList<Message> messages = new ArrayList<>();

            stmt = conn.prepareStatement(generateMessageQuery(users, start, end, read, from, recipient));
            int i = 1;
            stmt.setString(i, userId);
            i++;
            if(from == null || from == From.BOTH) {
                stmt.setString(i, userId);
                i++;
            }
            if(users != null && users.length > 0) {
                for(String user : users) {
                    stmt.setString(i, user);
                    i++;
                    if(from == null || from == From.BOTH) {
                        stmt.setString(i, user);
                        i++;
                    }
                }
            }
            if(start != null) {
                stmt.setLong(i, start.getTime());
                i++;
            }
            if(end != null) {
                stmt.setLong(i, end.getTime());
            }
            rs = stmt.executeQuery();

            while(rs.next()) {
                messages.add(new Message(rs.getString("sender"), rs.getString("recipient"), rs.getLong("time"),
                        rs.getString("message"), rs.getString("id"), (rs.getInt("opened") != 0)));
            }

            return messages.toArray(new Message[0]);
        } catch(SQLException e) {
            e.printStackTrace();
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }

    private String generateMessageQuery(String[] users, Date start, Date end, Read read, From from, RecipientType recipient) {
        StringBuilder getMessage = new StringBuilder();
        getMessage.append("SELECT id, sender, recipient, message, time, opened ");
        getMessage.append("FROM messages ");
        if(from == From.ME) {
            getMessage.append("WHERE sender = ? ");
        } else if(from == From.THEM) {
            getMessage.append("WHERE recipient = ? ");
        } else {
            getMessage.append("WHERE (sender = ? OR recipient = ?) ");
        }

        if(recipient == RecipientType.PLAYER) {
            getMessage.append(" AND recipientType = 'PLAYER'");
        }
        else if(recipient == RecipientType.GAME) {
            getMessage.append(" AND recipientType = 'GAME'");
        }

        if(users != null && users.length > 0) {
            getMessage.append(" AND (");
            boolean first = true;
            for(int i = 0; i < users.length; i++) {
                if(first) {
                    if(from == From.ME) {
                        getMessage.append("recipient = ?");
                    } else if(from == From.THEM) {
                        getMessage.append("sender = ?");
                    } else {
                        getMessage.append("(sender = ? OR recipient = ?)");
                    }
                    first = false;
                } else {
                    if(from == From.ME) {
                        getMessage.append(" OR recipient = ?");
                    } else if(from == From.THEM) {
                        getMessage.append(" OR sender = ?");
                    } else {
                        getMessage.append(" OR (sender = ? OR recipient = ?)");
                    }
                }
            }
            getMessage.append(")");
        }

        if(start != null) {
            getMessage.append(" AND time > ?");
        }

        if(end != null) {
            getMessage.append(" AND time < ?");
        }

        if(read != Read.BOTH) {
            if(read == Read.READ) {
                getMessage.append(" AND opened = 1");
            } else {
                getMessage.append(" AND opened = 0");
            }
        }

        getMessage.append(" ORDER BY time");
        getMessage.append(";");

        return getMessage.toString();
    }


    public SocialNotifications getSocialNotifications(String userId) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String countRequests = "SELECT COUNT(*) AS total " +
                "FROM friend_requests " +
                "WHERE acceptor = ?;";
        String countMessages = "SELECT COUNT(*) AS total " +
                "FROM messages " +
                "WHERE recipient = ? AND opened = 0;";

        try {
            stmt = conn.prepareStatement(countRequests);
            stmt.setString(1, userId);
            rs = stmt.executeQuery();
            int requests = rs.getInt("total");
            rs.close();

            stmt = conn.prepareStatement(countMessages);
            stmt.setString(1, userId);
            rs = stmt.executeQuery();
            int messages = rs.getInt("total");

            return new SocialNotifications(requests, messages);
        } catch(SQLException e) {
            throw new GameServerException(ErrorCode.DATABASE_ERROR);
        } finally {
            SqliteManager.closeStatement(stmt);
            SqliteManager.closeResultSet(rs);
        }
    }
}
