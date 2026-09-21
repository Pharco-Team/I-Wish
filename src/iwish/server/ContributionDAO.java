package iwish.server;

import iwish.common.NotificationDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.*;

public class ContributionDAO {

    public static class BusinessException extends Exception {
        public BusinessException(String m) { super(m); }
    }

    public static class Result {
        public boolean completed;
        public BigDecimal collected, price;
        /** Already saved in DB; the caller pushes them to online users AFTER commit. */
        public final List<NotificationDTO> notifications = new ArrayList<>();
    }

    /** Task 7: contribute `amount` of the item's price. Tasks 8/9 fire when the price is fully covered. */
    public Result contribute(int contributorId, int wishId, BigDecimal amount)
            throws SQLException, BusinessException {
        if (amount == null || amount.signum() <= 0)
            throw new BusinessException("Amount must be greater than zero.");
        amount = amount.setScale(2, RoundingMode.HALF_UP);

        try (Connection c = DBManager.getConnection()) {
            c.setAutoCommit(false);
            try {
                Result r = doContribute(c, contributorId, wishId, amount);
                c.commit();
                return r;
            } catch (SQLException | BusinessException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private Result doContribute(Connection c, int contributorId, int wishId, BigDecimal amount)
            throws SQLException, BusinessException {

        int receiverId;
        BigDecimal collected, price;
        String status, itemName;

        // Row lock: two friends paying at the same time can't overfund the item.
        String lockSql = "SELECT w.user_id, w.amount_collected, w.status, i.name, i.price "
                       + "FROM wishlist_items w JOIN items i ON i.id = w.item_id "
                       + "WHERE w.id = ? FOR UPDATE";
        try (PreparedStatement ps = c.prepareStatement(lockSql)) {
            ps.setInt(1, wishId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new BusinessException("Wish item not found.");
                receiverId = rs.getInt(1);
                collected  = rs.getBigDecimal(2);
                status     = rs.getString(3);
                itemName   = rs.getString(4);
                price      = rs.getBigDecimal(5);
            }
        }

        if (receiverId == contributorId)
            throw new BusinessException("You cannot contribute to your own wish.");
        if (!areFriends(c, contributorId, receiverId))
            throw new BusinessException("You can only contribute to a friend's wish list.");
        if (!"OPEN".equals(status))
            throw new BusinessException("This item is already fully funded.");

        BigDecimal remaining = price.subtract(collected);
        if (amount.compareTo(remaining) > 0)
            throw new BusinessException("Amount exceeds the remaining " + remaining + ".");

        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO contributions (wish_item_id, contributor_id, amount) VALUES (?,?,?)")) {
            ps.setInt(1, wishId);
            ps.setInt(2, contributorId);
            ps.setBigDecimal(3, amount);
            ps.executeUpdate();
        }

        BigDecimal newCollected = collected.add(amount);
        boolean completed = newCollected.compareTo(price) == 0;

        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE wishlist_items SET amount_collected = ?, status = ? WHERE id = ?")) {
            ps.setBigDecimal(1, newCollected);
            ps.setString(2, completed ? "COMPLETED" : "OPEN");
            ps.setInt(3, wishId);
            ps.executeUpdate();
        }

        Result r = new Result();
        r.completed = completed;
        r.collected = newCollected;
        r.price = price;
        if (completed) createCompletionNotifications(c, r, wishId, receiverId, itemName);
        return r;
    }

    private boolean areFriends(Connection c, int a, int b) throws SQLException {
        String sql = "SELECT 1 FROM friendships WHERE status = 'ACCEPTED' AND "
                   + "((user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)) LIMIT 1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, a); ps.setInt(2, b); ps.setInt(3, b); ps.setInt(4, a);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    private String userName(Connection c, int userId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT name FROM users WHERE id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getString(1) : "a friend"; }
        }
    }

    /** Task 8 (every contributor) + Task 9 (the receiver, listing who bought it). */
    private void createCompletionNotifications(Connection c, Result r, int wishId,
                                               int receiverId, String itemName) throws SQLException {
        Map<Integer, BigDecimal> shares = new LinkedHashMap<>();
        Map<Integer, String> names = new HashMap<>();

        String sql = "SELECT c.contributor_id, u.name, SUM(c.amount) FROM contributions c "
                   + "JOIN users u ON u.id = c.contributor_id WHERE c.wish_item_id = ? "
                   + "GROUP BY c.contributor_id, u.name ORDER BY MIN(c.id)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, wishId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    shares.put(rs.getInt(1), rs.getBigDecimal(3));
                    names.put(rs.getInt(1), rs.getString(2));
                }
            }
        }

        String receiverName = userName(c, receiverId);
        StringJoiner by = new StringJoiner(", ");
        for (Map.Entry<Integer, BigDecimal> e : shares.entrySet())
            by.add(names.get(e.getKey()) + " (" + e.getValue() + ")");

        for (Map.Entry<Integer, BigDecimal> e : shares.entrySet())
            r.notifications.add(NotificationDAO.insert(c, e.getKey(), NotificationDTO.ITEM_COMPLETED,
                "The gift \"" + itemName + "\" for " + receiverName
                + " is now fully funded. Your share: " + e.getValue() + "."));

        r.notifications.add(NotificationDAO.insert(c, receiverId, NotificationDTO.ITEM_BOUGHT,
            "Your wish \"" + itemName + "\" has been bought by " + by + "."));
    }
}
