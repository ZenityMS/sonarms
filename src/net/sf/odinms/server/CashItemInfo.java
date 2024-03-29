package net.sf.odinms.server;

/**
 *
 * @author Lerk
 */
public class CashItemInfo {
	private int itemId;
	private int count;
	private int price;
	
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