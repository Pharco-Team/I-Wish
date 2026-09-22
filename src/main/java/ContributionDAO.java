import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ContributionDAO {

    private final NotificationDAO notifications = new NotificationDAO();

    public synchronized List<Notification> contribute(int buyerId, int wishlistItemId, double amount)
            throws SQLException {

        amount = round(amount);
        if (amount <= 0) {
            throw new IllegalArgumentException("The amount must be greater than zero.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                List<Notification> created = doContribute(conn, buyerId, wishlistItemId, amount);
                conn.commit();
                return created;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private List<Notification> doContribute(Connection conn, int buyerId, int wishlistItemId, double amount)
            throws SQLException {

        int receiverId;
        double contributed, price;
        String status, itemName;

        String sql = "SELECT wi.user_id, wi.contributed_amount, wi.status, i.item_name, i.price "
                + "FROM wishlist_items wi JOIN items i ON i.item_id = wi.item_id "
                + "WHERE wi.wishlist_item_id = ? AND wi.is_deleted = 0";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, wishlistItemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("This wish list item no longer exists.");
                }
                receiverId = rs.getInt("user_id");
                contributed = rs.getDouble("contributed_amount");
                status = rs.getString("status");
                itemName = rs.getString("item_name");
                price = rs.getDouble("price");
            }
        }

        if (receiverId == buyerId) {
            throw new IllegalArgumentException("You cannot contribute to your own wish list.");
        }
        if (!areFriends(conn, buyerId, receiverId)) {
            throw new IllegalArgumentException("You can only contribute to a friend's wish list.");
        }
        if (!"open".equals(status)) {
            throw new IllegalArgumentException("This gift is already fully funded.");
        }

        double remaining = round(price - contributed);
        if (amount > remaining) {
            throw new IllegalArgumentException("The amount is more than the remaining " + remaining + " EGP.");
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO contributions (wishlist_item_id, buyer_id, amount) VALUES (?, ?, ?)")) {
            ps.setInt(1, wishlistItemId);
            ps.setInt(2, buyerId);
            ps.setDouble(3, amount);
            ps.executeUpdate();
        }

        double total = round(contributed + amount);
        boolean completed = total >= price;

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE wishlist_items SET contributed_amount = ?, status = ? WHERE wishlist_item_id = ?")) {
            ps.setDouble(1, total);
            ps.setString(2, completed ? "completed" : "open");
            ps.setInt(3, wishlistItemId);
            ps.executeUpdate();
        }

        return completed
                ? completionNotifications(conn, wishlistItemId, receiverId, itemName)
                : new ArrayList<Notification>();
    }

    private List<Notification> completionNotifications(Connection conn, int wishlistItemId,
                                                       int receiverId, String itemName) throws SQLException {
        List<Notification> created = new ArrayList<>();
        List<Integer> buyerIds = new ArrayList<>();
        List<String> buyerNames = new ArrayList<>();
        List<Double> shares = new ArrayList<>();

        String sql = "SELECT u.user_id, u.username, SUM(c.amount) AS total FROM contributions c "
                + "JOIN users u ON u.user_id = c.buyer_id "
                + "WHERE c.wishlist_item_id = ? GROUP BY u.user_id, u.username ORDER BY u.username";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, wishlistItemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    buyerIds.add(rs.getInt("user_id"));
                    buyerNames.add(rs.getString("username"));
                    shares.add(rs.getDouble("total"));
                }
            }
        }

        String receiverName = usernameOf(conn, receiverId);
        StringBuilder buyers = new StringBuilder();

        for (int i = 0; i < buyerIds.size(); i++) {
            if (i > 0) {
                buyers.append(", ");
            }
            buyers.append(buyerNames.get(i)).append(" (").append(shares.get(i)).append(" EGP)");
        }

        for (int i = 0; i < buyerIds.size(); i++) {
            created.add(notifications.add(conn, buyerIds.get(i),
                    "The gift '" + itemName + "' for " + receiverName
                    + " is now fully funded. Your share: " + shares.get(i) + " EGP."));
        }

        created.add(notifications.add(conn, receiverId,
                "Your wish '" + itemName + "' has been bought by " + buyers + "."));

        return created;
    }

    private boolean areFriends(Connection conn, int userId, int friendId) throws SQLException {
        String sql = "SELECT 1 FROM friends WHERE status = 'accepted' "
                + "AND ((user_id_1 = ? AND user_id_2 = ?) OR (user_id_1 = ? AND user_id_2 = ?))";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, friendId);
            ps.setInt(3, friendId);
            ps.setInt(4, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private String usernameOf(Connection conn, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT username FROM users WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("username") : "a friend";
            }
        }
    }

    private static double round(double value) {
        return Math.round(value * 100) / 100.0;
    }
}
