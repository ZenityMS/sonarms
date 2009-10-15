package net;

import client.MapleClient;

public abstract class AbstractMaplePacketHandler implements MaplePacketHandler {
    public boolean validateState(MapleClient c) {
        return c.isLoggedIn();
    }
}