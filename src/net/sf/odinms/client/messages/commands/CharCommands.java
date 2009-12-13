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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import static net.sf.odinms.client.messages.CommandProcessor.getOptionalIntArg;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.ISkill;
import net.sf.odinms.client.Item;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MapleJob;
import net.sf.odinms.client.MaplePet;
import net.sf.odinms.client.MapleStat;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.client.messages.Command;
import net.sf.odinms.client.messages.CommandDefinition;
import net.sf.odinms.client.messages.IllegalCommandSyntaxException;
import net.sf.odinms.client.messages.MessageCallback;
import net.sf.odinms.client.messages.ServernoticeMapleClientMessageCallback;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.provider.MapleData;
import net.sf.odinms.provider.MapleDataProviderFactory;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.MapleShop;
import net.sf.odinms.server.MapleShopFactory;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.Pair;

public class CharCommands implements Command {

	private MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

	@SuppressWarnings("static-access")
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception, IllegalCommandSyntaxException {
		MapleCharacter player = c.getPlayer();
                ChannelServer cserv = c.getChannelServer();
		if (splitted[0].equals("!lowhp")) {
			player.setHp(1);
			player.setMp(500);
			player.updateSingleStat(MapleStat.HP, 1);
			player.updateSingleStat(MapleStat.MP, 500);
		} else if (splitted[0].equals("!fullhp")) {
			player.addMPHP(player.getMaxHp() - player.getHp(), player.getMaxMp() - player.getMp());
                } else if (splitted[0].equals("!kill")) {
                        cserv.getPlayerStorage().getCharacterByName(splitted[1]).setHpMp(0);
                } else if (splitted[0].equals("!skill")) {
			int skill = Integer.parseInt(splitted[1]);
			int level = getOptionalIntArg(splitted, 2, 1);
			int masterlevel = getOptionalIntArg(splitted, 3, 1);
			c.getPlayer().changeSkillLevel(SkillFactory.getSkill(skill), level, masterlevel);
                } else if (splitted[0].equals("!maxall")) {
                        player.setLevel(200);
                        player.setStr(32767);
                        player.setDex(32767);
                        player.setInt(32767);
                        player.setLuk(32767);
                        player.setFame(13337);
                        player.setMaxHp(30000);
                        player.setMaxMp(30000);
                        player.updateSingleStat(MapleStat.LEVEL, 255);
                        player.updateSingleStat(MapleStat.STR, 32767);
                        player.updateSingleStat(MapleStat.DEX, 32767);
                        player.updateSingleStat(MapleStat.INT, 32767);
                        player.updateSingleStat(MapleStat.LUK, 32767);
                        player.updateSingleStat(MapleStat.FAME, 13337);
                        player.updateSingleStat(MapleStat.MAXHP, 30000);
                        player.updateSingleStat(MapleStat.MAXMP, 30000);
                } else if (splitted[0].equals("!maxskills")) {
            for (MapleData skill_ : MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren())
                try {
                    ISkill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
                    player.changeSkillLevel(skill, skill.getMaxLevel(), skill.getMaxLevel());
                } catch (NumberFormatException nfe) {
                    break;
                } catch (NullPointerException npe) {
                    continue;
                }
		} else if (splitted[0].equals("!ap")) {
			player.setRemainingAp(getOptionalIntArg(splitted, 1, 1));
			player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
		} else if (splitted[0].equals("!sp")) {
			player.setRemainingSp(getOptionalIntArg(splitted, 1, 1));
			player.updateSingleStat(MapleStat.AVAILABLESP, player.getRemainingSp());
		} else if (splitted[0].equals("!job")) {
			c.getPlayer().changeJob(MapleJob.getById(Integer.parseInt(splitted[1])));
                } else if (splitted[0].equals("!jobperson")) {
                        cserv.getPlayerStorage().getCharacterByName(splitted[1]).changeJob(MapleJob.getById(Integer.parseInt(splitted[2])));
		} else if (splitted[0].equals("!whereami")) {
			new ServernoticeMapleClientMessageCallback(c).dropMessage("You are on map " +
					c.getPlayer().getMap().getId());
		} else if (splitted[0].equals("!shop")) {
			MapleShopFactory sfact = MapleShopFactory.getInstance();
			MapleShop shop = sfact.getShop(getOptionalIntArg(splitted, 1, 1));
			shop.sendShop(c);
                } else if (splitted[0].equals("!gmshop")) {
                        MapleShopFactory.getInstance().getShop(1337).sendShop(c);
                } else if (splitted[0].equals("!mesos")) {
                        player.gainMeso(Integer.parseInt(splitted[1]), true);
                } else if (splitted[0].equals("!mesoperson")) {
                        cserv.getPlayerStorage().getCharacterByName(splitted[1]).gainMeso(Integer.parseInt(splitted[2]), true);
		} else if (splitted[0].equals("!levelup")) {
			c.getPlayer().levelUp();
			int newexp = c.getPlayer().getExp();
			if (newexp < 0) {
				c.getPlayer().gainExp(-newexp, false, false);
			}
		} else if (splitted[0].equals("!item")) {
			short quantity = (short) getOptionalIntArg(splitted, 2, 1);
			int itemId = Integer.parseInt(splitted[1]);
			if (ii.getSlotMax(itemId) > 0) {
				if (itemId >= 5000000 && itemId <= 5000100) {
					if (quantity > 1) {
						quantity = 1;
					}
					int petId = MaplePet.createPet(itemId);
					MapleInventoryManipulator.addById(c, itemId, quantity, c.getPlayer().getName() + "used !item with quantity " + quantity, player.getName(), petId);
					return;
				}
				MapleInventoryManipulator.addById(c, itemId, quantity, c.getPlayer().getName() + "used !item with quantity " + quantity, player.getName());
			} else {
				mc.dropMessage("Item " + itemId + " not found.");
			}
		} else if (splitted[0].equals("!drop")) {
			int itemId = Integer.parseInt(splitted[1]);
			short quantity = (short) getOptionalIntArg(splitted, 2, 1);
			IItem toDrop;
			if (ii.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
				toDrop = ii.getEquipById(itemId);
			} else {
				toDrop = new Item(itemId, (byte) 0, (short) quantity);
			}
			StringBuilder logMsg = new StringBuilder("Created by ");
			logMsg.append(c.getPlayer().getName());
			logMsg.append(" using !drop. Quantity: ");
			logMsg.append(quantity);
			toDrop.log(logMsg.toString(), false);
			toDrop.setOwner(player.getName());
			c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), toDrop, c.getPlayer().getPosition(), true, true);
		} else if (splitted[0].equals("!level")) {
			int quantity = Integer.parseInt(splitted[1]);
			c.getPlayer().setLevel(quantity);
			c.getPlayer().levelUp();
			int newexp = c.getPlayer().getExp();
			if (newexp < 0) {
				c.getPlayer().gainExp(-newexp, false, false);
			}
                } else if (splitted[0].equals("!levelperson")) {
                        MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
                        victim.setLevel(Integer.parseInt(splitted[2]));
                        victim.gainExp(-victim.getExp(), false, false);
                        victim.updateSingleStat(MapleStat.LEVEL, victim.getLevel());
                } else if (splitted[0].equals("!goto")) {
                    HashMap<String, Integer> maps = new HashMap<String, Integer>();
                    maps.put("gmmap", 180000000);
                    maps.put("henesys", 100000000);
                    maps.put("ellinia", 101000000);
                    maps.put("perion", 102000000);
                    maps.put("kerning", 103000000);
                    maps.put("lith", 104000000);
                    maps.put("sleepywood", 105040300);
                    maps.put("florina", 110000000);
                    maps.put("orbis", 200000000);
                    maps.put("happy", 209000000);
                    maps.put("elnath", 211000000);
                    maps.put("ludi", 220000000);
                    maps.put("omega", 221000000);
                    maps.put("korean", 222000000);
                    maps.put("aqua", 230000000);
                    maps.put("leafre", 240000000);
                    maps.put("mulung", 250000000);
                    maps.put("herb", 251000000);
                    maps.put("nlc", 600000000);
                    maps.put("shrine", 800000000);
                    maps.put("showa", 801000000);
                    maps.put("fm", 910000000);
                    if (maps.containsKey(splitted[1])) {
                        player.changeMap(cserv.getMapFactory().getMap(maps.get(splitted[1])), cserv.getMapFactory().getMap(maps.get(splitted[1])).getPortal(0));
                    } else {
                       player.dropMessage("No map enetered. Enter !goto  <Location>");
                    }
		} else if (splitted[0].equals("!online")) {
			mc.dropMessage("Characters connected to channel " + c.getChannel() + ":");
			Collection<MapleCharacter> chrs = c.getChannelServer().getInstance(c.getChannel()).getPlayerStorage().getAllCharacters();
			for (MapleCharacter chr : chrs) {
				mc.dropMessage(chr.getName() + " at map ID: " + chr.getMapId());
			}
			mc.dropMessage("Total characters on channel " + c.getChannel() + ": " + chrs.size());
		} else if (splitted[0].equals("!statreset")) {
			int str = c.getPlayer().getStr();
			int dex = c.getPlayer().getDex();
			int int_ = c.getPlayer().getInt();
			int luk = c.getPlayer().getLuk();
			int newap = c.getPlayer().getRemainingAp() + (str - 4) + (dex - 4) + (int_ - 4) + (luk - 4);
			c.getPlayer().setStr(4);
			c.getPlayer().setDex(4);
			c.getPlayer().setInt(4);
			c.getPlayer().setLuk(4);
			c.getPlayer().setRemainingAp(newap);
			List<Pair<MapleStat, Integer>> stats = new ArrayList<Pair<MapleStat, Integer>>();
			stats.add(new Pair<MapleStat, Integer>(MapleStat.STR, Integer.valueOf(4)));
			stats.add(new Pair<MapleStat, Integer>(MapleStat.DEX, Integer.valueOf(4)));
			stats.add(new Pair<MapleStat, Integer>(MapleStat.INT, Integer.valueOf(4)));
			stats.add(new Pair<MapleStat, Integer>(MapleStat.LUK, Integer.valueOf(4)));
			stats.add(new Pair<MapleStat, Integer>(MapleStat.AVAILABLEAP, Integer.valueOf(newap)));
			c.getSession().write(MaplePacketCreator.updatePlayerStats(stats));
		} else if (splitted[0].equals("!gmpacket")) {
			int type = Integer.parseInt(splitted[1]);
			int mode = Integer.parseInt(splitted[2]);
			c.getSession().write(MaplePacketCreator.sendGMOperation(type, mode));
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[]{
					new CommandDefinition("lowhp", "", "", 100),
					new CommandDefinition("fullhp", "", "", 100),
                                        new CommandDefinition("kill", "", "", 100),
					new CommandDefinition("skill", "", "", 100),
                                        new CommandDefinition("maxall", "", "", 100),
                                        new CommandDefinition("maxskills", "", "", 100),
					new CommandDefinition("ap", "", "", 100),
					new CommandDefinition("sp", "", "", 100),
					new CommandDefinition("job", "", "", 100),
                                        new CommandDefinition("jobperson", "", "", 100),
					new CommandDefinition("whereami", "", "", 100),
					new CommandDefinition("shop", "", "", 100),
                                        new CommandDefinition("gmshop", "", "", 100),
                                        new CommandDefinition("mesos", "", "", 100),
                                        new CommandDefinition("mesoperson", "", "", 100),
					new CommandDefinition("levelup", "", "", 100),
					new CommandDefinition("item", "", "", 100),
					new CommandDefinition("drop", "", "", 100),
					new CommandDefinition("level", "", "", 100),
                                        new CommandDefinition("levelperson", "", "", 100),
                                        new CommandDefinition("goto", "", "", 100),
					new CommandDefinition("online", "", "", 100),
					new CommandDefinition("ring", "", "", 100),
					new CommandDefinition("statreset", "", "", 100),
					new CommandDefinition("gmpacket", "", "", 100)
				};
	}
}
