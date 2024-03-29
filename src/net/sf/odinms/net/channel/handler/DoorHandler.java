

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.maps.MapleDoor;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public class DoorHandler extends AbstractMaplePacketHandler {

	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int oid = slea.readInt();
		@SuppressWarnings("unused")
		byte mode = slea.readByte(); // specifies if backwarp or not, but currently we do not care
		for (MapleMapObject obj : c.getPlayer().getMap().getMapObjects()) {
			if (obj instanceof MapleDoor) {
				MapleDoor door = (MapleDoor) obj;
				if (door.getOwner().getId() == oid) {
					door.warp(c.getPlayer());
					return;
				}
			}
		}
	}

}
