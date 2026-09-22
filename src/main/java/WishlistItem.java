import java.io.Serializable;

public class WishlistItem implements Serializable {

    private int wishlistItemId;
    private int userId;
    private int itemId;               // FK to catalog
    private String itemName;           // comes from items table via JOIN
    private String itemDescription;    // comes from items table via JOIN
    private double price;              // comes from items table via JOIN
    private double contributedAmount;  // read-only here - owned by contribution teammate
    private String status;             // read-only here - owned by contribution teammate

    public WishlistItem(int wishlistItemId, int userId, int itemId, String itemName,
                        String itemDescription, double price,
                        double contributedAmount, String status) {
        this.wishlistItemId = wishlistItemId;
        this.userId = userId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.price = price;
        this.contributedAmount = contributedAmount;
        this.status = status;
    }

    public int getWishlistItemId() { return wishlistItemId; }
    public int getUserId() { return userId; }
    public int getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getItemDescription() { return itemDescription; }
    public double getPrice() { return price; }
    public double getContributedAmount() { return contributedAmount; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return itemName + " - " + price + " EGP (" + status + ")";
    }
}