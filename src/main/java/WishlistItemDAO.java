import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsible for:
 *  - Create / Update / Delete my Wish List (spec item 4)
 *  - Listing the items catalog (so the user can pick from it)
 *
 * NOTE: per the current schema, wishlist_items has NO custom_* columns.
 * Every wishlist item MUST point to a catalog item via item_id (NOT NULL,
 * FK to items). Editing a wishlist item's name/price means editing the
 * catalog item itself (see updateCatalogItem) - it is not a per-user custom field.
 *
 * Uses DBConnection.getConnection() - swap this with your team's shared
 * connection utility if it's named differently.
 */
public class WishlistItemDAO {

    // ---------- LIST CATALOG (for the "pick from catalog" option) ----------
    public List<Item> getCatalog() {
        List<Item> catalog = new ArrayList<>();
        String sql = "SELECT item_id, item_name, item_description, price FROM items";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                catalog.add(new Item(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getString("item_description"),
                        rs.getDouble("price")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return catalog;
    }

    // ---------- CREATE - from catalog ----------
    public boolean addItemFromCatalog(int userId, int itemId) {
        String sql = "INSERT INTO wishlist_items (user_id, item_id) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, itemId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------- UPDATE - edit the catalog item's own data (name/description/price) ----------
    // This changes the item everywhere it's used (in the catalog dropdown AND in
    // everyone's wishlist that points to this item_id), since wishlist_items only
    // stores a reference (item_id) and reads the actual name/price via the JOIN.
    // Only the owner may edit, and the new price may not drop below what friends already paid.
    public boolean updateCatalogItem(int userId, int itemId, String itemName, String itemDescription, double price) {
        if (!canEdit(userId, itemId, price)) {
            return false;
        }

        String sql = "UPDATE items SET item_name = ?, item_description = ?, price = ? WHERE item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, itemName);
            ps.setString(2, itemDescription);
            ps.setDouble(3, price);
            ps.setInt(4, itemId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean canEdit(int userId, int itemId, double price) {
        String sql = "SELECT user_id, contributed_amount FROM wishlist_items "
                + "WHERE item_id = ? AND is_deleted = 0";
        boolean owner = false;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (rs.getDouble("contributed_amount") > price) {
                        return false;
                    }
                    if (rs.getInt("user_id") == userId) {
                        owner = true;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return owner;
    }

    // ---------- DELETE (soft delete) ----------
    public boolean deleteItem(int wishlistItemId, int userId) {
        String sql = "UPDATE wishlist_items SET is_deleted = 1 "
                + "WHERE wishlist_item_id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, wishlistItemId);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------- VIEW MY OWN WISHLIST ----------
    // Every wishlist_items row has a NOT NULL item_id, so a plain JOIN with items is enough -
    // no custom_* columns exist in this schema, so no COALESCE is needed.
    public List<WishlistItem> getMyWishlist(int userId) {
        List<WishlistItem> items = new ArrayList<>();
        String sql = "SELECT wi.wishlist_item_id, wi.item_id, "
                + "i.item_name, i.item_description, i.price, "
                + "wi.contributed_amount, wi.status "
                + "FROM wishlist_items wi "
                + "JOIN items i ON i.item_id = wi.item_id "
                + "WHERE wi.user_id = ? AND wi.is_deleted = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new WishlistItem(
                            rs.getInt("wishlist_item_id"),
                            userId,
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