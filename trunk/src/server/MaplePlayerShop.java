package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import client.IItem;
import client.MapleCharacter;
import client.MapleClient;
import net.MaplePacket;
import server.maps.AbstractMapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;

/**
 *
 * @author Matze
 */
public class MaplePlayerShop extends AbstractMapleMapObject {
    private MapleCharacter owner;
    private MapleCharacter[] visitors = new MapleCharacter[3];
    private List<MaplePlayerShopItem> items = new ArrayList<MaplePlayerShopItem>();
    private MapleCharacter[] slot = {null, null, null};
    private String description;
    private int boughtnumber = 0;

    public MaplePlayerShop(MapleCharacter owner, String description) {
        this.owner = owner;
        this.description = description;
    }

    public boolean hasFreeSlot() {
        return visitors[0] == null || visitors[1] == null || visitors[2] == null;
    }

    public boolean isOwner(MapleCharacter c) {
        return owner.equals(c);
    }

    public void addVisitor(MapleCharacter visitor) {
        for (int i = 0; i < 3; i++)
            if (visitors[i] == null) {
                visitors[i] = visitor;
                if (this.getSlot(1) == null) {
                    this.setSlot(visitor, 1);
                    this.broadcast(MaplePacketCreator.getPlayerShopNewVisitor(visitor, 1));
                } else if (this.getSlot(2) == null) {
                    this.setSlot(visitor, 2);
                    this.broadcast(MaplePacketCreator.getPlayerShopNewVisitor(visitor, 2));
                } else if (this.getSlot(3) == null) {
                    this.setSlot(visitor, 3);
                    this.broadcast(MaplePacketCreator.getPlayerShopNewVisitor(visitor, 3));
                    visitor.getMap().broadcastMessage(MaplePacketCreator.addCharBox(this.getOwner(), 1));
                }
                break;
            }
    }

    public void removeVisitor(MapleCharacter visitor) {
        for (int i = 0; i < 3; i++)
            if (visitors[i] == visitor) {
                visitors[i] = null;
                if (visitor.getSlot() == 1) {
                    this.setSlot(null, 1);
                    visitor.setSlot(0);
                    this.broadcastToVisitors(MaplePacketCreator.getPlayerShopRemoveVisitor(1));
                    break;
                } else if (visitor.getSlot() == 2) {
                    this.setSlot(null, 2);
                    visitor.setSlot(0);
                    this.broadcast(MaplePacketCreator.getPlayerShopRemoveVisitor(2));
                    break;
                } else if (visitor.getSlot() == 3) {
                    this.setSlot(null, 3);
                    visitor.setSlot(0);
                    this.broadcast(MaplePacketCreator.getPlayerShopRemoveVisitor(3));
                    break;
                }
                if (i == 3)
                    visitor.getMap().broadcastMessage(MaplePacketCreator.addCharBox(this.getOwner(), 4));
                break;
            }
    }

    public boolean isVisitor(MapleCharacter visitor) {
        return visitors[0] == visitor || visitors[1] == visitor || visitors[2] == visitor;
    }

    public void addItem(MaplePlayerShopItem item) {
        items.add(item);
    }

    public void removeItem(int item) {
        items.remove(item);
    }

    /**
     * no warnings for now o.op
     * @param c
     * @param item
     * @param quantity
     */
    public void buy(MapleClient c, int item, short quantity) {
        if (isVisitor(c.getPlayer())) {
            MaplePlayerShopItem pItem = items.get(item);
            owner = this.getOwner();
            synchronized (c.getPlayer()) {
                if (c.getPlayer().getMeso() >= pItem.getPrice() * quantity) {
                    IItem newItem = pItem.getItem().copy();
                    if (MapleInventoryManipulator.addFromDrop(c, newItem, false)) {
                        newItem.setQuantity((short) (newItem.getQuantity() * quantity));
                        c.getPlayer().gainMeso(-pItem.getPrice() * quantity, true);
                        owner.gainMeso(pItem.getPrice() * quantity, true);
                        pItem.setBundles((short) (pItem.getBundles() - quantity));
                        boughtnumber++;
                        if (boughtnumber == items.size()) {
                            owner.setPlayerShop(null);
                            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeCharBox(c.getPlayer()));
                            this.removeVisitors();
                            owner.dropMessage(1, "Your items are sold out, and therefore your shop is closed.");
                        }
                    } else
                        c.getPlayer().dropMessage(1, "Your inventory is full. Please clean a slot before buying this item.");
                }
            }
        }
    }

    public void broadcastToVisitors(MaplePacket packet) {
        for (int i = 0; i < 3; i++)
            if (visitors[i] != null)
                visitors[i].getClient().getSession().write(packet);
    }

    public void removeVisitors() {
        for (int i = 0; i < 3; i++) {
            if (visitors[i] != null)
                visitors[i].changeMap(visitors[i].getMap(), visitors[i].getPosition());
            if (this.getOwner() != null)
                this.getOwner().changeMap(this.getOwner().getMap(), this.getOwner().getPosition());
        }
    }

    public void broadcast(MaplePacket packet) {
        if (owner.getClient() != null && owner.getClient().getSession() != null)
            owner.getClient().getSession().write(packet);
        broadcastToVisitors(packet);
    }

    public void chat(MapleClient c, String chat) {
        byte slot = 0;
        for (MapleCharacter mc : getVisitors()) {
            slot++;
            if (mc != null) {
                if (mc.getName().equalsIgnoreCase(c.getPlayer().getName()))
                    break;
            } else if (slot == 3)
                slot = 0;
        }
        broadcast(MaplePacketCreator.getPlayerShopChat(c.getPlayer(), chat, slot));
    }

    public void sendShop(MapleClient c) {
        c.getSession().write(MaplePacketCreator.getPlayerShop(c, this, isOwner(c.getPlayer())));
    }

    public MapleCharacter getOwner() {
        return owner;
    }

    public MapleCharacter[] getVisitors() {
        return visitors;
    }

    public MapleCharacter getSlot(int s) {
        return slot[s];
    }

    private void setSlot(MapleCharacter person, int s) {
        slot[s] = person;
        if (person != null)
            person.setSlot(s);
    }

    public List<MaplePlayerShopItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.SHOP;
    }
}