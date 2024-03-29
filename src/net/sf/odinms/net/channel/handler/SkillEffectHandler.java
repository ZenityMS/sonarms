

package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class SkillEffectHandler extends AbstractMaplePacketHandler {

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int skillId = slea.readInt();
		slea.readByte(); // unknown at this point
		byte flags = slea.readByte();
		c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.skillEffect(c.getPlayer(), skillId, flags), false);
	}
}
