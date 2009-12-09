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
import net.AbstractMaplePacketHandler;
import scripting.npc.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Generic
 */
public final class RemoteGachaponHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int type = slea.readInt();
        int mode = slea.readInt();
        if (c.getPlayer().getInventory(MapleItemInformationProvider.getInstance().getInventoryType(type)).countById(type) <= 0)
            return;
        MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, type, 1, true, true);
        if (type == 5451000) // Incase there is more later.
            handleRemoteGacha(c, mode);
    }

    private void handleRemoteGacha(MapleClient c, int mode) {
        int npcId = 9100100;
        if (mode != 8 && mode != 9)
            npcId += mode;
        else if (mode == 8)
            npcId = 9100109;
        else if (mode == 9)
            npcId = 9100117;
        NPCScriptManager.getInstance().start(c, npcId, null, null);
    }
}
