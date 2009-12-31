

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.odinms.net.channel.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.odinms.client.IItem;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.AutobanManager;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.life.MapleLifeFactory;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author AngelSL
 */
public class UseSummonBag extends AbstractMaplePacketHandler {
    private static Logger log = LoggerFactory.getLogger(UseItemHandler.class);
	public UseSummonBag() {
	}
    
        public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		if (!c.getPlayer().isAlive()) {
			c.getSession().write(MaplePacketCreator.enableActions());
			return;
		}
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		slea.readInt(); // i have no idea :) (o.o)
		byte slot = (byte)slea.readShort();
		int itemId = slea.readInt(); //as if we cared... ;)
		//List<IItem> existing = c.getPlayer().getInventory(MapleInventoryType.USE).listById(itemId);
		IItem toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
		if (toUse != null && toUse.getQuantity() > 0) {
			if (toUse.getItemId() != itemId) {
				log.info("[h4x] Player {} is using a summonbag not in the slot: {}", c.getPlayer().getName(), Integer.valueOf(itemId));
				AutobanManager.getInstance().autoban(c, "Using a summoning sack that is not available. Item ID: " + itemId + ". Slot: " + slot + ".");
			}
			MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
			int[][] toSpawn = ii.getSummonMobs(itemId);
                        for(int z = 0; z < toSpawn.length; z++){
				int[] toSpawnChild = toSpawn[z];
				if((int) Math.ceil(Math.random() * 100) <= toSpawnChild[1]){
					MapleMonster ht = MapleLifeFactory.getMonster(toSpawnChild[0]);
					c.getPlayer().getMap().spawnMonsterOnGroudBelow(ht, c.getPlayer().getPosition());
				}
                        }
		} else {
			log.info("[h4x] Player {} is using a summonbag he does not have: {}", c.getPlayer().getName(), Integer.valueOf(itemId));
			AutobanManager.getInstance().autoban(c, "Using a summoning sack that is not available. Item ID: " + itemId + ". Slot: " + slot + ".");
		}
		c.getSession().write(MaplePacketCreator.enableActions());
	}
}
