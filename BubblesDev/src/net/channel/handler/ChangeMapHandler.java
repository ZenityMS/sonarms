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

import java.net.InetAddress;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.MapleStat;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import server.MapleInventoryManipulator;
import server.MaplePortal;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class ChangeMapHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (slea.available() == 0) {
            if (c.getPlayer().getParty() != null)
                c.getPlayer().setParty(c.getPlayer().getParty());
            String ip = ChannelServer.getInstance(c.getChannel()).getIP(c.getChannel());
            String[] socket = ip.split(":");
            c.getPlayer().saveToDB(true);
            c.getPlayer().setInCS(false);
            ChannelServer.getInstance(c.getChannel()).removePlayer(c.getPlayer());
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
            try {
                c.getSession().write(MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
                c.getSession().close(true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            slea.readByte(); // 1 = from dying 2 = regular portals
            int targetid = slea.readInt(); // FF FF FF FF
            String startwp = slea.readMapleAsciiString();
            MaplePortal portal = c.getPlayer().getMap().getPortal(startwp);
            slea.skip(1);
            boolean wheel = slea.readShort() > 0;
            MapleCharacter player = c.getPlayer();
            if (targetid != -1 && !c.getPlayer().isAlive()) {
                boolean executeStandardPath = true;
                if (player.getEventInstance() != null)
                    executeStandardPath = player.getEventInstance().revivePlayer(player);
                if (executeStandardPath)
                    if (wheel) {
                        if (c.getPlayer().haveItem(5510000, 1, false, true)) {
                            c.getPlayer().setHp(50);
                            MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, 5510000, 1, true, false);
                            c.getPlayer().changeMap(c.getPlayer().getMap(), c.getPlayer().getMap().getPortal(0));
                            c.getPlayer().updateSingleStat(MapleStat.HP, 50);
                        }
                    } else {
                        c.getPlayer().cancelAllBuffs();
                        player.setHp(50);
                        MapleMap to = c.getPlayer().getMap().getReturnMap();
                        MaplePortal pto = to.getPortal(0);
                        player.setStance(0);
                        player.changeMap(to, pto);
                    }
            } else if (targetid != -1 && (c.getPlayer().isGM() || c.getPlayer().getAllowWarpToId() == targetid)) {
                MapleMap to = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(targetid);
                MaplePortal pto = to.getPortal(0);
                player.changeMap(to, pto);
            } else if (portal != null)
                portal.enterPortal(c);
            else
                c.getSession().write(MaplePacketCreator.enableActions());
        }
        c.getPlayer().setExpMod(false, false); // will only fail when hour changes while player is in map
        c.getPlayer().setDropMod();// but this beats checking 15 booleans every monster kill
    }
}
