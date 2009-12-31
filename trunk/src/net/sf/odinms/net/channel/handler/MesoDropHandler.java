

/*
 * MesoDropHandler.java
 *
 * Created on 8. Dezember 2007, 14:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.AutobanManager;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public class MesoDropHandler extends AbstractMaplePacketHandler {
	
	/** Creates a new instance of MesoDropHandler */
	public MesoDropHandler() {
	}

	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		slea.readInt(); // i don't know :)
		int meso = slea.readInt();
		if (meso < 10 || meso > 50000) {
			AutobanManager.getInstance().addPoints(c, 1000, 0, "Dropping " + meso + " mesos");
			return;
		}
		if (meso <= c.getPlayer().getMeso()) {
			c.getPlayer().gainMeso(-meso, true, true);
			c.getPlayer().getMap().spawnMesoDrop(meso, meso, c.getPlayer().getPosition(), c.getPlayer(),
				c.getPlayer(), false);
		}
	}
}
