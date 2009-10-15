package net.channel.handler;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Xterminator
 */
public class InnerPortalHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        /*
        // TODO Need to code this check somehow
        slea.readByte();
        String portal = slea.readMapleAsciiString();
        int toX = slea.readShort();
        int toY = slea.readShort();
        int X = slea.readShort();
        int Y = slea.readShort();
        log.info("[Hacks] Player {} is trying to jump to a different map portal rather than the correct one");
         */
    }
}