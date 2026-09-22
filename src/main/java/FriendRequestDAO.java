import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FriendRequestDAO {

    public String sendRequest(int senderId, int receiverId) {
        String outgoing = statusOf(senderId, receiverId);
        String incoming = statusOf(receiverId, senderId);

        if ("accepted".equals(outgoing) || "accepted".equals(incoming)) {
            return "You are already friends!";
        }
        if ("pending".equals(outgoing)) {
            return "Friend request already sent!";
        }
        if ("pending".equals(incoming)) {
            return "This user already sent you a request - check your friend requests.";
        }

        String sql = "INSERT INTO friends (user_id_1, user_id_2, status) VALUES (?, ?, 'pending')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);

            return ps.executeUpdate() > 0 ? null : "Could not send the friend request.";

        } catch (SQLException e) {
            e.printStackTrace();
            return "Could not send the friend request.";
        }
    }

    public List<Friend> getPendingRequests(int userId) {
        List<Friend> requests = new ArrayList<>();
        String sql = "SELECT u.user_id, u.username FROM friends f "
                + "JOIN users u ON u.user_id = f.user_id_1 "
                + "WHERE f.user_id_2 = ? AND f.status = 'pending'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    requests.add(new Friend(rs.getInt("user_id"), rs.getString("username")));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
    }

    public boolean acceptRequest(int userId, int senderId) {
        String sql = "UPDATE friends SET status = 'accepted' "
                + "WHERE user_id_1 = ? AND user_id_2 = ? AND status = 'pending'";
        return update(sql, senderId, userId);
    }

    public boolean declineRequest(int userId, int senderId) {
        String sql = "DELETE FROM friends "
                + "WHERE user_id_1 = ? AND user_id_2 = ? AND status = 'pending'";
        return update(sql, senderId, userId);
    }

    public boolean removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM friends WHERE status = 'accepted' "
                + "AND ((user_id_1 = ? AND user_id_2 = ?) OR (user_id_1 = ? AND user_id_2 = ?))";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, friendId);
            ps.setInt(3, friendId);
            ps.setInt(4, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String statusOf(int senderId, int receiverId) {
        String sql = "SELECT status FROM friends WHERE user_id_1 = ? AND user_id_2 = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("status") : null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean update(String sql, int firstId, int secondId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, firstId);
            ps.setInt(2, secondId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
