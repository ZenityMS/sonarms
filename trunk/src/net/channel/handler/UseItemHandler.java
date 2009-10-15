package net.channel.handler;

import client.IItem;
import client.MapleClient;
import client.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author Matze
 */
public class UseItemHandler extends AbstractMaplePacketHandler {
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
            if (toUse.getItemId() != itemId)
                return;
            if (itemId == 2022178 || itemId == 2050004) {
                c.getPlayer().dispelDebuffs();
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            if (MapleItemInformationProvider.getInstance().isTownScroll(itemId)) {
                if (MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyTo(c.getPlayer()))
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
            MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyTo(c.getPlayer());
            c.getPlayer().checkBerserk();
        }
    }
}