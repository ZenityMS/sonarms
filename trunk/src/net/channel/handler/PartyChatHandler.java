package net.channel.handler;

import java.rmi.RemoteException;
import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public class PartyChatHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (!player.getCanTalk()) {
            player.dropMessage(1, "Your chat has been disabled.");
            return;
        }
        int type = slea.readByte(); // 0 for buddys, 1 for partys
        int numRecipients = slea.readByte();
        int recipients[] = new int[numRecipients];
        for (int i = 0; i < numRecipients; i++)
            recipients[i] = slea.readInt();
        String chattext = slea.readMapleAsciiString();
        try {
            if (type == 0)
                c.getChannelServer().getWorldInterface().buddyChat(recipients, player.getId(), player.getName(), chattext);
            else if (type == 1 && player.getParty() != null)
                c.getChannelServer().getWorldInterface().partyChat(player.getParty().getId(), chattext, player.getName());
            else if (type == 2 && player.getGuildId() > 0)
                c.getChannelServer().getWorldInterface().guildChat(player.getGuildId(), player.getName(), player.getId(), chattext);
        } catch (RemoteException e) {
            c.getChannelServer().reconnectWorld();
        }
    }
}