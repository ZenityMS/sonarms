

/*
 * ItemMoveHandler.java
 *
 * Created on 27. November 2007, 02:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public class ItemMoveHandler extends AbstractMaplePacketHandler {
	// private static Logger log = LoggerFactory.getLogger(ItemMoveHandler.class);
	
	/** Creates a new instance of ItemMoveHandler */
	public ItemMoveHandler() {
	}

	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		slea.readInt(); //?
		MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
		byte src = (byte)slea.readShort();
		byte dst = (byte)slea.readShort();
		short quantity = slea.readShort();
		if (src < 0 && dst > 0) {
			MapleInventoryManipulator.unequip(c, src, dst);
		} else if (dst < 0) {
			MapleInventoryManipulator.equip(c, src, dst);
		} else if (dst == 0) {
			MapleInventoryManipulator.drop(c, type, src, quantity);
		} else {
			MapleInventoryManipulator.move(c, type, src, dst);
		}
	}
	
}
