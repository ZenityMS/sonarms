package net.channel.handler;

import client.IItem;
import client.MapleClient;
import client.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStorage;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public class StorageHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        byte mode = slea.readByte();
        final MapleStorage storage = c.getPlayer().getStorage();
        if (mode == 4) { // take out
            byte type = slea.readByte();
            byte slot = slea.readByte();
            slot = storage.getSlot(MapleInventoryType.getByType(type), slot);
            IItem item = storage.takeOut(slot);
            if (item != null) {
                if (MapleInventoryManipulator.checkSpace(c, item.getItemId(), item.getQuantity(), item.getOwner()))
                    MapleInventoryManipulator.addFromDrop(c, item, false);
                else {
                    storage.store(item);
                    c.getPlayer().dropMessage(1, "Your inventory is full");
                }
                storage.sendTakenOut(c, ii.getInventoryType(item.getItemId()));
            } else {
                c.disconnect();
                return;
            }
        } else if (mode == 5) { // store
            byte slot = (byte) slea.readShort();
            int itemId = slea.readInt();
            short quantity = slea.readShort();
            if (quantity < 1)
                return;
            if (storage.isFull()) {
                c.getSession().write(MaplePacketCreator.getStorageFull());
                return;
            }
            if (c.getPlayer().getMeso() < 100)
                c.getPlayer().dropMessage(1, "You don't have enough mesos to store the item");
            else {
                MapleInventoryType type = ii.getInventoryType(itemId);
                IItem item = c.getPlayer().getInventory(type).getItem(slot).copy();
                if (item.getItemId() == itemId && (item.getQuantity() >= quantity || ii.isThrowingStar(itemId) || ii.isBullet(itemId))) {
                    if (ii.isThrowingStar(itemId) || ii.isBullet(itemId))
                        quantity = item.getQuantity();
                    c.getPlayer().gainMeso(-100, false, true, false);
                    MapleInventoryManipulator.removeFromSlot(c, type, slot, quantity, false);
                    item.setQuantity(quantity);
                    storage.store(item);
                } else
                    c.getPlayer().ban("Trying to store non-matching itemid (" + itemId + "/" + item.getItemId() + ") or quantity not in posession (" + quantity + "/" + item.getQuantity() + ")", false);
            }
            storage.sendStored(c, ii.getInventoryType(itemId));
        } else if (mode == 7) { // meso
            int meso = slea.readInt();
            int storageMesos = storage.getMeso();
            int playerMesos = c.getPlayer().getMeso();
            if ((meso > 0 && storageMesos >= meso) || (meso < 0 && playerMesos >= -meso)) {
                if (meso < 0 && (storageMesos - meso) < 0) { // storing with overflow
                    meso = -(2147483647 - storageMesos);
                    if ((-meso) > playerMesos) // should never happen just a failsafe
                        throw new RuntimeException("everything sucks");
                } else if (meso > 0 && (playerMesos + meso) < 0) { // taking out with overflow
                    meso = 2147483647 - playerMesos;
                    if (meso > storageMesos) // should never happen just a failsafe
                        throw new RuntimeException("everything sucks");
                }
                storage.setMeso(storageMesos - meso);
                c.getPlayer().gainMeso(meso, false, true, false);
            } else
                return;
            storage.sendMeso(c);
        } else if (mode == 8) // close
            storage.close();
    }
}