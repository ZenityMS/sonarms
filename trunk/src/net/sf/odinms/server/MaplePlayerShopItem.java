

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.odinms.server;

import net.sf.odinms.client.IItem;

/**
 *
 * @author Matze
 */
public class MaplePlayerShopItem {
	
	private IItem item;
	private MaplePlayerShop shop;
	private short bundles;
	private int price;
	
	public MaplePlayerShopItem(MaplePlayerShop shop, IItem item, short bundles, int price) {
		this.shop = shop;
		this.item = item;
		this.bundles = bundles;
		this.price = price;
	}

	public IItem getItem() {
		return item;
	}

	public MaplePlayerShop getShop() {
		return shop;
	}

	public short getBundles() {
		return bundles;
	}

	public int getPrice() {
		return price;
	}

	public void setBundles(short bundles) {
		this.bundles = bundles;
	}

}
