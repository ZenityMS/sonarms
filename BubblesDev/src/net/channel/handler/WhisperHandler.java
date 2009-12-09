/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package net.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.command.CommandProcessor;
import java.rmi.RemoteException;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public final class WhisperHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
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
                        if (c.getChannelServer().getWorldInterface().isConnected(recipient)) {
                            c.getChannelServer().getWorldInterface().whisper(c.getPlayer().getName(), recipient, c.getChannel(), text);
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
            if (victim != null && c.getPlayer().gmLevel() >= victim.gmLevel())
                c.getSession().write(MaplePacketCreator.getFindReplyWithMap(recipient, victim.getMap().getId()));
            else if (c.getPlayer().gmLevel() < victim.gmLevel()) {
                c.getSession().write(MaplePacketCreator.getFindReply(recipient, 0));
                c.getSession().write(MaplePacketCreator.enableActions());
            } else
                try {
                    int channel = c.getChannelServer().getWorldInterface().find(recipient);
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