package server;

import client.IItem;

/**
 *
 * @author Matze
 */
public class MaplePlayerShopItem {
    private IItem item;
    private short bundles;
    private int price;
    private boolean doesExist;

    public MaplePlayerShopItem(IItem item, short bundles, int price) {
        this.item = item;
        this.bundles = bundles;
        this.price = price;
        this.doesExist = true;
    }

    public void setDoesExist(boolean tf) {
        this.doesExist = tf;
    }

    public boolean isExist() {
        return doesExist;
    }

    public IItem getItem() {
        return item;
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
