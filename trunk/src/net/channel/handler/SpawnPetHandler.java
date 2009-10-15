package net.channel.handler;

import java.awt.Point;
import java.io.File;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import client.MapleClient;
import client.MapleInventoryType;
import client.MaplePet;
import client.MapleStat;
import client.PetDataFactory;
import client.SkillFactory;
import tools.DatabaseConnection;
import net.AbstractMaplePacketHandler;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

public class SpawnPetHandler extends AbstractMaplePacketHandler {
    private static MapleDataProvider dataRoot = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Item.wz"));

    private void deletePet(int itemid, MapleClient c) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM pets WHERE `petid` = ?");
            ps.setInt(1, c.getPlayer().getInventory(MapleInventoryType.CASH).findById(itemid).getPetId());
            ps.executeUpdate();
            ps.close();
        } catch (Exception ex) {
        }
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readInt();
        byte slot = slea.readByte();
        slea.readByte();
        boolean lead = slea.readByte() == 1;
        int petid = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slot).getItemId();
        if (petid == 5000028) //Handles Dragon
            if (c.getPlayer().haveItem(5000029)) {
                c.getPlayer().dropMessage(5, "You can't hatch your Dragon egg if you already have a Baby Dragon.");
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            } else {
                int evolveid = MapleDataTool.getInt("info/evol1", dataRoot.getData("Pet/" + petid + ".img"));
                int petId = MaplePet.createPet(evolveid);
                if (petId == -1)
                    return;
                deletePet(petid, c);
                MapleInventoryManipulator.addById(c, evolveid, (short) 1, null, petId);
                MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, petid, (short) 1, false, false);
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            } // Handle robo
        else if (petid == 5000047)
            if (c.getPlayer().haveItem(5000048)) {
                c.getPlayer().dropMessage(5, "You can't hatch your Robo egg if you already have a Baby Robo.");
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            } else {
                MapleData petData = dataRoot.getData("Pet/" + petid + ".img");
                int evolveid = MapleDataTool.getInt("info/evol1", petData);
                int petId = MaplePet.createPet(evolveid);
                if (petId == -1)
                    return;
                deletePet(petid, c);
                MapleInventoryManipulator.addById(c, evolveid, (short) 1, null, petId);
                MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, petid, (short) 1, false, false);
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
        MaplePet pet = MaplePet.loadFromDb(c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slot).getItemId(), slot, c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slot).getPetId());
        if (c.getPlayer().getPetIndex(pet) != -1)
            c.getPlayer().unequipPet(pet, true);
        else {
            if (c.getPlayer().getSkillLevel(SkillFactory.getSkill(8)) == 0 && c.getPlayer().getPet(0) != null)
                c.getPlayer().unequipPet(c.getPlayer().getPet(0), false);
            if (lead)
                c.getPlayer().shiftPetsRight();
            Point pos = c.getPlayer().getPosition();
            pos.y -= 12;
            pet.setPos(pos);
            pet.setFh(c.getPlayer().getMap().getFootholds().findBelow(pet.getPos()).getId());
            pet.setStance(0);
            c.getPlayer().addPet(pet);
            c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showPet(c.getPlayer(), pet, false), true);
            List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
            stats.add(new Pair<MapleStat, Integer>(MapleStat.PET, Integer.valueOf(pet.getUniqueId())));
            c.getSession().write(MaplePacketCreator.petStatUpdate(c.getPlayer()));
            c.getSession().write(MaplePacketCreator.enableActions());
            c.getPlayer().startFullnessSchedule(PetDataFactory.getHunger(pet.getItemId()), pet, c.getPlayer().getPetIndex(pet));
        }
    }
}