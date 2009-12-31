

package net.sf.odinms.net.login.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.net.login.LoginServer;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class ServerlistRequestHandler extends AbstractMaplePacketHandler {
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		c.getSession().write(MaplePacketCreator.getServerList(0, "Zenith", LoginServer.getInstance().getLoad()));
		//c.getSession().write(MaplePacketCreator.getServerList(1, "Zenith", LoginServer.getInstance().getChannels(), 1200));
		//c.getSession().write(MaplePacketCreator.getServerList(2, "Zenith", LoginServer.getInstance().getChannels(), 1200));
		//c.getSession().write(MaplePacketCreator.getServerList(3, "Zenith", LoginServer.getInstance().getChannels(), 1200));
		c.getSession().write(MaplePacketCreator.getEndOfServerList());
	}
}
