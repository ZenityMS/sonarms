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



import java.io.*;
import java.util.*;
import static net.sf.odinms.client.messages.CommandProcessor.getOptionalIntArg;
import net.sf.odinms.client.GameConstants;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.ISkill;
import net.sf.odinms.client.Item;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MapleJob;
import net.sf.odinms.client.MaplePet;
import net.sf.odinms.client.MapleRing;
import net.sf.odinms.client.MapleStat;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.client.messages.*;
import net.sf.odinms.net.channel.*;
import net.sf.odinms.provider.MapleData;
import net.sf.odinms.provider.MapleDataProviderFactory;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.server.MapleShop;
import net.sf.odinms.server.MapleShopFactory;
import net.sf.odinms.tools.MaplePacketCreator;

public class CharCommands implements Command {
    MapleClient c;
    char heading;
    ChannelServer cserv = c.getChannelServer();
	@SuppressWarnings("static-access")
	@Override

	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,
																					IllegalCommandSyntaxException {
		MapleCharacter player = c.getPlayer();
		if (splitted[0].equals("/lowhp")) {
			player.setHp(1);
			player.setMp(500);
			player.updateSingleStat(MapleStat.HP, 1);
			player.updateSingleStat(MapleStat.MP, 500);
		} else if (splitted[0].equals("/fullhp")) {
			player.setHp(player.getMaxHp());
			player.updateSingleStat(MapleStat.HP, player.getMaxHp());
		} else if (splitted[0].equals("/heal")) {
			player.setHp(player.getMaxHp());
			player.updateSingleStat(MapleStat.HP, player.getMaxHp());
			player.setMp(player.getMaxMp());
			player.updateSingleStat(MapleStat.MP, player.getMaxMp());
		} else if (splitted[0].equals("/skill")) {
			int skill = Integer.parseInt(splitted[1]);
			int level = getOptionalIntArg(splitted, 2, 1);
			int masterlevel = getOptionalIntArg(splitted, 3, 1);
			c.getPlayer().changeSkillLevel(SkillFactory.getSkill(skill), level, masterlevel);
		} else if (splitted[0].equals("/sp")) {
			player.setRemainingSp(getOptionalIntArg(splitted, 1, 1));
			player.updateSingleStat(MapleStat.AVAILABLESP, player.getRemainingSp());
		} else if (splitted[0].equals("/ap")) {
			player.setRemainingAp(getOptionalIntArg(splitted, 1, 1));
			player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
		} else if (splitted[0].equals("/giftnx")) {
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).modifyCSPoints(1, Integer.parseInt(splitted[2]));
            player.dropMessage("Done");
		} else if (splitted[0].equals("/mesos"))
            player.gainMeso(Integer.parseInt(splitted[1]), true);
		 else if (splitted[0].equals("/maxskills"))
            for (MapleData skill_ : MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren())
                try {
                    ISkill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
                    if (skill.getId() < 9000000 || skill.getId() > 9120000)
                        player.changeSkillLevel(skill, skill.getMaxLevel(), skill.getMaxLevel());
                } catch (NumberFormatException nfe) {
                    break;
                } catch (NullPointerException npe) {
                    continue;
                
		} else if (splitted[0].equals("/job")) {
			c.getPlayer().changeJob(MapleJob.getById(Integer.parseInt(splitted[1])));
		} else if (splitted[0].equals("/whereami")) {
			new ServernoticeMapleClientMessageCallback(c).dropMessage("You are on map " +
				c.getPlayer().getMap().getId());
		} else if (splitted[0].equals("/shop")) {
			MapleShopFactory sfact = MapleShopFactory.getInstance();
			MapleShop shop = sfact.getShop(getOptionalIntArg(splitted, 1, 1));
			shop.sendShop(c);
		} else if (splitted[0].equals("/levelup")) {
			if (c.getPlayer().getLevel() >= GameConstants.getMaxLevel(c.getPlayer().getJob()))
				return;
			c.getPlayer().levelUp();
			int newexp = c.getPlayer().getExp();
			if (newexp < 0) {
				c.getPlayer().gainExp(-newexp, false, false);
			}
		} else if (splitted[0].equals("/item")) {
			short quantity = (short) getOptionalIntArg(splitted, 2, 1);
			if (Integer.parseInt(splitted[1]) >= 5000000 && Integer.parseInt(splitted[1]) <= 5000100) {
				if (quantity > 1) {
					quantity = 1;
				}
				int petId = MaplePet.createPet(Integer.parseInt(splitted[1]));
				//c.getPlayer().equipChanged();
				MapleInventoryManipulator.addById(c, Integer.parseInt(splitted[1]), quantity, c.getPlayer().getName() +
					"used !item with quantity " + quantity, player.getName(), petId);
				return;
			}
			MapleInventoryManipulator.addById(c, Integer.parseInt(splitted[1]), quantity, c.getPlayer().getName() +
				"used !item with quantity " + quantity, player.getName());
		} else if (splitted[0].equals("/ring")) {
			int itemId = Integer.parseInt(splitted[1]);
			String partnerName = splitted[2];
			int partnerId = MapleCharacter.getIdByName(partnerName, 0);
			int[] ret = MapleRing.createRing(c, itemId, c.getPlayer().getId(), c.getPlayer().getName(), partnerId, partnerName);
			if (ret[0] == -1 || ret[1] == -1) {
				mc.dropMessage("There was an unknown error.");
				mc.dropMessage("Make sure the person you are attempting to create a ring with is online.");
			}
		} else if (splitted[0].equals("/drop")) {
			MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
			int itemId = Integer.parseInt(splitted[1]);
			short quantity = (short) (short) getOptionalIntArg(splitted, 2, 1);
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
		} else if (splitted[0].equals("/level")) {
			int quantity = Integer.parseInt(splitted[1]);
			c.getPlayer().setLevel(quantity);
			c.getPlayer().levelUp();
			int newexp = c.getPlayer().getExp();
			if (newexp < 0) {
				c.getPlayer().gainExp(-newexp, false, false);
			}
		} else if (splitted[0].equals("/online")) {
			mc.dropMessage("Characters connected to channel " + c.getChannel() + ":");
			Collection<MapleCharacter> chrs = c.getChannelServer().getInstance(c.getChannel()).getPlayerStorage().getAllCharacters();
			for (MapleCharacter chr : chrs) {
				mc.dropMessage(chr.getName() + " at map ID: " + chr.getMapId());
			}
			mc.dropMessage("Total characters on channel " + c.getChannel() + ": " + chrs.size());
		} else if (splitted[0].equals("/iamevil")) {
			MapleCharacter victim = ChannelServer.getInstance(c.getChannel()).getPlayerStorage().getCharacterByName(splitted[1]);
			boolean enable = Integer.parseInt(splitted[2]) == 1;
			if (victim != null) {
				victim.getClient().getSession().write(MaplePacketCreator.hideUI(enable));
			} else {
				c.getPlayer().dropMessage(splitted[1] + " does not exist.");
			}
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("/lowhp", "", "", 1),
			new CommandDefinition("/fullhp", "", "", 1),
			new CommandDefinition("/heal", "", "", 1),
			new CommandDefinition("/skill", "", "", 1),
			new CommandDefinition("/sp", "", "", 1),
			new CommandDefinition("/ap", "", "", 1),
			new CommandDefinition("/giftnx", "", "", 1),
			new CommandDefinition("/mesos", "", "", 1),
			new CommandDefinition("/maxskills", "", "", 1),
			new CommandDefinition("/job", "", "", 1),
			new CommandDefinition("/whereami", "", "", 1),
			new CommandDefinition("/shop", "", "", 1),
			new CommandDefinition("/levelup", "", "", 1),
			new CommandDefinition("/item", "", "", 2),
			new CommandDefinition("/drop", "", "", 2),
			new CommandDefinition("/level", "", "", 1),
			new CommandDefinition("/online", "", "", 1),
			new CommandDefinition("/ring", "", "", 1),
			new CommandDefinition("/iamevil", "<victim name> <1 == enable 0 == disable>", "You are evil. Disables/enables the victim's UI. :)", 4),
		};
	}
}
