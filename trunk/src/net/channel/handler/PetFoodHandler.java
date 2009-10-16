package net.channel.handler;

import client.ExpTable;
import client.MapleClient;
import client.MapleInventoryType;
import client.MaplePet;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class PetFoodHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer().getNoPets() == 0) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        int previousFullness = 100;
        int slot = 0;
        MaplePet[] pets = c.getPlayer().getPets();
        for (int i = 0; i < 3; i++)
            if (pets[i] != null)
                if (pets[i].getFullness() < previousFullness) {
                    slot = i;
                    previousFullness = pets[i].getFullness();
                }
        MaplePet pet = c.getPlayer().getPet(slot);
        slea.readInt();
        slea.readShort();
        int itemId = slea.readInt();
        boolean gainCloseness = false;
        if (Math.random() > .5)
            gainCloseness = true;
        if (pet.getFullness() < 100) {
            int newFullness = pet.getFullness() + 30;
            if (newFullness > 100)
                newFullness = 100;
            pet.setFullness(newFullness);
            if (gainCloseness && pet.getCloseness() < 30000) {
                int newCloseness = pet.getCloseness() + c.getChannelServer().getPetExpRate();
                if (newCloseness > 30000)
                    newCloseness = 30000;
                pet.setCloseness(newCloseness);
                if (newCloseness >= ExpTable.getClosenessNeededForLevel(pet.getLevel() + 1)) {
                    pet.setLevel(pet.getLevel() + 1);
                    c.getSession().write(MaplePacketCreator.showOwnPetLevelUp(c.getPlayer().getPetIndex(pet)));
                    c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.showPetLevelUp(c.getPlayer(), c.getPlayer().getPetIndex(pet)));
                }
            }
            c.getSession().write(MaplePacketCreator.updatePet(pet));
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.commandResponse(c.getPlayer().getId(), (byte) 1, slot, true, true), true);
        } else {
            if (gainCloseness) {
                int newCloseness = pet.getCloseness() - c.getChannelServer().getPetExpRate();
                if (newCloseness < 0)
                    newCloseness = 0;
                pet.setCloseness(newCloseness);
                if (newCloseness < ExpTable.getClosenessNeededForLevel(pet.getLevel()))
                    pet.setLevel(pet.getLevel() - 1);
            }
            c.getSession().write(MaplePacketCreator.updatePet(pet));
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.commandResponse(c.getPlayer().getId(), (byte) 1, slot, false, true), true);
        }
        MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, false);
    }
}