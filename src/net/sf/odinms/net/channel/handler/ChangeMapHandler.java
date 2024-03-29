

package net.sf.odinms.net.channel.handler;

import java.net.InetAddress;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.net.MaplePacket;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.server.MaplePortal;
import net.sf.odinms.server.maps.MapleMap;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeMapHandler extends AbstractMaplePacketHandler {
	private static Logger log = LoggerFactory.getLogger(ChangeMapHandler.class);

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		if (slea.available() == 0) {
			int channel = c.getChannel();
			String ip = ChannelServer.getInstance(c.getChannel()).getIP(channel);
			String[] socket = ip.split(":");
			c.getPlayer().saveToDB(true);
			c.getPlayer().setInCS(false);
			ChannelServer.getInstance(c.getChannel()).removePlayer(c.getPlayer());
			c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
			try {
				MaplePacket packet = MaplePacketCreator.getChannelChange(
					InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]));
				c.getSession().write(packet);
				c.getSession().close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			slea.readByte(); //?
			int targetid = slea.readInt(); //FF FF FF FF

			String startwp = slea.readMapleAsciiString();
			MaplePortal portal = c.getPlayer().getMap().getPortal(startwp);

			MapleCharacter player = c.getPlayer();
			if (targetid != -1 && !c.getPlayer().isAlive()) {
				boolean executeStandardPath = true;
				if (player.getEventInstance() != null) {
					executeStandardPath = player.getEventInstance().revivePlayer(player);
				}
				if (executeStandardPath) {
					player.setHp(50);
					if (c.getPlayer().getMap().getForcedReturnId() != 999999999) {
						MapleMap to = c.getPlayer().getMap().getForcedReturnMap();
						MaplePortal pto = to.getPortal(0);
						player.setStance(0);
						player.changeMap(to, pto);
					} else {
						MapleMap to = c.getPlayer().getMap().getReturnMap();
						MaplePortal pto = to.getPortal(0);
						player.setStance(0);
						player.changeMap(to, pto);
					}
				}
			} else if (targetid != -1 && c.getPlayer().isGM()) {
				MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
				MaplePortal pto = to.getPortal(0);
				player.changeMap(to, pto);
			} else if (targetid != -1 && !c.getPlayer().isGM()) {
				log.warn("Player {} attempted Mapjumping without being a gm", c.getPlayer().getName());
			} else {
				if (portal != null) {
					portal.enterPortal(c);
				} else {
					c.getSession().write(MaplePacketCreator.enableActions());
					log.warn("Portal {} not found on map {}", startwp, c.getPlayer().getMap().getId());
				}
			}
		}
	}
}
