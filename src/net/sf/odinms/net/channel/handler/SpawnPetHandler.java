

package net.sf.odinms.net.channel.handler;

import java.awt.Point;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MaplePet;
import net.sf.odinms.client.PetDataFactory;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.client.messages.ServernoticeMapleClientMessageCallback;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class SpawnPetHandler extends AbstractMaplePacketHandler {
	
	/*	TODO:
	 *	1.  Move the equpping into a function.
	 */ 
	
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		slea.readInt();
		byte slot = slea.readByte();
		slea.readByte();
		boolean lead = slea.readByte() == 1;

		// Handle dragons
		if (c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slot).getItemId() == 5000028) {
			new ServernoticeMapleClientMessageCallback(5, c).dropMessage("Dragon eggs cannot currently be hatched.");
			c.getSession().write(MaplePacketCreator.enableActions());
			return;
		}

		// New instance of MaplePet - using the item ID and unique pet ID
		MaplePet pet = MaplePet.loadFromDb(c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slot).getItemId(), slot, c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slot).getPetId());

		// Assign the pet to the player, set stats
		if (c.getPlayer().getPetIndex(pet) != -1) {
			c.getPlayer().unequipPet(pet, true);
		} else {			
			if (c.getPlayer().getSkillLevel(SkillFactory.getSkill(8)) == 0 && c.getPlayer().getPet(0) != null) {
				c.getPlayer().unequipPet(c.getPlayer().getPet(0), false);
			}

			if (lead) {
				c.getPlayer().shiftPetsRight();
			}
			
			Point pos = c.getPlayer().getPosition();
			pos.y -= 12;
			pet.setPos(pos);
			pet.setFh(c.getPlayer().getMap().getFootholds().findBelow(pet.getPos()).getId());
			pet.setStance(0);
			
			c.getPlayer().addPet(pet);
			
			// Broadcast packet to the map...
			c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.showPet(c.getPlayer(), pet, false));

			// Write the stat update to the player...
			c.getSession().write(MaplePacketCreator.petStatUpdate(c.getPlayer()));
			c.getSession().write(MaplePacketCreator.enableActions());

			// Get the data
			int hunger = PetDataFactory.getHunger(pet.getItemId());

			// Start the fullness schedule
			c.getPlayer().startFullnessSchedule(hunger, pet, c.getPlayer().getPetIndex(pet));

		}
	}	
}
