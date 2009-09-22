package net.handler;

import client.MapleClient;
import net.MaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public final class LoginRequiringNoOpHandler implements MaplePacketHandler {
    private static LoginRequiringNoOpHandler instance = new LoginRequiringNoOpHandler();

    public static LoginRequiringNoOpHandler getInstance() {
        return instance;
    }

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    }

    public boolean validateState(MapleClient c) {
        return c.isLoggedIn();
    }
}
