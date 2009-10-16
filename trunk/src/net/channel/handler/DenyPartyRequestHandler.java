package net.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class DenyPartyRequestHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readByte();
        String from = slea.readMapleAsciiString();
        slea.readMapleAsciiString(); //wtf?
        MapleCharacter cfrom = c.getChannelServer().getPlayerStorage().getCharacterByName(from);
        if (cfrom != null)
            cfrom.getClient().getSession().write(MaplePacketCreator.partyStatusMessage(23, c.getPlayer().getName()));
    }
}
