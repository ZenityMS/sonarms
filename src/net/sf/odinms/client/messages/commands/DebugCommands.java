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

package net.sf.odinms.client.messages.commands;

import java.awt.Point;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.anticheat.CheatingOffense;
import net.sf.odinms.client.messages.Command;
import net.sf.odinms.client.messages.CommandDefinition;
import net.sf.odinms.client.messages.IllegalCommandSyntaxException;
import net.sf.odinms.client.messages.MessageCallback;
import net.sf.odinms.server.MaplePortal;
import net.sf.odinms.server.TimerManager;
import net.sf.odinms.server.life.MobSkill;
import net.sf.odinms.server.life.MobSkillFactory;
import net.sf.odinms.server.maps.MapleDoor;
import net.sf.odinms.server.quest.MapleQuest;
import net.sf.odinms.tools.HexTool;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.StringUtil;
import net.sf.odinms.tools.data.output.MaplePacketLittleEndianWriter;

public class DebugCommands implements Command {
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,
																					IllegalCommandSyntaxException {
		MapleCharacter player = c.getPlayer();
		if (splitted[0].equals("/resetquest")) {
			MapleQuest.getInstance(Integer.parseInt(splitted[1])).forfeit(c.getPlayer());
		} else if (splitted[0].equals("/nearestPortal")) {
			final MaplePortal portal = player.getMap().findClosestSpawnpoint(player.getPosition());
			mc.dropMessage(portal.getName() + " id: " + portal.getId() + " script: " + portal.getScriptName());
		} else if (splitted[0].equals("/spawndebug")) {
			c.getPlayer().getMap().spawnDebug(mc);
		} else if (splitted[0].equals("/door")) {
			Point doorPos = new Point(player.getPosition());
			doorPos.y -= 270;
			MapleDoor door = new MapleDoor(c.getPlayer(), doorPos);
			door.getTarget().addMapObject(door);
			// c.getSession().write(MaplePacketCreator.spawnDoor(/*c.getPlayer().getId()*/ 0x1E47, door.getPosition(),
			// false));
			/* c.getSession().write(MaplePacketCreator.saveSpawnPosition(door.getPosition())); */
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.write(HexTool.getByteArrayFromHexString("B9 00 00 47 1E 00 00 0A 04 76 FF"));
			c.getSession().write(mplew.getPacket());
			mplew = new MaplePacketLittleEndianWriter();
			mplew.write(HexTool.getByteArrayFromHexString("36 00 00 EF 1C 0D 4C 3E 1D 0D 0A 04 76 FF"));
			c.getSession().write(mplew.getPacket());
			c.getSession().write(MaplePacketCreator.enableActions());
			door = new MapleDoor(door);
			door.getTown().addMapObject(door);
		} else if (splitted[0].equals("/timerdebug")) {
			TimerManager.getInstance().dropDebugInfo(mc);
		} else if (splitted[0].equals("/threads")) {
			Thread[] threads = new Thread[Thread.activeCount()];
			Thread.enumerate(threads);
			String filter = "";
			if (splitted.length > 1) {
				filter = splitted[1];
			}
			for (int i = 0; i < threads.length; i++) {
				String tstring = threads[i].toString();
				if (tstring.toLowerCase().indexOf(filter.toLowerCase()) > -1) {
					mc.dropMessage(i + ": " + tstring);
				}
			}
		} else if (splitted[0].equals("/showtrace")) {
			if (splitted.length < 2) {
				throw new IllegalCommandSyntaxException(2);
			}
			Thread[] threads = new Thread[Thread.activeCount()];
			Thread.enumerate(threads);
			Thread t = threads[Integer.parseInt(splitted[1])];
			mc.dropMessage(t.toString() + ":");
			for (StackTraceElement elem : t.getStackTrace()) {
				mc.dropMessage(elem.toString());
			}
		} else if (splitted[0].equals("/fakerelog")) {
			c.getSession().write(MaplePacketCreator.getCharInfo(player));
			player.getMap().removePlayer(player);
			player.getMap().addPlayer(player);
			/*int i = 1;
			if (c.getPlayer().getNoPets() > 0) {
				for (MaplePet pet : c.getPlayer().getPets()) {
					List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
					stats.add(new Pair<MapleStat, Integer>(MapleStat.PET, Integer.valueOf(pet.getUniqueId())));
					// Write the stat update to the player...
					c.getSession().write(MaplePacketCreator.updatePlayerStats(stats, false, true, i));
					c.getSession().write(MaplePacketCreator.enableActions());
					i++;
				}
			}*/
		} else if (splitted[0].equals("/toggleoffense")) {
			try {
				CheatingOffense co = CheatingOffense.valueOf(splitted[1]);
				co.setEnabled(!co.isEnabled());
			} catch (IllegalArgumentException iae) {
				mc.dropMessage("Offense " + splitted[1] + " not found");
			}
		} else if (splitted[0].equals("/tdrops")) {
			player.getMap().toggleDrops();
		} else if (splitted[0].equals("/givebuff")) {
			long mask = 0;
			mask |= Long.decode(splitted[1]);
			c.getSession().write(MaplePacketCreator.giveBuffTest(1000, mask));
		} else if (splitted[0].equals("/givemonsbuff")) {
			int mask = 0;
			mask |= Integer.decode(splitted[1]);
			MobSkill skill = MobSkillFactory.getMobSkill(128, 1);
			c.getSession().write(MaplePacketCreator.applyMonsterStatusTest(Integer.valueOf(splitted[2]), mask, 0, skill, Integer.valueOf(splitted[3])));
		} else if (splitted[0].equals("/givemonstatus")) {
			int mask = 0;
			mask |= Integer.decode(splitted[1]);
			c.getSession().write(MaplePacketCreator.applyMonsterStatusTest2(Integer.valueOf(splitted[2]), mask, 1000, Integer.valueOf(splitted[3])));
		} else if (splitted[0].equals("/packet")) {
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
	        mplew.write(HexTool.getByteArrayFromHexString(StringUtil.joinStringFrom(splitted, 1)));
			c.getSession().write(mplew.getPacket());
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("/resetquest", "", "", 4),
			new CommandDefinition("/nearestPortal", "", "", 4),
			new CommandDefinition("/spawndebug", "", "", 4),
			new CommandDefinition("/timerdebug", "", "", 4),
			new CommandDefinition("/threads", "", "", 4),
			new CommandDefinition("/showtrace", "", "", 4),
			new CommandDefinition("/toggleoffense", "", "", 4),
			new CommandDefinition("/fakerelog", "", "", 4),
			new CommandDefinition("/tdrops", "", "", 4),
			new CommandDefinition("/givebuff", "", "", 4),
			new CommandDefinition("/givemonsbuff", "", "", 4),
			new CommandDefinition("/givemonstatus", "", "", 4),
			new CommandDefinition("/packet", "", "", 4),
		};
	}

}
