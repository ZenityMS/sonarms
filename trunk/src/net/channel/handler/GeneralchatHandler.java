package net.channel.handler;

import client.command.CommandProcessor;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleClient;

public class GeneralchatHandler extends net.AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!c.getPlayer().getCanTalk()) {
            c.getPlayer().dropMessage(1, "Your chat has been disabled");
            return;
        }
        String text = slea.readMapleAsciiString();
        if (!CommandProcessor.processCommand(c, text))
            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getChatText(c.getPlayer().getId(), text, c.getPlayer().gmLevel() > 0 && c.getPlayer().getGMChat(), slea.readByte()));
    }
}