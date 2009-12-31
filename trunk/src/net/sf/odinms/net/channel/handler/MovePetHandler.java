package net.sf.odinms.net.channel.handler;

import java.awt.Point;
import java.util.Arrays;
import java.util.List;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.server.maps.MapleMapItem;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.server.maps.MapleMapObjectType;
import net.sf.odinms.server.movement.LifeMovementFragment;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import net.sf.odinms.tools.data.input.StreamUtil;

public class MovePetHandler extends AbstractMovementPacketHandler {

	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MovePetHandler.class);
    
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int petId = slea.readInt();
		slea.readInt();
		
		@SuppressWarnings("unused")
		Point startPos = StreamUtil.readShortPoint(slea);
		List<LifeMovementFragment> res = parseMovement(slea);

		MapleCharacter player = c.getPlayer();
		int slot = player.getPetIndex(petId);
		if (slot == -1) {
			log.warn("[h4x] {} ({}) trying to move a pet he/she does not own.", c.getPlayer().getName(), c.getPlayer().getId());
			return;
		}
		player.getPet(slot).updatePosition(res);
		player.getMap().broadcastMessage(player, MaplePacketCreator.movePet(player.getId(), petId, slot, res), false);
                
                Boolean meso = false;
                Boolean item = false;
                if (c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(1812001) != null) 
                        item = true;
                if (c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).findById(1812000) != null)
                        meso = true;
                
                if (meso || item) {
                        List<MapleMapObject> objects = player.getMap().getMapObjectsInRange(player.getPosition(), MapleCharacter.MAX_VIEW_RANGE_SQ, Arrays.asList(MapleMapObjectType.ITEM));

                        for (LifeMovementFragment move : res) {
                                Point petPos = move.getPosition();
                                double petX = petPos.getX();
                                double petY = petPos.getY();
                                for (MapleMapObject map_object : objects) {
                                        Point objectPos = map_object.getPosition();
                                        double objectX = objectPos.getX();
                                        double objectY = objectPos.getY();
                                        if (Math.abs(petX - objectX) <= 30 || Math.abs(objectX - petX) <= 30) {
                                                if (Math.abs(petY - objectY) <= 30 || Math.abs(objectY - petY) <= 30) {
                                                        if (map_object instanceof MapleMapItem) {
                                                                MapleMapItem mapitem = (MapleMapItem)map_object;
                                                                synchronized (mapitem) {
                                                                        if (mapitem.isPickedUp() || mapitem.getOwner().getId() != player.getId()) {
                                                                                continue;
                                                                        }
                                                                        if (mapitem.getMeso() > 0 && meso) {
                                                                                c.getPlayer().gainMeso(mapitem.getMeso(), true, true);
                                                                                c.getPlayer().getMap().broadcastMessage(
                                                                                        MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 5, c.getPlayer().getId(), true, slot),
                                                                                        mapitem.getPosition());
                                                                                c.getPlayer().getMap().removeMapObject(map_object);
                                                                                mapitem.setPickedUp(true);
                                                                        } 
                                                                        else {
                                                                                if (item) {
                                                                                        StringBuilder logInfo = new StringBuilder("Picked up by ");
                                                                                        logInfo.append(c.getPlayer().getName());
                                                                                        if (MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), logInfo.toString())) {
                                                                                                c.getPlayer().getMap().broadcastMessage(
                                                                                                        MaplePacketCreator.removeItemFromMap(mapitem.getObjectId(), 5, c.getPlayer().getId(), true, slot),
                                                                                                        mapitem.getPosition());
                                                                                                c.getPlayer().getMap().removeMapObject(map_object);
                                                                                                mapitem.setPickedUp(true);
                                                                                        } 
                                                                                }
                                                                        }
                                                                }
                                                        }                                         
                                                }
                                        }
                                }                        
                        }
                }
	}
}