package server;

import java.util.Calendar;
import client.IItem;

/**
 *
 * @author Traitor
 */
public class MTSItemInfo {
    private int price;
    private IItem item;
    private String seller;
    private int id;
    private int cid;
    private int year,  month,  day = 1;

    public MTSItemInfo(IItem item, int price, int id, int cid, String seller, String date) {
        this.item = item;
        this.price = price;
        this.seller = seller;
        this.id = id;
        this.cid = cid;
        this.year = Integer.parseInt(date.substring(0, 4));
        this.month = Integer.parseInt(date.substring(5, 7));
        this.day = Integer.parseInt(date.substring(8, 10));
    }

    public IItem getItem() {
        return item;
    }

    public int getPrice() {
        return price;
    }

    public int getTaxes() {
        return 100 + (int) (price / 10);
    }

    public int getID() {
        return id;
    }

    public long getEndingDate() {
        Calendar now = Calendar.getInstance();
        now.set(year, month - 1, day);
        return now.getTimeInMillis();
    }

    public String getSeller() {
        return seller;
    }
}