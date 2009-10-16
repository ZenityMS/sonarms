package net.channel.handler;

import client.IItem;
import client.MapleClient;
import client.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.life.MapleLifeFactory;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author AngelSL
 */
public class UseSummonBag extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!c.getPlayer().isAlive()) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        slea.readInt();
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt();
        IItem toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse != null && toUse.getQuantity() > 0) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
            int[][] toSpawn = MapleItemInformationProvider.getInstance().getSummonMobs(itemId);
            for (int z = 0; z < toSpawn.length; z++) {
                int[] toSpawnChild = toSpawn[z];
                if ((int) Math.ceil(Math.random() * 100) <= toSpawnChild[1])
                    c.getPlayer().getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(toSpawnChild[0]), c.getPlayer().getPosition());
            }
        } else
            c.disconnect();
        c.getSession().write(MaplePacketCreator.enableActions());
    }
}