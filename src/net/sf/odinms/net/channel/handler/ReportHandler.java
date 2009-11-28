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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class ReportHandler extends AbstractMaplePacketHandler {
	private static final Logger log = LoggerFactory.getLogger(PlayerLoggedinHandler.class);
	
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		//[69 00] 	- Header
		//[00]		- Type of Report
		//[07 00]	- Victim length
		//[54 72 61 69 74 6F 72] - Victim
		//[09]		- Type of report
		//[06 00]	- Description length
		//[55 52 20 42 41 44] - Description
		//i....Traitor...UR BAD
		
		byte report = slea.readByte();
		String victim = slea.readMapleAsciiString();
		byte report2 = slea.readByte();
		String description = slea.readMapleAsciiString();
		log.info("Report: " + report + "\nReport2: " + report2 + "\nVictim: " + victim + "\nDescription: " + description);
		
		c.getSession().write(MaplePacketCreator.reportResponse((byte) 0x02, 100));
	}
}
