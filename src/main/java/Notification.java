import java.io.Serializable;
import java.sql.Timestamp;

public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    private int userId;
    private String message;
    private Timestamp createdAt;
    private boolean read;

    public Notification(int userId, String message, Timestamp createdAt, boolean read) {
        this.userId = userId;
        this.message = message;
        this.createdAt = createdAt;
        this.read = read;
    }

    public int getUserId() { return userId; }
    public String getMessage() { return message; }
    public Timestamp getCreatedAt() { return createdAt; }
    public boolean isRead() { return read; }

    @Override
    public String toString() {
        return message;
    }
}
