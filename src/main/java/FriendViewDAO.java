import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsible ONLY for:
 *  - View my Friends list        (spec item 5)
 *  - View a specific Friend's Wish List (spec item 6)
 *
 * Assumes a "friends" table with columns:
 *   user_id_1, user_id_2, status  (status = 'accepted' once confirmed)
 * If your teammate built it differently (e.g. friend_requests + friendships
 * as two separate tables), send me the real structure and I'll adjust the SQL.
 */
public class FriendViewDAO {

    // ---------- VIEW MY FRIENDS LIST ----------
    public List<Friend> getFriendsList(int userId) {
        List<Friend> friends = new ArrayList<>();
        String sql = "SELECT u.user_id, u.username "
                + "FROM friends f "
                + "JOIN users u ON u.user_id = CASE "
                + "    WHEN f.user_id_1 = ? THEN f.user_id_2 "
                + "    ELSE f.user_id_1 END "
                + "WHERE (f.user_id_1 = ? OR f.user_id_2 = ?) "
                + "AND f.status = 'accepted'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Fills the 1st "?" - inside the CASE WHEN, decides which side of the
            // friendship row is "the other person" relative to me
            ps.setInt(1, userId);

            // Fills the 2nd "?" - checks if I'm a participant in the row as user_id_1
            ps.setInt(2, userId);

            // Fills the 3rd "?" - same check, but in case I'm user_id_2 instead
            ps.setInt(3, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    friends.add(new Friend(
                            rs.getInt("user_id"),
                            rs.getString("username")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return friends;
    }

    // ---------- VIEW A FRIEND'S WISH LIST ----------
    // Only returns items if friendUserId is actually an accepted friend of me
    // Now JOINs with items to get the actual name/description/price
    public List<WishlistItem> getFriendWishlist(int myUserId, int friendUserId) {
        List<WishlistItem> items = new ArrayList<>();
        String sql = "SELECT wi.wishlist_item_id, wi.item_id, i.item_name, i.item_description, "
                + "i.price, wi.contributed_amount, wi.status "
                + "FROM wishlist_items wi "
                + "JOIN items i ON i.item_id = wi.item_id "
                + "WHERE wi.user_id = ? AND wi.is_deleted = 0 "
                + "AND EXISTS ( "
                + "    SELECT 1 FROM friends f "
                + "    WHERE f.status = 'accepted' "
                + "    AND ((f.user_id_1 = ? AND f.user_id_2 = wi.user_id) "
                + "      OR (f.user_id_2 = ? AND f.user_id_1 = wi.user_id)) "
                + ")";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Fills the 1st "?" - WHOSE wishlist we want to fetch (the friend's user_id)
            ps.setInt(1, friendUserId);

            // Fills the 2nd "?" - inside the EXISTS check: am I (myUserId) linked to
            // that friend as user_id_1 in the friends table?
            ps.setInt(2, myUserId);

            // Fills the 3rd "?" - same check, but in case I'm user_id_2 instead.
            // Together, 2nd and 3rd make sure we only return the list if we're
            // actually accepted friends - a security/privacy check.
            ps.setInt(3, myUserId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new WishlistItem(
                            rs.getInt("wishlist_item_id"),
                            friendUserId,
                            rs.getInt("item_id"),
                            rs.getString("item_name"),
                            rs.getString("item_description"),
                            rs.getDouble("price"),
                            rs.getDouble("contributed_amount"),
                            rs.getString("status")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }
}