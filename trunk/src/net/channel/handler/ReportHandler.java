package net.channel.handler;

import java.sql.PreparedStatement;
import client.MapleClient;
import java.sql.ResultSet;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;

public class ReportHandler extends AbstractMaplePacketHandler {
    private final String[] reasons = {"HACKING", "BOTTING", "SCAMMING", "FAKE GM", "HARASSMENT", "ADVERTISING"};

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int reportedCharId = slea.readInt();
        byte reason = slea.readByte();
        String chatlog = "No chatlog";
        short clogLen = slea.readShort();
        if (clogLen > 0)
            chatlog = slea.readAsciiString(clogLen);
        if (addReportEntry(c.getPlayer().getId(), reportedCharId, reason, chatlog))
            c.getSession().write(MaplePacketCreator.reportReply((byte) 0));
        else
            c.getSession().write(MaplePacketCreator.reportReply((byte) 4));
        try {
            c.getChannelServer().getWorldInterface().broadcastGMMessage(null, MaplePacketCreator.serverNotice(6, getNameById(reportedCharId) + " FOR " + reasons[reason]).getBytes());
        } catch (Exception ex) {
        }
    }

    private String getNameById(int id) {
        try {
            PreparedStatement  ps = DatabaseConnection.getConnection().prepareStatement("SELECT name FROM characters WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return "";
            }
            String name = rs.getString("name");
            rs.close();
            ps.close();
            return name;
        } catch (Exception e) {
        }
        return "";
    }

    private boolean addReportEntry(int reporterId, int victimId, byte reason, String chatlog) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO reports (`reporttime`, `reporterid`, `victimid`, `reason`, `chatlog`, `status`) VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?, 'UNHANDLED')");
            ps.setInt(1, reporterId);
            ps.setInt(2, victimId);
            ps.setInt(3, reason);
            ps.setString(4, chatlog);
            ps.executeUpdate();
            ps.close();
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
}