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
package client.command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import constants.ExpTable;
import client.IItem;
import client.ISkill;
import client.Item;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.MapleJob;
import client.MaplePet;
import client.MapleStat;
import client.SkillFactory;
import constants.ServerConstants;
import java.io.File;
import java.util.LinkedList;
import tools.DatabaseConnection;
import net.channel.ChannelServer;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import scripting.npc.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleShopFactory;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.Pair;

class GMCommand {
    static boolean execute(MapleClient c, String[] splitted, char heading) {
        MapleCharacter player = c.getPlayer();
        ChannelServer cserv = c.getChannelServer();
        if (splitted[0].equals("ap")) {
            player.setRemainingAp(Integer.parseInt(splitted[1]));
            player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
        } else if (splitted[0].equals("buffme")) {
            final int[] array = {9001000, 9101002, 9101003, 9101008, 2001002, 1101007, 1005, 2301003, 5121009, 1111002, 4111001, 4111002, 4211003, 4211005, 1321000, 2321004, 3121002};
            for (int i : array)
                SkillFactory.getSkill(i).getEffect(SkillFactory.getSkill(i).getMaxLevel()).applyTo(player);
        } else if (splitted[0].equals("chattype")) {
            player.toggleGMChat();
            player.message("You now chat in " + (player.getGMChat() ? "white." : "black."));
        } else if (splitted[0].equals("cleardrops"))
            player.getMap().clearDrops(player, true);
        else if (splitted[0].equals("cody"))
            NPCScriptManager.getInstance().start(c, 9200000, null, null);
        else if (splitted[0].equals("dc")) {
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).getClient().disconnect();
        } else if (splitted[0].equals("dispose")) {
            NPCScriptManager.getInstance().dispose(c);
            c.getSession().write(MaplePacketCreator.enableActions());
            player.message("Done.");
        } else if (splitted[0].equals("exprate")) {
            byte exp = (byte) (Integer.parseInt(splitted[1]) % 128);
            ServerConstants.EXP_RATE = exp;
            cserv.broadcastPacket(MaplePacketCreator.serverNotice(6, "Exp Rate has been changed to " + Integer.parseInt(splitted[1]) + "x."));
        } else if (splitted[0].equals("fame")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setFame(Integer.parseInt(splitted[2]));
            victim.updateSingleStat(MapleStat.FAME, victim.getFame());
        } else if (splitted[0].equals("giftnx")) {
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).modifyCSPoints(1, Integer.parseInt(splitted[2]));
            player.message("Done");
        } else if (splitted[0].equals("gmshop"))
            MapleShopFactory.getInstance().getShop(1337).sendShop(c);
        else if (splitted[0].equals("heal"))
            player.setHpMp(30000);
        else if (splitted[0].equals("id"))
            try {
                BufferedReader dis = new BufferedReader(new InputStreamReader(new URL("http://www.mapletip.com/search_java.php?search_value=" + splitted[1] + "&check=true").openConnection().getInputStream()));
                String s;
                while ((s = dis.readLine()) != null)
                    player.dropMessage(s);
                dis.close();
            } catch (Exception e) {
            }
        else if (splitted[0].equals("item") || splitted[0].equals("drop")) {
            int itemId = Integer.parseInt(splitted[1]);
            short quantity = 1;
            try {
                quantity = Short.parseShort(splitted[2]);
            } catch (Exception e) {
            }
            if (splitted[0].equals("item"))
                if (itemId >= 5000000 && itemId < 5000065)
                    MaplePet.createPet(itemId);
                else
                    MapleInventoryManipulator.addById(c, itemId, quantity, player.getName());
            else {
                IItem toDrop;
                if (MapleItemInformationProvider.getInstance().getInventoryType(itemId) == MapleInventoryType.EQUIP)
                    toDrop = MapleItemInformationProvider.getInstance().getEquipById(itemId);
                else
                    toDrop = new Item(itemId, (byte) 0, (short) quantity);
                c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), toDrop, c.getPlayer().getPosition(), true, true);
            }
        } else if (splitted[0].equals("job"))
            player.changeJob(MapleJob.getById(Integer.parseInt(splitted[1])));
        else if (splitted[0].equals("jobperson"))
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).changeJob(MapleJob.getById(Integer.parseInt(splitted[2])));
        else if (splitted[0].equals("kill"))
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).setHpMp(0);
        else if (splitted[0].equals("killall")) {
            List<MapleMapObject> monsters = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
            for (MapleMapObject monstermo : monsters) {
                MapleMonster monster = (MapleMonster) monstermo;
                player.getMap().killMonster(monster, player, true);
                monster.giveExpToCharacter(player, monster.getExp(), true, 1);
            }
            player.dropMessage("Killed " + monsters.size() + " monsters.");
        } else if (splitted[0].equals("level")) {
            player.setLevel(Integer.parseInt(splitted[1]));
            player.gainExp(-player.getExp(), false, false);
            player.updateSingleStat(MapleStat.LEVEL, player.getLevel());
        } else if (splitted[0].equals("levelperson")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setLevel(Integer.parseInt(splitted[2]));
            victim.gainExp(-victim.getExp(), false, false);
            victim.updateSingleStat(MapleStat.LEVEL, victim.getLevel());
        } else if (splitted[0].equals("levelpro")) {
            while (player.getLevel() < Math.min(255, Integer.parseInt(splitted[1])))
                player.levelUp();
            player.setExp(0);
            player.updateSingleStat(MapleStat.EXP, 0);
        } else if (splitted[0].equals("levelup"))
            player.gainExp(ExpTable.getExpNeededForLevel(player.getLevel()) - player.getExp(), false, false);
        else if (splitted[0].equals("maxstat")) {
            final String[] s = {"setall", String.valueOf(Short.MAX_VALUE)};
            execute(c, s, heading);
            player.setLevel(255);
            player.setFame(13337);
            player.setMaxHp(30000);
            player.setMaxMp(30000);
            player.updateSingleStat(MapleStat.LEVEL, 255);
            player.updateSingleStat(MapleStat.FAME, 13337);
            player.updateSingleStat(MapleStat.MAXHP, 30000);
            player.updateSingleStat(MapleStat.MAXMP, 30000);
        } else if (splitted[0].equals("maxskills"))
            for (MapleData skill_ : MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren())
                try {
                    ISkill skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
                    player.changeSkillLevel(skill, skill.getMaxLevel(), skill.getMaxLevel());
                } catch (NumberFormatException nfe) {
                    break;
                } catch (NullPointerException npe) {
                    continue;
                }
        else if (splitted[0].equals("mesoperson"))
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).gainMeso(Integer.parseInt(splitted[2]), true);
        else if (splitted[0].equals("mesorate")) {
            byte meso = (byte) (Integer.parseInt(splitted[1]) % 128);
            ServerConstants.MESO_RATE = meso;
            cserv.broadcastPacket(MaplePacketCreator.serverNotice(6, "Meso Rate has been changed to " + Integer.parseInt(splitted[1]) + "x."));
        } else if (splitted[0].equals("mesos"))
            player.gainMeso(Integer.parseInt(splitted[1]), true);
        else if (splitted[0].equals("notice"))
            try {
                cserv.getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, "[Notice] " + joinStringFrom(splitted, 1)).getBytes());
            } catch (Exception e) {
                cserv.reconnectWorld();
            }
        else if (splitted[0].equals("online")) {
            String s = "Characters online (" + cserv.getPlayerStorage().getAllCharacters().size() + ") : ";
            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters())
                s += MapleCharacter.makeMapleReadable(chr.getName()) + ", ";
            player.dropMessage(s.substring(0, s.length() - 2));
        } else if (splitted[0].equals("pap"))
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8500001), player.getPosition());
        else if (splitted[0].equals("pianus"))
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8510000), player.getPosition());
        else if (splitted[0].equalsIgnoreCase("search"))
            if (splitted.length > 2) {
                String search = joinStringFrom(splitted, 2);
                MapleData data = null;
                MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File("wz/String.wz"));
                player.dropMessage("~Searching~ <<Type: " + splitted[1] + " | Search: " + search + ">>");
                if (!splitted[1].equalsIgnoreCase("ITEM")) {
                    if (splitted[1].equalsIgnoreCase("NPC"))
                        data = dataProvider.getData("Npc.img");
                    else if (splitted[1].equalsIgnoreCase("MAP"))
                        data = dataProvider.getData("Map.img");
                    else if (splitted[1].equalsIgnoreCase("MOB")) {
                        List<String> retMobs = new LinkedList<String>();
                        data = dataProvider.getData("Mob.img");
                        List<Pair<Integer, String>> mobPairList = new LinkedList<Pair<Integer, String>>();
                        for (MapleData mobIdData : data.getChildren()) {
                            int mobIdFromData = Integer.parseInt(mobIdData.getName());
                            String mobNameFromData = MapleDataTool.getString(mobIdData.getChildByPath("name"), "NO-NAME");
                            mobPairList.add(new Pair<Integer, String>(mobIdFromData, mobNameFromData));
                        }
                        for (Pair<Integer, String> mobPair : mobPairList)
                            if (mobPair.getRight().toLowerCase().contains(search.toLowerCase()))
                                retMobs.add(mobPair.getLeft() + " - " + mobPair.getRight());
                        if (retMobs != null && retMobs.size() > 0)
                            for (String singleRetMob : retMobs)
                                player.dropMessage(singleRetMob);
                        else
                            player.dropMessage("No Mob's Found");
                    } else if (splitted[1].equalsIgnoreCase("SKILL"))
                        data = dataProvider.getData("Skill.img");
                    else
                        player.dropMessage("Invalid search.\nSyntax: '/search [type] [name]', where [type] is NPC, MAP, ITEM, MOB, or SKILL.");
                    List<Pair<Integer, String>> searchList = new LinkedList<Pair<Integer, String>>();
                    for (MapleData searchData : data.getChildren()) {
                        int searchFromData = Integer.parseInt(searchData.getName());
                        String npcNameFromData = (splitted[1].equalsIgnoreCase("MAP") || splitted[1].equalsIgnoreCase("MAPS")) ? MapleDataTool.getString(searchData.getChildByPath("streetName"), "NO-NAME") + " - " + MapleDataTool.getString(searchData.getChildByPath("mapName"), "NO-NAME") : MapleDataTool.getString(searchData.getChildByPath("name"), "NO-NAME");
                        searchList.add(new Pair<Integer, String>(searchFromData, npcNameFromData));
                    }
                    for (Pair<Integer, String> searched : searchList)
                        if (searched.getRight().toLowerCase().contains(search.toLowerCase()))
                            player.dropMessage(searched.getLeft() + " - " + searched.getRight());
                } else {
                    for (Pair<Integer, String> itemPair : MapleItemInformationProvider.getInstance().getAllItems())
                        if (itemPair.getRight().toLowerCase().contains(search.toLowerCase()))
                            player.dropMessage(itemPair.getLeft() + " - " + itemPair.getRight());
                    player.dropMessage("Search Complete.");
                }
            } else
                player.dropMessage("Invalid search.\nSyntax: '/search [type] [name]', where [type] is NPC, MAP, ITEM, MOB, or SKILL.");
        else if (splitted[0].equals("servermessage"))
            for (int i = 1; i <= ChannelServer.getAllInstances().size(); i++)
                ChannelServer.getInstance(i).setServerMessage(joinStringFrom(splitted, 1));
        else if (splitted[0].equals("setall")) {
            final int x = Short.parseShort(splitted[1]);
            player.setStr(x);
            player.setDex(x);
            player.setInt(x);
            player.setLuk(x);
            player.updateSingleStat(MapleStat.STR, x);
            player.updateSingleStat(MapleStat.DEX, x);
            player.updateSingleStat(MapleStat.INT, x);
            player.updateSingleStat(MapleStat.LUK, x);
        } else if (splitted[0].equals("sp")) {
            player.setRemainingSp(Integer.parseInt(splitted[1]));
            player.updateSingleStat(MapleStat.AVAILABLESP, player.getRemainingSp());
        } else if (splitted[0].equals("unban")) {
            try {
                PreparedStatement p = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET banned = -1 WHERE id = " + MapleCharacter.getIdByName(splitted[1]));
                p.executeUpdate();
                p.close();
            } catch (Exception e) {
                player.message("Failed to unban " + splitted[1]);
                return true;
            }
            player.message("Unbanned " + splitted[1]);
        } else {
            if (player.gmLevel() == 1)
                player.message("GM Command " + heading + splitted[0] + " does not exist");
            return false;
        }
        return true;
    }

    static String joinStringFrom(String arr[], int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < arr.length; i++) {
            builder.append(arr[i]);
            if (i != arr.length - 1)
                builder.append(" ");
        }
        return builder.toString();
    }
}
