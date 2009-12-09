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
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class ReportHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readByte();
        String victim = slea.readMapleAsciiString();
        slea.readByte();
        String description = slea.readMapleAsciiString();
        if (c.getPlayer().getPossibleReports() > 0)
            if (c.getPlayer().getMeso() > 299) {
                c.getPlayer().decreaseReports();
                c.getSession().write(MaplePacketCreator.reportResponse((byte) 2, c.getPlayer().getPossibleReports()));
            } else
                return;
        else {
            c.getPlayer().dropMessage("You do not have any reports left.");
            return;
        }
        c.getChannelServer().broadcastGMPacket(MaplePacketCreator.serverNotice(6, victim + " was reported for: " + description));
    }
}
