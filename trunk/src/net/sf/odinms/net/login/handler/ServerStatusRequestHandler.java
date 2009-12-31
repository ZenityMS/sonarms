
package net.sf.odinms.net.login.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.net.login.LoginServer;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class ServerStatusRequestHandler extends AbstractMaplePacketHandler {
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int load = 0;
		for (ChannelServer cservs : ChannelServer.getAllInstances()) {
			load += LoginServer.getInstance().getLoad().get(cservs.getChannel());
		}
		if (LoginServer.getInstance().getUserLimit() <= load) {
			c.getSession().write(MaplePacketCreator.getServerStatus(2));
		} else if (LoginServer.getInstance().getUserLimit() * 0.9 <= load) {
			c.getSession().write(MaplePacketCreator.getServerStatus(1));
		} else {
			c.getSession().write(MaplePacketCreator.getServerStatus(0));
		}
	}
}
