package server;

/**
 *
 * @author Lerk
 */
public class CashItemInfo {
    private int itemId,  count,  price;

    public CashItemInfo(int itemId, int count, int price) {
        this.itemId = itemId;
        this.count = count;
        this.price = price;
    }

    public int getId() {
        return itemId;
    }

    public int getCount() {
        return count;
    }

    public int getPrice() {
        return price;
    }
}