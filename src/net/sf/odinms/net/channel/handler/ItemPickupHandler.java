
package net.sf.odinms.net.channel.handler;


import java.util.Collection;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.net.world.MaplePartyCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MaplePet;
import net.sf.odinms.client.anticheat.CheatingOffense;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.server.maps.MapleMapItem;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class ItemPickupHandler extends AbstractMaplePacketHandler {
    public ItemPickupHandler() {
    }

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readByte();
        slea.readInt(); //?
        slea.readInt(); // position, but we dont need it o.o
        int oid = slea.readInt();
        MapleMapObject ob = c.getPlayer().getMap().getMapObject(oid);
        if (ob == null) {
            c.getSession().write(MaplePacketCreator.getInventoryFull());
            c.getSession().write(MaplePacketCreator.getShowInventoryFull());
            return;
        }
        if (ob instanceof MapleMapItem) {
            MapleMapItem mapitem = (MapleMapItem)ob;
            synchronized (mapitem) {
                if (mapitem.isPickedUp()) {
                    c.getSession().write(MaplePacketCreator.getInventoryFull());
                    c.getSession().write(MaplePacketCreator.getShowInventoryFull());
                    return;
                }
                double distance = c.getPlayer().getPosition().distanceSq(mapitem.getPosition());
                c.getPlayer().getCheatTracker().checkPickupAgain();
                if (distance > 90000.0) { // 300^2, 550 is approximatly the range of ultis
                    // AutobanManager.getInstance().addPoints(c, 100, 300000, "Itemvac");
                    c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.ITEMVAC);
                    // Double.valueOf(Math.sqrt(distance))
                } else if (distance > 30000.0) {
                    // log.warn("[h4x] Player {} is picking up an item that's fairly far away: {}", c.getPlayer().getName(), Double.valueOf(Math.sqrt(distance)));
                    c.getPlayer().getCheatTracker().registerOffense(CheatingOffense.SHORT_ITEMVAC);
                }
                                
                if (mapitem.getMeso() > 0) {
                    if (c.getPlayer().getParty() != null) {
                        ChannelServer cserv = c.getChannelServer();
                        int mesoAmm = mapitem.getMeso();
                        int partyNum = 0;
                        Collection<MaplePartyCharacter> partyMembers = c.getPlayer().getParty().getMembers();
                        for (MaplePartyCharacter partyMember : partyMembers) {
                            if (partyMember.getMapid() == c.getPlayer().getMapId()) {
                                partyNum++;
                            }
                        }
                        int mesoGain = mesoAmm / partyNum;
                        for (MaplePartyCharacter partyMember : partyMembers) {
                            if (partyMember.getMapid() == c.getPlayer().getMapId()) {
                                cserv.getPlayerStorage().getCharacterById(partyMember.getId()).gainMeso(mesoGain, true, true);
                            }
                        }
                    } else {
                        c.getPlayer().gainMeso(mapitem.getMeso(), true, true);
                    }
                    c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, c.getPlayer().getId()), mapitem.getPosition());
                    c.getPlayer().getCheatTracker().pickupComplete();
                    c.getPlayer().getMap().removeMapObject(ob);
                } else {
                    StringBuilder logInfo = new StringBuilder("Picked up by ");
                    logInfo.append(c.getPlayer().getName());
                    if (mapitem.getItem().getItemId() >= 5000000 && mapitem.getItem().getItemId() <= 5000100) {
                        int petId = MaplePet.createPet(mapitem.getItem().getItemId());
                        if (petId == -1) {
                            return;
                        }
                        MapleInventoryManipulator.addById(c, mapitem.getItem().getItemId(), mapitem.getItem().getQuantity(), "Cash Item was purchased.", null, petId);
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, c.getPlayer().getId()), mapitem.getPosition());
                        c.getPlayer().getCheatTracker().pickupComplete();
                        c.getPlayer().getMap().removeMapObject(ob);
                    } else {
                        if (MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), logInfo.toString())) {
                            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 2, c.getPlayer().getId()), mapitem.getPosition());
                            c.getPlayer().getCheatTracker().pickupComplete();
                            c.getPlayer().getMap().removeMapObject(ob);
                        } else {
                            c.getPlayer().getCheatTracker().pickupComplete();
                            return;
                        }
                    if(mapitem.getItem().getItemId() == 1002357){ c.getPlayer().finishAchievement(13);
                                                        }
                                        }
                }
                mapitem.setPickedUp(true);
            }
        }
        c.getSession().write(MaplePacketCreator.enableActions());
    }

  }  