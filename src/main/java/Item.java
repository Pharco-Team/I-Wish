import java.io.Serializable;

public class Item implements Serializable {

    private int itemId;
    private String itemName;
    private String itemDescription;
    private double price;

    public Item(int itemId, String itemName, String itemDescription, double price) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.price = price;
    }

    public int getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getItemDescription() { return itemDescription; }
    public double getPrice() { return price; }

    @Override
    public String toString() {
        return itemName + " - " + price + " EGP";
    }
}
