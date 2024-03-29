

package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventory;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.anticheat.CheatingOffense;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FaceExpressionHandler extends AbstractMaplePacketHandler {
	private static Logger log = LoggerFactory.getLogger(FaceExpressionHandler.class);

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int emote = slea.readInt();
		if (emote > 7) {
			int emoteid = 5159992 + emote;
			MapleInventoryType type = MapleItemInformationProvider.getInstance().getInventoryType(emoteid);
			MapleInventory iv = c.getPlayer().getInventory(type);
			if (iv.findById(emoteid) == null) {
				log.info("[h4x] Player {} is using a face expression he does not have: {}", c.getPlayer().getName(), Integer.valueOf(emoteid));
				c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.USING_UNAVAILABLE_ITEM, Integer.toString(emoteid));
				return;
			}
		}
		c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.facialExpression(c.getPlayer(), emote), false);
	}
}
