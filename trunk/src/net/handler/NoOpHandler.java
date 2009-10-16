package net.handler;

import client.MapleClient;
import net.MaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public final class NoOpHandler implements MaplePacketHandler {
    private static NoOpHandler instance = new NoOpHandler();

    public static NoOpHandler getInstance() {
        return instance;
    }

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    }

    public boolean validateState(MapleClient c) {
        return true;
    }
}
