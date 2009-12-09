/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package net.channel.handler;

import client.Equip;
import client.Item;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventory;
import client.MapleInventoryType;
import client.MapleJob;
import client.MapleSkinColor;
import java.sql.PreparedStatement;
import net.AbstractMaplePacketHandler;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Jay Estrella
 */
public final class CreateCygnusHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        String name = slea.readMapleAsciiString();
        int face = slea.readInt();
        int hair = slea.readInt();
        int hairColor = slea.readInt();
        int skinColor = slea.readInt();
        int top = slea.readInt();
        int bottom = slea.readInt();
        int shoes = slea.readInt();
        int weapon = slea.readInt();
        int gender = slea.readByte();
        if (!MapleCharacter.canCreateChar(name)) {
            c.getSession().write(MaplePacketCreator.sendCygnusMessage(3));
            return;
        } else if (c.loadCharacters(c.getWorld()).size() >= c.getCharacterSlots()) { // remove for better usage...
            c.getSession().write(MaplePacketCreator.sendCygnusMessage(2));
            return;
        } else if (c.getPlayer().isLinked() || c.getPlayer().getJob().getId() / 1000 == 1)
            return;
        MapleCharacter newchar = MapleCharacter.getDefault(c);
        newchar.setWorld(c.getWorld());
        newchar.setFace(face);
        newchar.setHair(hair + hairColor);
        newchar.setGender(gender);
        newchar.setName(name);
        newchar.setSkinColor(MapleSkinColor.getById(skinColor));
        newchar.setMap(c.getChannelServer().getMapFactory().getMap(130000000)); // just to save the id
        newchar.setJob(MapleJob.NOBLESSE);
        MapleInventory equip = newchar.getInventory(MapleInventoryType.EQUIPPED);
        Equip eq_top = new Equip(top, (byte) -5);
        eq_top.setWdef((short) 3);
        eq_top.setUpgradeSlots((byte) 7);
        equip.addFromDB(eq_top.copy());
        Equip eq_bottom = new Equip(bottom, (byte) -6);
        eq_bottom.setWdef((short) 2);
        eq_bottom.setUpgradeSlots((byte) 7);
        equip.addFromDB(eq_bottom.copy());
        Equip eq_shoes = new Equip(shoes, (byte) -7);
        eq_shoes.setWdef((short) 2);
        eq_shoes.setUpgradeSlots((byte) 7);
        equip.addFromDB(eq_shoes.copy());
        Equip eq_weapon = new Equip(weapon, (byte) -11);
        eq_weapon.setWatk((short) 15);
        eq_weapon.setUpgradeSlots((byte) 7);
        equip.addFromDB(eq_weapon.copy());
        newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161001, (byte) 0, (short) 1));
        newchar.setGM(c.getPlayer().gmLevel());
        newchar.saveToDB(false);
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO koccharacters (knightId, linkedId) VALUES (?, ?)");
            ps.setInt(1, MapleCharacter.getIdByName(name));
            ps.setInt(2, c.getPlayer().getId());
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
        }
        c.getSession().write(MaplePacketCreator.sendCygnusMessage(0));
    }
}
