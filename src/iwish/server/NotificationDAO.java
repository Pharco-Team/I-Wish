package iwish.server;

import iwish.common.NotificationDTO;
import java.sql.*;
import java.util.ArrayList;

public class NotificationDAO {

    /** Inserts inside the caller's transaction. */
    public static NotificationDTO insert(Connection c, int userId, String type, String message) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, type, message) VALUES (?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setString(3, message);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                k.next();
                return new NotificationDTO(k.getInt(1), userId, type, message, System.currentTimeMillis());
            }
        }
    }

    public ArrayList<NotificationDTO> getUnread(int userId) throws SQLException {
        String sql = "SELECT id, type, message, created_at FROM notifications "
                   + "WHERE user_id = ? AND is_read = 0 ORDER BY created_at DESC, id DESC";
        ArrayList<NotificationDTO> list = new ArrayList<>();
        try (Connection c = DBManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new NotificationDTO(rs.getInt(1), userId, rs.getString(2),
                                                 rs.getString(3), rs.getTimestamp(4).getTime()));
            }
        }
        return list;
    }

    public void markAllRead(int userId) throws SQLException {
        try (Connection c = DBManager.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE notifications SET is_read = 1 WHERE user_id = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}
