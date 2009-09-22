package net.channel.handler;

import java.sql.SQLException;

import client.MapleClient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Penguins (Acrylic)
 */
public class CouponCodeHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.skip(2);
        String code = slea.readMapleAsciiString();
        boolean validcode = false;
        int type = -1;
        int item = -1;
        try {
            validcode = getNXCodeValid(code.toUpperCase(), validcode);
        } catch (SQLException e) {
        }
        if (validcode) {
            try {
                type = getNXCodeType(code);
                item = getNXCodeItem(code);
            } catch (SQLException e) {
            }
            if (type != 5)
                try {
                    Connection con = DatabaseConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement("UPDATE nxcode SET `valid` = 0 WHERE code = " + code);
                    ps.executeUpdate();
                    ps = con.prepareStatement("UPDATE nxcode SET `user` = ? WHERE code = ?");
                    ps.setString(1, c.getPlayer().getName());
                    ps.setString(2, code);
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                }
            switch (type) {
                case 0:
                case 1:
                case 2:
                    c.getPlayer().modifyCSPoints(type, item);
                    break;
                case 3:
                    c.getPlayer().modifyCSPoints(0, item);
                    c.getPlayer().modifyCSPoints(2, (item / 5000));
                    break;
                case 4:
                    MapleInventoryManipulator.addById(c, item, (short) 1, null, -1);
                    c.getSession().write(MaplePacketCreator.showCouponRedeemedItem(item));
                    break;
                case 5:
                    c.getPlayer().modifyCSPoints(0, item);
                    break;
            }
            c.getSession().write(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
        } else
            c.getSession().write(MaplePacketCreator.wrongCouponCode());
        c.getSession().write(MaplePacketCreator.enableCSUse0());
        c.getSession().write(MaplePacketCreator.enableCSUse1());
        c.getSession().write(MaplePacketCreator.enableCSUse2());
    }

    private int getNXCodeItem(String code) throws SQLException {
        int item = -1;
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `item` FROM nxcode WHERE code = ?");
        ps.setString(1, code);
        ResultSet rs = ps.executeQuery();
        while (rs.next())
            item = rs.getInt("item");
        rs.close();
        ps.close();
        return item;
    }

    private int getNXCodeType(String code) throws SQLException {
        int type = -1;
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `type` FROM nxcode WHERE code = ?");
        ps.setString(1, code);
        ResultSet rs = ps.executeQuery();
        while (rs.next())
            type = rs.getInt("type");
        rs.close();
        ps.close();
        return type;
    }

    private boolean getNXCodeValid(String code, boolean validcode) throws SQLException {
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `valid` FROM nxcode WHERE code = ?");
        ps.setString(1, code);
        ResultSet rs = ps.executeQuery();
        while (rs.next())
            validcode = rs.getInt("valid") == 0 ? false : true;
        rs.close();
        ps.close();
        return validcode;
    }
}