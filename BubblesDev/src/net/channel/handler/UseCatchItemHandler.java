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

import client.MapleClient;
import client.MapleInventoryType;
import tools.Randomizer;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author BubblesDev
 */
public final class UseCatchItemHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        /*[E1 55 F2 00] TimeStamp [08 00] type [30 A3 22 00]ItemId [6D 00 00 00] MonsterObID*/
        slea.readInt();
        short type = slea.readShort();
        switch (type) {
            case 0x08:
                int itemId = slea.readInt();
                int monsObId = slea.readInt();
                if (c.getPlayer().getInventory(MapleItemInformationProvider.getInstance().getInventoryType(itemId)).countById(itemId) <= 0)
                    return;
                MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemId, 1, true, true);
                if (c.getPlayer().getMap().getMonsterByOid(monsObId) == null)
                    return;
                if (Randomizer.getInstance().nextInt(10) > 3) {
                    if (c.getPlayer().getMap().getMonsterByOid(monsObId).getId() == 9300101)
                        MapleInventoryManipulator.addById(c, 1902000, (short) 1, "");
                    c.getSession().write(MaplePacketCreator.enableActions());
                }
                break;
            default:
                System.out.println("UseCatchItemHandler: \r\n" + slea.toString());
        }
    }
}
