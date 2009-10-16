package net.channel.handler;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import server.maps.MapleMap;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Jay Estrella
 */
public class MobDamageMobHandler extends AbstractMaplePacketHandler {
    //(24) A3 00 67 00 00 00 03 00 00 00 68 00 00 00 FF 0D 00 00 00 00 29 02 A1 00
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int from = slea.readInt(); // oid to
        slea.readInt(); // player id (Bui)
        int to = slea.readInt(); // oid from
        slea.readByte(); // Same as player damage, -1 = bump, integer = skill ID (Bui)
        int dmg = slea.readInt(); // dmg
        slea.readByte(); // // Facing direction (Bui)
        slea.readShort(); // pos-x?
        slea.readShort(); // pos-y?
        MapleMap map = c.getPlayer().getMap();
        if (map.getMonsterByOid(from) != null && map.getMonsterByOid(to) != null)
            map.damageMonster(c.getPlayer(), map.getMonsterByOid(to), dmg);
    }
}