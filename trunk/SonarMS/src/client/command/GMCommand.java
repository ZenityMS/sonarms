package client.command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import client.ExpTable;
import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.MapleJob;
import client.MaplePet;
import client.MapleStat;
import client.SkillFactory;
import java.util.HashMap;
import tools.DatabaseConnection;
import net.channel.ChannelServer;
import net.world.remote.WorldLocation;
import server.MapleInventoryManipulator;
import server.MapleShopFactory;
import server.MapleTrade;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import tools.StringUtil;

class GMCommand {
    static boolean execute(MapleClient c, String[] splitted) {
        MapleCharacter player = c.getPlayer();
        ChannelServer cserv = c.getChannelServer();
        if (splitted[0].equals("!ap")) {
            player.setRemainingAp(Integer.parseInt(splitted[1]));
            player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
        } else if (splitted[0].equals("!balrog")) {
            int[] ids = {8130100, 8150000, 9400536};
            for (int a : ids)
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(a), player.getPosition());
        } else if (splitted[0].equals("!ban"))
            try {
                String v = splitted[1];
                MapleCharacter target = cserv.getPlayerStorage().getCharacterByName(v);
                if (target != null) {
                    if (target.gmLevel() < player.gmLevel()) {
                        target.ban(player.getName() + " banned " + v + ": " + StringUtil.joinStringFrom(splitted, 2) + " (IP: " + target.getClient().getSession().getRemoteAddress().toString().split(":")[0] + ")", true);
                        player.message("You banned " + MapleCharacterUtil.makeMapleReadable(target.getName()) + " for " + StringUtil.joinStringFrom(splitted, 2));
                    }
                } else if (MapleCharacter.ban(v, player.getName() + " banned " + v + " for " + StringUtil.joinStringFrom(splitted, 2), false))
                    player.message("Offline Banned " + v);
                else
                    player.message("Failed to ban " + v);
            } catch (Exception e) {
                player.message(splitted[1] + " could not be banned.");
            }
        else if (splitted[0].equals("!bossdroprate")) {
            int drop = Integer.parseInt(splitted[1]);
            cserv.setBossDropRate(drop);
            cserv.broadcastPacket(MaplePacketCreator.serverNotice(6, "Boss Drop Rate has been changed to " + drop + "x."));
        } else if (splitted[0].equals("!map")) {
            MapleMap target = cserv.getMapFactory().getMap(Integer.parseInt(splitted[1]));
            c.getPlayer().changeMap(target, target.getPortal(0));
        } else if (splitted[0].equals("!buffme")) {
            int[] array = {9001000, 9101002, 9101003, 9101008, 2001002, 1101007, 1005, 2301003, 5121009, 1111002, 4111001, 4111002, 4211003, 4211005, 1321000, 2321004, 3121002};
            for (int i : array)
                SkillFactory.getSkill(i).getEffect(SkillFactory.getSkill(i).getMaxLevel()).applyTo(player);
            player.setEnergyBar(10000);
            c.getSession().write(MaplePacketCreator.giveEnergyCharge(10000));
        } else if (splitted[0].equals("!fakerelog")) {
            c.getSession().write(MaplePacketCreator.getCharInfo(player));
            player.getMap().removePlayer(player);
            player.getMap().addPlayer(player);

        } else if (splitted[0].equals("!cheaters"))
            try {
                for (int x = 0; x < cserv.getWorldInterface().getCheaters().size(); x++)
                    player.message(cserv.getWorldInterface().getCheaters().get(x).getInfo());
            } catch (Exception e) {
                cserv.reconnectWorld();
            }
        else if (splitted[0].equals("!chattype")) {
            player.toggleGMChat();
            player.message("Done.");
        } else if (splitted[0].equals("!cleardrops")) {
            List<MapleMapObject> items = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
            for (MapleMapObject i : items) {
                player.getMap().removeMapObject(i);
                player.getMap().broadcastMessage(MaplePacketCreator.removeItemFromMap(i.getObjectId(), 0, player.getId()));
            }
            player.message("Items Destroyed: " + items.size());
        } else if (splitted[0].equals("!dc")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.getClient().getSession().close();
            victim.getClient().disconnect();
            victim.saveToDB(true);
            cserv.removePlayer(victim);
        } else if (splitted[0].equals("!droprate")) {
            int drop = Integer.parseInt(splitted[1]);
            cserv.setDropRate(drop);
            cserv.broadcastPacket(MaplePacketCreator.serverNotice(6, "Drop Rate has been changed to " + drop + "x."));
        } else if (splitted[0].equals("!exp")) {
            int exp = Integer.parseInt(splitted[1]);
            player.setExp(exp);
            player.updateSingleStat(MapleStat.EXP, exp);
        } else if (splitted[0].equals("!exprate")) {
            int exp = Integer.parseInt(splitted[1]);
            cserv.setExpRate(exp);
            cserv.broadcastPacket(MaplePacketCreator.serverNotice(6, "Exp Rate has been changed to " + exp + "x."));
        } else if (splitted[0].equals("!fame")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setFame(Integer.parseInt(splitted[2]));
            victim.updateSingleStat(MapleStat.FAME, victim.getFame());
        } else if (splitted[0].equals("!giftnx")) {
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).modifyCSPoints(1, Integer.parseInt(splitted[2]));
            player.message("Done!");
        } else if (splitted[0].equals("!gmshop"))
            MapleShopFactory.getInstance().getShop(1337).sendShop(c);
        else if (splitted[0].equals("!heal"))
            player.setHpMp(30000);
         else if (splitted[0].equals("!healmap")) {
            for (MapleCharacter mch : player.getMap().getCharacters()) {
                if (mch != null) {
                    mch.setHp(mch.getMaxHp());
                    mch.updateSingleStat(MapleStat.HP, mch.getMaxHp());
                    mch.setMp(mch.getMaxMp());
                    mch.updateSingleStat(MapleStat.MP, mch.getMaxMp());
                }
                }
         }

         else if (splitted[0].equals("!id"))
            try {
                URL url = new URL("http://www.mapletip.com/search_java.php?search_value=" + splitted[1] + "&check=true");
                BufferedReader dis = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
                String s;
                while ((s = dis.readLine()) != null)
                    player.message(s);
                dis.close();
            } catch (Exception e) {
            }
        else if (splitted[0].equals("!item")) {
            int itemId = Integer.parseInt(splitted[1]);
            short quantity = 1;
            try {
                quantity = (short) Integer.parseInt(splitted[2]);
            } catch (Exception e) {
            }
            if (itemId >= 5000000 && itemId < 5000065)
                MaplePet.createPet(itemId);
            else
                MapleInventoryManipulator.addById(c, itemId, quantity, player.getName());
		} else if (splitted[0].equals("!goto")) {
            HashMap<String, Integer> maps = new HashMap<String, Integer>();
            maps.put("southperry", 60000);
            maps.put("amherst", 1010000);
            maps.put("henesys", 100000000);
            maps.put("ellinia", 101000000);
            maps.put("perion", 102000000);
            maps.put("kerning", 103000000);
            maps.put("lith", 104000000);
            maps.put("sleepywood", 105040300);
            maps.put("florina", 110000000);
            maps.put("gmmap", 180000000);
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
            }
            
        } else if (splitted[0].equals("!job"))
            player.changeJob(MapleJob.getById(Integer.parseInt(splitted[1])));
        else if (splitted[0].equals("!jobperson"))
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).changeJob(MapleJob.getById(Integer.parseInt(splitted[2])));
        else if (splitted[0].equals("!kill"))
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).setHpMp(0);
        else if (splitted[0].equals("!killall")) {
            List<MapleMapObject> monsters = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
            for (MapleMapObject monstermo : monsters) {
                MapleMonster monster = (MapleMonster) monstermo;
                player.getMap().killMonster(monster, player, true);
                monster.giveExpToCharacter(player, monster.getExp(), true, 1);
            }
            player.message("Killed " + monsters.size() + " monsters.");
        } else if (splitted[0].equals("!level")) {
            player.setLevel(Integer.parseInt(splitted[1]));
            player.gainExp(-player.getExp(), false, false);
            player.updateSingleStat(MapleStat.LEVEL, player.getLevel());
        } else if (splitted[0].equals("!levelperson")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setLevel(Integer.parseInt(splitted[2]));
            victim.gainExp(-victim.getExp(), false, false);
            victim.updateSingleStat(MapleStat.LEVEL, victim.getLevel());
        } else if (splitted[0].equals("!levelpro")) {
            while (player.getLevel() < Math.min(255, Integer.parseInt(splitted[1])))
                player.levelUp();
            player.setExp(0);
            player.updateSingleStat(MapleStat.EXP, 0);
        } else if (splitted[0].equals("!levelup"))
            player.gainExp(ExpTable.getExpNeededForLevel(player.getLevel() + 1) - player.getExp(), false, false);
        else if (splitted[0].equals("!maxstat")) {
            String[] s = {"!setall", "32767"};
            execute(c, s);
            player.setLevel(255);
            player.setFame(13337);
            player.setMaxHp(30000);
            player.setMaxMp(30000);
            player.updateSingleStat(MapleStat.LEVEL, 255);
            player.updateSingleStat(MapleStat.FAME, 13337);
            player.updateSingleStat(MapleStat.MAXHP, 30000);
            player.updateSingleStat(MapleStat.MAXMP, 30000);
        } else if (splitted[0].equals("!maxskills"))
            player.maxAllSkills();

        else if (splitted[0].equals("!mesoperson"))
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).gainMeso(Integer.parseInt(splitted[2]), true);
        else if (splitted[0].equals("!mesorate")) {
            int meso = Integer.parseInt(splitted[1]);
            cserv.setMesoRate(meso);
            cserv.broadcastPacket(MaplePacketCreator.serverNotice(6, "Meso Rate has been changed to " + meso + "x."));
        } else if (splitted[0].equals("!maxall")) {
            player.setStr(32767);
            player.setDex(32767);
            player.setInt(32767);
            player.setLuk(32767);
            player.setLevel(255);
            player.setFame(13337);
            player.setMaxHp(30000);
            player.setMaxMp(30000);
            player.updateSingleStat(MapleStat.STR, 32767);
            player.updateSingleStat(MapleStat.DEX, 32767);
            player.updateSingleStat(MapleStat.INT, 32767);
            player.updateSingleStat(MapleStat.LUK, 32767);
            player.updateSingleStat(MapleStat.LEVEL, 255);
            player.updateSingleStat(MapleStat.FAME, 13337);
            player.updateSingleStat(MapleStat.MAXHP, 30000);
            player.updateSingleStat(MapleStat.MAXMP, 30000);
        } else if (splitted[0].equals("!mesos"))
            player.gainMeso(Integer.parseInt(splitted[1]), true);
        else if (splitted[0].equals("!mushmom")) {
            int[] ids = {6130101, 6300005, 9400205};
            for (int a : ids)
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(a), player.getPosition());
        } else if (splitted[0].equals("!mute")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setCanTalk(victim.getCanTalk() ? 2 : 0);
            player.message("Done.");
        } else if (splitted[0].equals("!notice") || (splitted[0].equals("!say"))) {
            String type = "[Notice] ";
            if (splitted[0].equals("!say"))
                type = "[" + player.getName() + "] ";
            try {
                cserv.getWorldInterface().broadcastMessage(player.getName(), MaplePacketCreator.serverNotice(6, type + StringUtil.joinStringFrom(splitted, 1)).getBytes());
            } catch (Exception e) {
                cserv.reconnectWorld();
            }
        } else if (splitted[0].equals("!nx"))
            for (int x = 0; x < 10; x++)
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9400202), player.getPosition());
        else if (splitted[0].equals("!online")) {
            String s = "Characters online: ";
            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters())
                s += MapleCharacterUtil.makeMapleReadable(chr.getName()) + ", ";
            player.message(s.substring(0, s.length() - 2));
        } else if (splitted[0].equals("!pap"))
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8500001), player.getPosition());
        else if (splitted[0].equals("!pianus"))
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8510000), player.getPosition());
        else if (splitted[0].equals("!pos"))
            player.message("Map: " + player.getMap().getId() + ", Position: (" + player.getPosition().x + ", " + player.getPosition().y + ") FH: " + player.getMap().getFootholds().findBelow(player.getPosition()).getId());
        else if (splitted[0].equals("!servermessage"))
            for (int i = 1; i <= ChannelServer.getAllInstances().size(); i++)
                ChannelServer.getInstance(i).setServerMessage(StringUtil.joinStringFrom(splitted, 1));
        else if (splitted[0].equals("!setall")) {
            int x = Short.parseShort(splitted[1]);
            player.setStr(x);
            player.setDex(x);
            player.setInt(x);
            player.setLuk(x);
            player.updateSingleStat(MapleStat.STR, x);
            player.updateSingleStat(MapleStat.DEX, x);
            player.updateSingleStat(MapleStat.INT, x);
            player.updateSingleStat(MapleStat.LUK, x);
        } else if (splitted[0].equals("!sp")) {
            player.setRemainingSp(Integer.parseInt(splitted[1]));
            player.updateSingleStat(MapleStat.AVAILABLESP, player.getRemainingSp());
        } else if (splitted[0].equals("!spawn")) {
            int quantity;
            try {
                quantity = Integer.parseInt(splitted[2]);
            } catch (Exception e) {
                quantity = 1;
            }
            for (int i = 0; i < Math.min(quantity, 500); i++)
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(Integer.parseInt(splitted[1])), player.getPosition());
        } else if (splitted[0].equals("!unban")) {
            try {
                PreparedStatement p = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET banned = -1 WHERE id = " + MapleCharacter.getIdByName(splitted[1], player.getWorld()));
                p.executeUpdate();
                p.close();
            } catch (Exception e) {
                player.message("Failed to unban " + splitted[1]);
                return true;
            }
            player.message("Unbanned " + splitted[1]);
        } else if (splitted[0].equals("!warp")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            if (victim != null)
                if (splitted.length == 2)
                    player.changeMap(victim.getMap(), victim.getMap().findClosestSpawnpoint(victim.getPosition()));
                else
                    victim.changeMap(cserv.getMapFactory().getMap(Integer.parseInt(splitted[2])));
            else
                try {
                    WorldLocation loc = cserv.getWorldInterface().getLocation(splitted[1]);
                    if (loc != null) {
                        String ip = cserv.getIP(loc.channel);
                        player.getMap().removePlayer(player);
                        player.setMap(cserv.getMapFactory().getMap(loc.map));
                        if (player.getTrade() != null)
                            MapleTrade.cancelTrade(player);
                        player.saveToDB(true);
                        if (player.getCheatTracker() != null)
                            player.getCheatTracker().dispose();
                        cserv.removePlayer(player);
                        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
                        try {
                            c.getSession().write(MaplePacketCreator.getChannelChange(InetAddress.getByName(ip.split(":")[0]), Integer.parseInt(ip.split(":")[1])));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else
                        player.changeMap(cserv.getMapFactory().getMap(Integer.parseInt(splitted[1])));
                } catch (Exception e) {
                }
        } else if (splitted[0].equals("!warpallhere")) {
            for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                if (mch.getMapId() != player.getMapId()) {
                    mch.changeMap(player.getMap(), player.getPosition());
                }
            }
        } else if (splitted[0].equals("!whosthere")) {
            StringBuilder builder = new StringBuilder("Players on Map: ");
            for (MapleCharacter chr : player.getMap().getCharacters()) {
                if (builder.length() > 150) {
                    builder.setLength(builder.length() - 2);
                    player.dropMessage(builder.toString());
                }
                builder.append(MapleCharacterUtil.makeMapleReadable(chr.getName()) + ", ");
            }
            builder.setLength(builder.length() - 2);
            c.getSession().write(MaplePacketCreator.serverNotice(6, builder.toString()));
        } else if (splitted[0].equals("!warphere"))
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).changeMap(player.getMap(), player.getPosition());
        else {
            if (player.gmLevel() == 1)
                player.message("GM Command " + splitted[0] + " does not exist");
            return false;
        }
        return true;
    }
}