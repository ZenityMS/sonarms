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
package client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import tools.DatabaseConnection;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;

/**
 *
 * @author Danny
 */
public class MapleRing implements Comparable<MapleRing> {
    private int ringId;
    private int ringId2;
    private int partnerId;
    private int itemId;
    private String partnerName;
    private boolean equipped;

    private MapleRing(int id, int id2, int partnerId, int itemid, String partnername) {
        this.ringId = id;
        this.ringId2 = id2;
        this.partnerId = partnerId;
        this.itemId = itemid;
        this.partnerName = partnername;
    }

    public static MapleRing loadFromDb(int ringId) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM rings WHERE id = ?");
            ps.setInt(1, ringId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            MapleRing ret = new MapleRing(ringId, rs.getInt("partnerRingId"), rs.getInt("partnerChrId"), rs.getInt("itemid"), rs.getString("partnerName"));
            rs.close();
            ps.close();
            return ret;
        } catch (Exception ex) {
            return null;
        }
    }

    public static int[] createRing(MapleClient c, int itemid, int chrId, String chrName, int partnerId, String partnername, String message) {
        try {
            MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterById(partnerId);
            if (chr == null) {
                int[] ret_ = new int[2];
                ret_[0] = -1;
                ret_[1] = -1;
                return ret_;
            }
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO rings (itemid, partnerChrId, partnername) VALUES (?, ?, ?)");
            ps.setInt(1, itemid);
            ps.setInt(2, partnerId);
            ps.setString(3, partnername);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            int[] ret = new int[2];
            ret[0] = rs.getInt(1);
            rs.close();
            ps.close();
            ps = con.prepareStatement("INSERT INTO rings (itemid, partnerRingId, partnerChrId, partnername) VALUES (?, ?, ?, ?)");
            ps.setInt(1, itemid);
            ps.setInt(2, ret[0]);
            ps.setInt(3, chrId);
            ps.setString(4, chrName);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            rs.next();
            ret[1] = rs.getInt(1);
            rs.close();
            ps.close();
            ps = con.prepareStatement("UPDATE rings SET partnerRingId = ? WHERE id = ?");
            ps.setInt(1, ret[1]);
            ps.setInt(2, ret[0]);
            ps.executeUpdate();
            ps.close();
            MapleCharacter player = c.getPlayer();
            MapleInventoryManipulator.addRing(player, itemid, ret[0]);
            MapleInventoryManipulator.addRing(chr, itemid, ret[1]);
            c.getSession().write(MaplePacketCreator.getCharInfo(player));
            if (message == null) {
                player.getMap().removePlayer(player);
                player.getMap().addPlayer(player);
                chr.getClient().getSession().write(MaplePacketCreator.getCharInfo(chr));
                chr.getMap().removePlayer(chr);
                chr.getMap().addPlayer(chr);
            }
            chr.dropMessage(5, "You have received a ring from " + player.getName() + ". Please log out and log back in again if it does not work correctly.");
            if (message != null)
                chr.dropMessage(5, "The message that came with the ring is: " + message);
            return ret;
        } catch (Exception e) {
            int[] ret = new int[2];
            ret[0] = -1;
            ret[1] = -1;
            return ret;
        }
    }

    public int getRingId() {
        return ringId;
    }

    public int getPartnerRingId() {
        return ringId2;
    }

    public int getPartnerChrId() {
        return partnerId;
    }

    public int getItemId() {
        return itemId;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public boolean isEquipped() {
        return equipped;
    }

    public void setEquipped(boolean equipped) {
        this.equipped = equipped;
    }

    @Override
    public int compareTo(MapleRing other) {
        if (ringId < other.getRingId())
            return -1;
        else if (ringId == other.getRingId())
            return 0;
        else
            return 1;
    }
}