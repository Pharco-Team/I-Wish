import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AdminDAO {

    public boolean addCatalogItem(String itemName, String itemDescription, double price) {
        String sql = "INSERT INTO items (item_name, item_description, price) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, itemName);
            ps.setString(2, itemDescription);
            ps.setDouble(3, price);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
