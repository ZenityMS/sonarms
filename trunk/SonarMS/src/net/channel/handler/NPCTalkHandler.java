package net.channel.handler;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import scripting.npc.NPCScriptManager;
import server.life.MapleNPC;
import server.maps.MapleMapObject;
import server.maps.PlayerNPCs;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class NPCTalkHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int oid = slea.readInt();
        slea.readInt();
        MapleMapObject obj = c.getPlayer().getMap().getMapObject(oid);
        if (obj instanceof MapleNPC) {
            MapleNPC npc = (MapleNPC) obj;
//            if (npc.getId() == 9010009)
//                c.getSession().write(MaplePacketCreator.sendDuey((byte) 8, DueyHandler.loadItems(c.getPlayer())));
//            else
            if (npc.hasShop()) {
                if (c.getPlayer().getShop() != null) {
                    c.getPlayer().setShop(null);
                    c.getSession().write(MaplePacketCreator.confirmShopTransaction((byte) 20));
                }
                npc.sendShop(c);
            } else {
                if (c.getCM() != null || c.getQM() != null) {
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                NPCScriptManager.getInstance().start(c, npc.getId());
            }
        } else if (obj instanceof PlayerNPCs) {
            PlayerNPCs npc = (PlayerNPCs) obj;
            NPCScriptManager.getInstance().start(c, npc.getId());
        }
    }
}