package net.channel.handler;

import client.MapleClient;
import client.SkillFactory;
import net.AbstractMaplePacketHandler;
import net.MaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.MaplePacketCreator;

public class CancelBuffHandler extends AbstractMaplePacketHandler implements MaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int sourceid = slea.readInt();
        switch (sourceid) {
            case 3121004:
            case 3221001:
            case 2121001:
            case 2221001:
            case 5221004:
            case 2321001:
                c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.skillCancel(c.getPlayer(), sourceid), false);
                break;
            default:
                c.getPlayer().cancelEffect(SkillFactory.getSkill(sourceid).getEffect(1), false, -1);
                break;
        }
    }
}