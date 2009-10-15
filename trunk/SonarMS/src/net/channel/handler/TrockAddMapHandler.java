package net.channel.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import client.MapleClient;
import tools.DatabaseConnection;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public class TrockAddMapHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        Connection con = DatabaseConnection.getConnection();
        byte addrem;
        addrem = slea.readByte();
        byte rocktype = slea.readByte();
        if (addrem == 0x00) {
            int mapId = slea.readInt();
            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM trocklocations WHERE characterid = ? AND mapid = ?");
                ps.setInt(1, c.getPlayer().getId());
                ps.setInt(2, mapId);
                ps.executeUpdate();
                ps.close();
            } catch (Exception e) {
            }
        } else if (addrem == 0x01) {
            int mapid = c.getPlayer().getMapId();
            if (!((mapid >= 240050000 && mapid <= 240060200) || mapid < 100000000 || (mapid >= 280010010 && mapid <= 280030000) || (mapid >= 670000100 && mapid <= 670011000) || mapid >= 809020000 || (mapid >= 101000100 && mapid <= 101000104) || mapid == 101000301 || (mapid >= 105040310 && mapid <= 105040316) || (mapid >= 108000100 && mapid <= 109080003) || (mapid >= 190000000 && mapid <= 197010000) || (mapid >= 200090000 && mapid <= 209080000) || mapid == 240000110 || mapid == 240000111 || mapid == 260000110)) //disallowed maps
                try {
                    PreparedStatement ps = con.prepareStatement("INSERT into trocklocations (characterid, mapid) VALUES (?, ?)");
                    ps.setInt(1, c.getPlayer().getId());
                    ps.setInt(2, c.getPlayer().getMapId());
                    ps.executeUpdate();
                    ps.close();
                } catch (Exception e) {
                }
            else
                c.getSession().write(MaplePacketCreator.serverNotice(5, "You may not save this map."));
        }
        c.getSession().write(MaplePacketCreator.TrockRefreshMapList(c.getPlayer().getId(), rocktype));
    }
}