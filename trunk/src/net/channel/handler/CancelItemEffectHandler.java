package net.channel.handler;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import server.MapleItemInformationProvider;
import tools.data.input.SeekableLittleEndianAccessor;

public class CancelItemEffectHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int sourceid = slea.readInt();
        c.getPlayer().cancelEffect(MapleItemInformationProvider.getInstance().getItemEffect(-sourceid), false, -1);
    }
}
