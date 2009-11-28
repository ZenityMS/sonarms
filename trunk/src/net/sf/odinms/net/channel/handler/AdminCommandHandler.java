/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package net.sf.odinms.net.channel.handler;

import java.util.Calendar;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleCharacterUtil;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.net.MaplePacketHandler;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import net.sf.odinms.tools.MaplePacketCreator;

public class AdminCommandHandler extends AbstractMaplePacketHandler implements MaplePacketHandler {
	private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminCommandHandler.class);
	
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		if (!c.getPlayer().isGM())
			return; //hacks, or the account is just set as a gm.
		
		byte mode = slea.readByte();
		
		String victim;
		String reason;
		MapleCharacter target;
		switch (mode)
		{
			case 0x00: 
				slea.readShort();
				break;
				
			case 0x03: // Ban
				victim = slea.readMapleAsciiString();
				reason = c.getPlayer().getName() + " used /ban to ban";
				
				target = c.getChannelServer().getPlayerStorage().getCharacterByName(victim);
				if (target != null) {
					String readableTargetName = MapleCharacterUtil.makeMapleReadable(target.getName());
					String ip = target.getClient().getSession().getRemoteAddress().toString().split(":")[0];
					reason += readableTargetName + " (IP: " + ip + ")";
					target.ban(reason);
					c.getSession().write(MaplePacketCreator.getGMEffect(4, (byte)0));
				} else {
					if (MapleCharacter.ban(victim, reason, false)) {
						c.getSession().write(MaplePacketCreator.getGMEffect(4, (byte)0));
					} else {
						c.getSession().write(MaplePacketCreator.getGMEffect(6, (byte)1));
					}
				}
				break;
				
			case 0x04: // Block
				/*7E 00
				04 - Ban
				07 00 - To ban
				54 54 54 54 54 54 54 - Name
				01 - Type
				FF FF FF FF - Length (-1 == perma)
				01 00 - Description length
				54 - Description*/
				victim = slea.readMapleAsciiString();
				byte type = slea.readByte();
				int duration = slea.readInt();
				String description = slea.readMapleAsciiString();
				
				reason = c.getPlayer().getName() + " used /ban to ban";
				
				target = c.getChannelServer().getPlayerStorage().getCharacterByName(victim);
				if (target != null) {
					String readableTargetName = MapleCharacterUtil.makeMapleReadable(target.getName());
					String ip = target.getClient().getSession().getRemoteAddress().toString().split(":")[0];
					reason += readableTargetName + " (IP: " + ip + ")";
					Calendar dur;
					if (duration == -1)
					{
						
					}
					//target.tempban(reason, duration, type);
					c.getSession().write(MaplePacketCreator.getGMEffect(4, (byte)0));
				} else {
					if (MapleCharacter.ban(victim, reason, false)) {
						c.getSession().write(MaplePacketCreator.getGMEffect(4, (byte)0));
					} else {
						c.getSession().write(MaplePacketCreator.getGMEffect(6, (byte)1));
					}
				}
				break;
				
			case 0x10: // Hide
				c.getPlayer().changeHide(slea.readByte() == 1);
				break;
				
			case 0x11: // /u ?
				slea.readByte();
				break;
				
			case 0x1D: // Warn
				victim = slea.readMapleAsciiString();
				String message = slea.readMapleAsciiString();
				
				target = c.getChannelServer().getPlayerStorage().getCharacterByName(victim);
				if (target != null) {
					target.getClient().getSession().write(MaplePacketCreator.serverNotice(1, message));
					c.getSession().write(MaplePacketCreator.getGMEffect(29, (byte)1));
				} else {
					c.getSession().write(MaplePacketCreator.getGMEffect(29, (byte)0));
				}
				break;
				
			default:
				log.info("New GM packet encountered (MODE : " + mode + ": " + slea.toString());
				break;
		}
	}
}
