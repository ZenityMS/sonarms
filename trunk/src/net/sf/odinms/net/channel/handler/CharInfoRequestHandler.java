

package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class CharInfoRequestHandler extends AbstractMaplePacketHandler {
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		slea.readShort(); //most likely two shorts rather than one int but dunno ^___^
		slea.readShort();
		int cid = slea.readInt();
		c.getSession().write(MaplePacketCreator.charInfo((MapleCharacter) c.getPlayer().getMap().getMapObject(cid)));
	}
}
