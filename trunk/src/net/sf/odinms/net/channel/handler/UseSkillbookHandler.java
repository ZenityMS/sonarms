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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.odinms.client.IItem;
import net.sf.odinms.client.ISkill;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public class UseSkillbookHandler extends AbstractMaplePacketHandler {
	private static Logger log = LoggerFactory.getLogger(UseItemHandler.class);
	
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		if (!c.getPlayer().isAlive()) {
			c.getSession().write(MaplePacketCreator.enableActions());
			return;
		}
		
		slea.skip(4); //random crap, who CARES
		byte slot = (byte)slea.readShort();
		int itemId = slea.readInt();
		IItem item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
		if (item != null && item.getQuantity() == 1) {
			if (item.getItemId() != itemId) {
				log.warn("[h4x] Player {} is using wrong skillbook id with skillbook", c.getPlayer().getName());
				return;
			}
			
			MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
			Map<String, Integer> skilldata = ii.getSkillStats(item.getItemId(), c.getPlayer().getJob().getId());
			
			// Initialize defaults
			boolean canuse = false;
			boolean success = false;
			int skill = 0;
			int maxlevel = 0;
			
			if(skilldata == null)
				return;
			
			if (skilldata.get("skillid") != 0 && c.getPlayer().getMasterLevel(SkillFactory.getSkill(skilldata.get("skillid"))) >= skilldata.get("reqSkillLevel") || skilldata.get("reqSkillLevel") == 0) {
				canuse = true;
				if(Math.ceil(Math.random() * 100.0) <= skilldata.get("success") && skilldata.get("success") != 0) {
					success = true;
					ISkill skill2 = SkillFactory.getSkill(skilldata.get("skillid"));
					int curlevel = c.getPlayer().getSkillLevel(skill2);
					c.getPlayer().changeSkillLevel(skill2, curlevel, skilldata.get("masterLevel"));
				} else {
					success = false;
				}
			}
			
			if(canuse)
				MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short)1, false);
			
			c.getSession().write(MaplePacketCreator.skillBookSuccess(c.getPlayer(), skill, maxlevel, canuse, success));
		} else {
			log.warn("[h4x] Player {} is using skillbook without having one", c.getPlayer().getName());
		}
		c.getSession().write(MaplePacketCreator.enableActions()); //until I can get the proper packets.
	}
}