

package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.scripting.npc.NPCScriptManager;
import net.sf.odinms.server.life.MapleNPC;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class NPCTalkHandler extends AbstractMaplePacketHandler {
	@Override
	public synchronized void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int oid = slea.readInt();
		slea.readInt(); // dont know
		if (c.getPlayer().getNpcId() == -1) {
			MapleNPC npc = (MapleNPC) c.getPlayer().getMap().getMapObject(oid);
			if (npc != null) {
				c.getPlayer().setNpcId(npc.getId());
				if (npc.hasShop()) {
					npc.sendShop(c);
				} else {
					NPCScriptManager.getInstance().start(c, npc.getId());
				}
			}
		}
	}
}
