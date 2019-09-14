package io.joshatron.bgt.server.database;

import io.joshatron.bgt.server.exceptions.ErrorCode;
import io.joshatron.bgt.server.exceptions.GameServerException;
import io.joshatron.bgt.server.request.From;
import io.joshatron.bgt.server.request.Read;
import io.joshatron.bgt.server.request.RecipientType;
import io.joshatron.bgt.server.response.MessageInfo;
import io.joshatron.bgt.server.utils.IdUtils;
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


    public MessageInfo[] listMessages(String userId, String[] users, Date start, Date end, Read read, From from, RecipientType recipient) throws GameServerException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            ArrayList<MessageInfo> messages = new ArrayList<>();

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
                messages.add(new MessageInfo(rs.getString("sender"), rs.getString("recipient"), rs.getLong("time"),
                        rs.getString("message"), rs.getString("id"), (rs.getInt("opened") != 0)));
            }

            return messages.toArray(new MessageInfo[0]);
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
}
