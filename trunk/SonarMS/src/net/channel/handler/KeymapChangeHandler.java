package net.channel.handler;

import client.MapleClient;
import client.MapleKeyBinding;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public class KeymapChangeHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (slea.available() != 8) {
            slea.readInt();
            int numChanges = slea.readInt();
            for (int i = 0; i < numChanges; i++) {
                int key = slea.readInt();
                int type = slea.readByte();
                int action = slea.readInt();
                c.getPlayer().changeKeybinding(key, new MapleKeyBinding(type, action));
            }
        }
    }
}