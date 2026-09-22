import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DBConnection {

    private static final String URL = "jdbc:derby:iwishdb;create=true";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initSchema() {
        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {

            try (ResultSet rs = conn.getMetaData().getTables(null, null, "USERS", null)) {
                if (rs.next()) {
                    return;
                }
            }

            for (String sql : readSchema().split(";")) {
                if (!sql.trim().isEmpty()) {
                    st.execute(sql);
                }
            }
            System.out.println("Database created.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        try {
            DriverManager.getConnection("jdbc:derby:iwishdb;shutdown=true");
        } catch (SQLException e) {
            // Derby always throws here on a successful shutdown
        }
    }

    private static String readSchema() throws IOException {
        InputStream in = DBConnection.class.getResourceAsStream("/schema.sql");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
