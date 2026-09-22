import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public Notification add(int userId, String message) {
        try (Connection conn = DBConnection.getConnection()) {
            return add(conn, userId, message);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    Notification add(Connection conn, int userId, String message) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, message) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, message);
            ps.executeUpdate();
        }

        return new Notification(userId, message, new Timestamp(System.currentTimeMillis()), false);
    }

    public List<Notification> getNotifications(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT message, is_read, created_at FROM notifications "
                + "WHERE user_id = ? ORDER BY created_at DESC, notification_id DESC "
                + "FETCH FIRST 50 ROWS ONLY";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(new Notification(userId,
                            rs.getString("message"),
                            rs.getTimestamp("created_at"),
                            rs.getInt("is_read") == 1));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return notifications;
    }

    public void markAllRead(int userId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
