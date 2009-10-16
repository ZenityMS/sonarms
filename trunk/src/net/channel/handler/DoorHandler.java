package net.channel.handler;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import server.maps.MapleDoor;
import server.maps.MapleMapObject;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public class DoorHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int oid = slea.readInt();
        boolean mode = (slea.readByte() == 0); // specifies if backwarp or not, 1 town to target, 0 target to town
        for (MapleMapObject obj : c.getPlayer().getMap().getMapObjects())
            if (obj instanceof MapleDoor) {
                MapleDoor door = (MapleDoor) obj;
                if (door.getOwner().getId() == oid) {
                    door.warp(c.getPlayer(), mode);
                    return;
                }
            }
    }
}
