package net.channel.handler;

import java.sql.PreparedStatement;
import client.MapleClient;
import tools.DatabaseConnection;
import tools.data.input.SeekableLittleEndianAccessor;
import net.AbstractMaplePacketHandler;

public class NoteActionHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int action = slea.readByte();
        if (action == 1) {
            int num = slea.readByte();
            slea.readByte();
            slea.readByte();
            for (int i = 0; i < num; i++) {
                int id = slea.readInt();
                slea.readByte();
                try {
                    PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM notes WHERE `id`=?");
                    ps.setInt(1, id);
                    ps.executeUpdate();
                    ps.close();
                } catch (Exception e) {
                }
            }
        }
    }
}