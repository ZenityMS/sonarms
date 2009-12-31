

package net.sf.odinms.net.channel.handler;


import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.maps.MapleMap;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class MonsterBombHandler extends AbstractMaplePacketHandler {
	
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		// 9F 00 92 00 00 00
		int oid = slea.readInt();
		
		MapleMap map = c.getPlayer().getMap();
		
		MapleMonster monster = map.getMonsterByOid(oid);
		
		if (monster != null) {
			map.killMonster(monster, c.getPlayer(), false);
		}
	}

}
