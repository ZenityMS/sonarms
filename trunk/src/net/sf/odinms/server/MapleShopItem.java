

/*
 * MapleShopItem.java
 *
 * Created on 25. November 2007, 20:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.odinms.server;

/**
 *
 * @author Matze
 */
public class MapleShopItem {

		private int maxSlot;
        private int itemId;
        private int price;
        
        /** Creates a new instance of MapleShopItem */
        public MapleShopItem(int itemId, int price, int maxSlot) {
                this.itemId = itemId;
                this.price = price;
				this.maxSlot = maxSlot;
        }

        public int getMaxSlot() {
                return maxSlot;
        }

        public int getItemId() {
                return itemId;
        }

        public int getPrice() {
                return price;
        }
        
}
