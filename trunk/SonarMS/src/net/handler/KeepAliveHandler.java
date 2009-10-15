package net.handler;

import client.MapleClient;
import net.MaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public class KeepAliveHandler implements MaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.pongReceived();
    }

    public boolean validateState(MapleClient c) {
        return true;
    }
}
