package net.channel.handler;

import client.ExpTable;
import client.MapleClient;
import client.MaplePet;
import client.PetCommand;
import client.PetDataFactory;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class PetCommandHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int petId = slea.readInt();
        int petIndex = c.getPlayer().getPetIndex(petId);
        MaplePet pet = null;
        if (petIndex == -1)
            return;
        else
            pet = c.getPlayer().getPet(petIndex);
        slea.readInt();
        slea.readByte();
        byte command = slea.readByte();
        PetCommand petCommand = PetDataFactory.getPetCommand(pet.getItemId(), (int) command);
        boolean success = false;
        if (Math.random() * 100 <= petCommand.getProbability()) {
            success = true;
            if (pet.getCloseness() < 30000) {
                int newCloseness = pet.getCloseness() + (petCommand.getIncrease() * c.getChannelServer().getPetExpRate());
                if (newCloseness > 30000)
                    newCloseness = 30000;
                pet.setCloseness(newCloseness);
                if (newCloseness >= ExpTable.getClosenessNeededForLevel(pet.getLevel() + 1)) {
                    pet.setLevel(pet.getLevel() + 1);
                    c.getSession().write(MaplePacketCreator.showOwnPetLevelUp(c.getPlayer().getPetIndex(pet)));
                    c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.showPetLevelUp(c.getPlayer(), c.getPlayer().getPetIndex(pet)));
                }
                c.getSession().write(MaplePacketCreator.updatePet(pet));
            }
        }
        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.commandResponse(c.getPlayer().getId(), command, petIndex, success, false), true);
    }
}