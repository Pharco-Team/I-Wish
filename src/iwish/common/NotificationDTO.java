package iwish.common;

import java.io.Serializable;

/** Sent both as a stored record (GET_NOTIFICATIONS) and as a live push. */
public class NotificationDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String ITEM_COMPLETED = "ITEM_COMPLETED"; // task 8 (buyer)
    public static final String ITEM_BOUGHT    = "ITEM_BOUGHT";    // task 9 (receiver)

    public final int id;
    public final int userId;
    public final String type;
    public final String message;
    public final long createdAt;

    public NotificationDTO(int id, int userId, String type, String message, long createdAt) {
        this.id = id; this.userId = userId; this.type = type;
        this.message = message; this.createdAt = createdAt;
    }
}
