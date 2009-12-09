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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import client.MapleClient;
import client.MaplePet;
import java.sql.Connection;
import tools.DatabaseConnection;
import net.AbstractMaplePacketHandler;
import server.CashItemFactory;
import server.CashItemInfo;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class BuyCSItemHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        final int action = slea.readByte();
        slea.readByte();
        if (action == 3) {
            final int useNX = slea.readInt();
            final int snCS = slea.readInt();
            final CashItemInfo item = CashItemFactory.getInstance().getItem(snCS);
            if (c.getPlayer().getCSPoints(useNX) >= item.getPrice())
                c.getPlayer().modifyCSPoints(useNX, -item.getPrice());
            else {
                c.getSession().write(MaplePacketCreator.enableActions());
                return;
            }
            if (item.getId() >= 5000000 && item.getId() <= 5000100) {
                final int petId = MaplePet.createPet(item.getId());
                if (petId == -1)
                    return;
                MapleInventoryManipulator.addById(c, item.getId(), (short) 1, null, petId);
            } else
                MapleInventoryManipulator.addById(c, item.getId(), (short) item.getCount(), null);
            c.getSession().write(MaplePacketCreator.showBoughtCSItem(c, item));
            showCS(c);
        } else if (action == 5) {
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM wishlist WHERE charid = ?");
                ps.setInt(1, c.getPlayer().getId());
                ps.executeUpdate();
                ps.close();
                int i = 10;
                while (i > 0) {
                    int sn = slea.readInt();
                    if (sn != 0) {
                        ps = con.prepareStatement("INSERT INTO wishlist(charid, sn) VALUES(?, ?) ");
                        ps.setInt(1, c.getPlayer().getId());
                        ps.setInt(2, sn);
                        ps.executeUpdate();
                        ps.close();
                    }
                    i--;
                }
            } catch (SQLException se) {
            }
            c.getSession().write(MaplePacketCreator.sendWishList(c.getPlayer().getId(), true));
        } else if (action == 7) {
            slea.readByte();
            byte toCharge = slea.readByte();
            int toIncrease = slea.readInt();
            if (c.getPlayer().getCSPoints(toCharge) >= 4000 && c.getPlayer().getStorage().getSlots() < 48) { // 48 is max.
                c.getPlayer().modifyCSPoints(toCharge, -4000);
                if (toIncrease == 0)
                    c.getPlayer().getStorage().gainSlots((byte) 4);
                showCS(c);
            }
        } else if (action == 31) {
            CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());
            if (c.getPlayer().getMeso() >= item.getPrice() && (item.getId() == 4031180 || item.getId() == 4031192)) {
                c.getPlayer().gainMeso(-item.getPrice(), false);
                MapleInventoryManipulator.addById(c, item.getId(), (short) item.getCount(), null);
                c.getSession().write(MaplePacketCreator.showBoughtCSQuestItem(item.getId()));
            }
        }
    }

    private static final void showCS(MapleClient c) {
        c.getSession().write(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
        c.getSession().write(MaplePacketCreator.enableCSUse0());
        c.getSession().write(MaplePacketCreator.enableCSUse1());
        c.getSession().write(MaplePacketCreator.enableCSUse2());
        c.getSession().write(MaplePacketCreator.enableCSUse3());
        c.getSession().write(MaplePacketCreator.enableActions());
    }
}
