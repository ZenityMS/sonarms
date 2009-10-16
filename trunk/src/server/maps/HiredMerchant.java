package server.maps;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import client.IItem;
import client.MapleCharacter;
import client.MapleClient;
import tools.DatabaseConnection;
import net.MaplePacket;
import server.MapleInventoryManipulator;
import server.MaplePlayerShopItem;
import server.TimerManager;
import tools.MaplePacketCreator;

/**
 *
 * @author XoticStory
 */
public class HiredMerchant extends AbstractMapleMapObject {
    private int ownerId;
    private int itemId;
    private String ownerName = "";
    private String description = "";
    private MapleCharacter[] visitors = new MapleCharacter[3];
    private List<MaplePlayerShopItem> items = new LinkedList<MaplePlayerShopItem>();
    private boolean open;
    public ScheduledFuture<?> schedule = null;
    private MapleMap map;

    public HiredMerchant(final MapleCharacter owner, int itemId, String desc) {
        this.setPosition(owner.getPosition());
        this.ownerId = owner.getId();
        this.itemId = itemId;
        this.ownerName = owner.getName();
        this.description = desc;
        this.map = owner.getMap();
        this.schedule = TimerManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                HiredMerchant.this.closeShop(owner.getClient());
            }
        }, 1000 * 60 * 60 * 24);
    }

    public void broadcastToVisitors(MaplePacket packet) {
        for (MapleCharacter visitor : visitors)
            if (visitor != null)
                visitor.getClient().getSession().write(packet);
    }

    public void addVisitor(MapleCharacter visitor) {
        int i = this.getFreeSlot();
        if (i > -1) {
            visitors[i] = visitor;
            broadcastToVisitors(MaplePacketCreator.hiredMerchantVisitorAdd(visitor, i + 1));
        }
    }

    public void removeVisitor(MapleCharacter visitor) {
        int slot = getVisitorSlot(visitor);
        if (visitors[slot] == visitor) {
            visitors[slot] = null;
            broadcastToVisitors(MaplePacketCreator.hiredMerchantVisitorLeave(slot + 1, false));
        }
    }

    public int getVisitorSlot(MapleCharacter visitor) {
        for (int i = 0; i < 3; i++)
            if (visitors[i] == visitor)
                return i;
        return 1;
    }

    public void removeAllVisitors(String message) {
        for (int i = 0; i < 3; i++)
            if (visitors[i] != null) {
                visitors[i].getClient().getSession().write(MaplePacketCreator.hiredMerchantForceLeave1());
                visitors[i].getClient().getSession().write(MaplePacketCreator.hiredMerchantForceLeave2());
                if (message.length() > 0)
                    visitors[i].dropMessage(1, message);
                visitors[i] = null;
            }
    }

    public void buy(MapleClient c, int item, short quantity) {
        MaplePlayerShopItem pItem = items.get(item);
        synchronized (items) {
            IItem newItem = pItem.getItem().copy();
            newItem.setQuantity((short) (newItem.getQuantity() * quantity));
            if (c.getPlayer().getMeso() >= pItem.getPrice() * quantity) {
                c.getPlayer().gainMeso(-pItem.getPrice() * quantity, false);
                try {
                    PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET MerchantMesos = MerchantMesos + " + pItem.getPrice() * quantity + " WHERE id = ?");
                    ps.setInt(1, ownerId);
                    ps.executeUpdate();
                    ps.close();
                } catch (Exception e) {
                }
                MapleInventoryManipulator.addFromDrop(c, newItem, true);
                pItem.setBundles((short) (pItem.getBundles() - quantity));
                if (pItem.getBundles() < 1)
                    pItem.setDoesExist(false);
            } else
                c.getPlayer().dropMessage(1, "You do not have enough mesos.");
        }
    }

    public void closeShop(MapleClient c) {
        map.removeMapObject(this);
        map.broadcastMessage(MaplePacketCreator.destroyHiredMerchant(ownerId));
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET HasMerchant = 0 WHERE id = ?");
            ps.setInt(1, ownerId);
            ps.executeUpdate();
            ps.close();
            for (MaplePlayerShopItem mpsi : getItems())
                if (mpsi.getBundles() > 2)
                    MapleInventoryManipulator.addById(c, mpsi.getItem().getItemId(), (short) mpsi.getBundles(), null, -1);
                else if (mpsi.isExist())
                    MapleInventoryManipulator.addFromDrop(c, mpsi.getItem(), true);
        } catch (Exception e) {
        }
        schedule.cancel(false);
    }

    public String getOwner() {
        return ownerName;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getDescription() {
        return description;
    }

    public MapleCharacter[] getVisitors() {
        return visitors;
    }

    public List<MaplePlayerShopItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(MaplePlayerShopItem item) {
        items.add(item);
    }

    public void removeFromSlot(int slot) {
        items.remove(slot);
    }

    public int getFreeSlot() {
        for (int i = 0; i < 3; i++)
            if (visitors[i] == null)
                return i;
        return -1;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean set) {
        this.open = set;
    }

    public int getItemId() {
        return itemId;
    }

    public boolean isOwner(MapleCharacter chr) {
        return chr.getId() == ownerId && chr.getName().equals(ownerName);
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.HIRED_MERCHANT;
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.spawnHiredMerchant(this));
    }
}