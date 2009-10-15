package net.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class CharInfoRequestHandler extends AbstractMaplePacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readShort();
        slea.readShort();
        int cid = slea.readInt();
        MapleCharacter player = (MapleCharacter) c.getPlayer().getMap().getMapObject(cid);
        if (player.gmLevel() < 1 || c.getPlayer().gmLevel() > 0)
            c.getSession().write(MaplePacketCreator.charInfo((MapleCharacter) c.getPlayer().getMap().getMapObject(cid), cid == c.getPlayer().getId()));
        else if (c.getPlayer().gmLevel() > 0 && player.gmLevel() > 0)
            c.getSession().write(MaplePacketCreator.charInfo((MapleCharacter) c.getPlayer().getMap().getMapObject(cid), cid == c.getPlayer().getId()));
        else {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
    }
}
