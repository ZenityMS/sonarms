

/*
 * NPCBuyHandler.java
 *
 * Created on 26. November 2007, 00:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.scripting.npc.NPCScriptManager;

/**
 * 
 * @author Matze
 */
public class NPCShopHandler extends AbstractMaplePacketHandler {

	private int BUY = 0;
	private int SELL = 1;
	private int RECHARGE = 2;
	private int CLOSE = 3;

	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		if (c.getPlayer().getShop() == null) {
			c.disconnect();
			return;
		}
		byte bmode = slea.readByte();
		if (bmode == BUY) {
			short index = slea.readShort();
			int itemId = slea.readInt();
			short quantity = slea.readShort();
			int price = slea.readInt();
			c.getPlayer().getShop().buy(c, index, itemId, quantity, price);
		} else if (bmode == SELL) {
			byte slot = (byte) slea.readShort();
			int itemId = slea.readInt();
			MapleInventoryType type = MapleItemInformationProvider.getInstance().getInventoryType(itemId);
			short quantity = slea.readShort();
			c.getPlayer().getShop().sell(c, type, slot, quantity);
		} else if (bmode == RECHARGE) {
			byte slot = (byte) slea.readShort();
			c.getPlayer().getShop().recharge(c, slot);
		} else if (bmode == CLOSE) {
			c.getPlayer().setShop(null);
		}
	}
}
