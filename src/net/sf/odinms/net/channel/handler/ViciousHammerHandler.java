
package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.IItem;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Danny
 */
public class ViciousHammerHandler extends AbstractMaplePacketHandler {
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		if (c.getPlayer().getHammerSlot() == null) {
			c.disconnect();
			return;
		}
		byte slot = c.getPlayer().getHammerSlot().byteValue();
		IItem item = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(slot);
		c.getSession().write(MaplePacketCreator.sendHammerEnd());
		c.getSession().write(MaplePacketCreator.updateHammerItem(item));
		c.getPlayer().setHammerSlot(null);
	}
}
