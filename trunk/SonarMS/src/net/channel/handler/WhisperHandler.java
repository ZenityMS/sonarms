package net.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.command.CommandProcessor;
import java.rmi.RemoteException;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public class WhisperHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte mode = slea.readByte();
        if (mode == 6) { // whisper
            String recipient = slea.readMapleAsciiString();
            String text = slea.readMapleAsciiString();
            if (!CommandProcessor.processCommand(c, text)) {
                MapleCharacter player = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
                if (player != null) {
                    player.getClient().getSession().write(MaplePacketCreator.getWhisper(c.getPlayer().getName(), c.getChannel(), text));
                    c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 1));
                } else // not found
                    try {
                        if (ChannelServer.getInstance(c.getChannel()).getWorldInterface().isConnected(recipient)) {
                            ChannelServer.getInstance(c.getChannel()).getWorldInterface().whisper(
                                    c.getPlayer().getName(), recipient, c.getChannel(), text);
                            c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 1));
                        } else
                            c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
                    } catch (RemoteException e) {
                        c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
                        c.getChannelServer().reconnectWorld();
                    }
            }
        } else if (mode == 5) { // - /find
            String recipient = slea.readMapleAsciiString();
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
            if (victim != null && (victim.gmLevel() < 1 || c.getPlayer().gmLevel() > 0))
                c.getSession().write(MaplePacketCreator.getFindReplyWithMap(recipient, victim.getMap().getId()));
            else
                try {
                    int channel = ChannelServer.getInstance(c.getChannel()).getWorldInterface().find(recipient);
                    if (channel > -1)
                        c.getSession().write(MaplePacketCreator.getFindReply(recipient, channel));
                    else
                        c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
                } catch (RemoteException e) {
                    c.getChannelServer().reconnectWorld();
                }
        }
    }
}
