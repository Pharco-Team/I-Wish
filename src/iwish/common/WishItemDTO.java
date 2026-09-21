package iwish.common;

import java.io.Serializable;
import java.math.BigDecimal;

/** One row of a friend's wish list (returned by your "view friend's wish list" request). */
public class WishItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    public final int wishId;
    public final int receiverId;
    public final String itemName;
    public final BigDecimal price;
    public final BigDecimal collected;

    public WishItemDTO(int wishId, int receiverId, String itemName, BigDecimal price, BigDecimal collected) {
        this.wishId = wishId; this.receiverId = receiverId; this.itemName = itemName;
        this.price = price; this.collected = collected;
    }
    public BigDecimal remaining() { return price.subtract(collected); }
}
