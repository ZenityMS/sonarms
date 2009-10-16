package net.channel.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import client.MapleClient;
import client.MapleCharacter;
import tools.DatabaseConnection;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import scripting.npc.NPCScriptManager;

/**
 * @author Jvlaple
 */
public class RingActionHandler extends AbstractMaplePacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte mode = slea.readByte();
        MapleCharacter player = c.getPlayer();
        switch (mode) {
            case 0x00: //Send
                String partnerName = slea.readMapleAsciiString();
                MapleCharacter partner = c.getChannelServer().getPlayerStorage().getCharacterByName(partnerName);
                if (partnerName.equalsIgnoreCase(player.getName())) {
                    c.getSession().write(tools.MaplePacketCreator.serverNotice(1, "You cannot put your own name in it."));
                    return;
                } else if (partner == null) {
                    c.getSession().write(tools.MaplePacketCreator.serverNotice(1, partnerName + " was not found on this channel. If you are both logged in, please make sure you are in the same channel."));
                    return;
                } else if (partner.getGender() == player.getGender()) {
                    c.getSession().write(tools.MaplePacketCreator.serverNotice(1, "Your partner is the same gender as you."));
                    return;
                } else if (player.isMarried() && partner.isMarried())
                    NPCScriptManager.getInstance().start(partner.getClient(), 9201002, "marriagequestion", player);
                break;
            case 0x01: //Cancel send
                c.getSession().write(tools.MaplePacketCreator.serverNotice(1, "You've cancelled the request."));
                break;
            case 0x03: //Drop Ring
                if (player.getPartner() != null) {
                    try {
                        Connection con = DatabaseConnection.getConnection();
                        int pid = 0;
                        if (player.getGender() == 0)
                            pid = player.getId();
                        else
                            pid = player.getPartner().getId();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM engagements WHERE husbandid = ?");
                        ps.setInt(1, pid);
                        ps.executeUpdate();
                        ps = con.prepareStatement("UPDATE characters SET marriagequest = 0 WHERE id = ?, and WHERE id = ?");
                        ps.setInt(1, player.getId());
                        ps.setInt(2, player.getPartner().getId());
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception ex) {
                    }
                    c.getSession().write(tools.MaplePacketCreator.serverNotice(1, "Your engagement has been broken up."));
                    break;
                }
            default:
                break;
        }
    }
}