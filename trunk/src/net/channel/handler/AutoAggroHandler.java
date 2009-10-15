package net.channel.handler;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import server.life.MapleMonster;
import tools.data.input.SeekableLittleEndianAccessor;

public class AutoAggroHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int oid = slea.readInt();
        MapleMonster monster = c.getPlayer().getMap().getMonsterByOid(oid);
        if (monster != null && monster.getController() != null) {
            if (!monster.isControllerHasAggro())
                if (c.getPlayer().getMap().getCharacterById(monster.getController().getId()) == null)
                    monster.switchController(c.getPlayer(), true);
                else
                    monster.switchController(monster.getController(), true);
            else
                if (c.getPlayer().getMap().getCharacterById(monster.getController().getId()) == null)
                    monster.switchController(c.getPlayer(), true);
        } else if (monster != null && monster.getController() == null)
            monster.switchController(c.getPlayer(), true);
    }
}