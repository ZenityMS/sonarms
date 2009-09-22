package net.channel.handler;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public class HealOvertimeHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readByte();
        slea.readShort();
        slea.readByte();
        int healHP = slea.readShort();
        if (healHP != 0) {
            if (healHP > 140)
                c.disconnect();
            c.getPlayer().addHP(healHP);
            c.getPlayer().checkBerserk();
        }
        int healMP = slea.readShort();
        if (healMP != 0) {
            if (healMP > 1000) //1000 is definitely impossible
                c.disconnect();
            c.getPlayer().addMP(healMP);
        }
    }
}
