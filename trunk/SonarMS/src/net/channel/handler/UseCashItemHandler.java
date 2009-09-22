package net.channel.handler;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import client.Equip;
import client.ExpTable;
import client.ISkill;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleInventoryType;
import client.MapleJob;
import client.MaplePet;
import client.MapleStat;
import client.SkillFactory;
import net.AbstractMaplePacketHandler;
import scripting.npc.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.maps.MapleMap;
import server.maps.MapleMist;
import server.maps.MapleTVEffect;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

public class UseCashItemHandler extends AbstractMaplePacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        slea.readByte();
        slea.readByte();
        int itemId = slea.readInt();
        int itemType = itemId / 10000;
        try {
            if (itemType == 505) { // AP/SP reset
                int max = c.getChannelServer().getMaxStat();
                if (itemId > 5050000) {
                    int SPTo = slea.readInt();
                    int SPFrom = slea.readInt();
                    ISkill skillSPTo = SkillFactory.getSkill(SPTo);
                    ISkill skillSPFrom = SkillFactory.getSkill(SPFrom);
                    int curLevel = player.getSkillLevel(skillSPTo);
                    int curLevelSPFrom = player.getSkillLevel(skillSPFrom);
                    if ((curLevel < skillSPTo.getMaxLevel()) && curLevelSPFrom > 0) {
                        player.changeSkillLevel(skillSPFrom, curLevelSPFrom - 1, player.getMasterLevel(skillSPFrom));
                        player.changeSkillLevel(skillSPTo, curLevel + 1, player.getMasterLevel(skillSPTo));
                    }
                } else {
                    List<Pair<MapleStat, Integer>> statupdate = new ArrayList<Pair<MapleStat, Integer>>(2);
                    int APTo = slea.readInt();
                    int APFrom = slea.readInt();
                    switch (APFrom) {
                        case 64: // str
                            if (player.getStr() < 5)
                                return;
                            player.addStat(1, -1);
                            break;
                        case 128: // dex
                            if (player.getDex() < 5)
                                return;
                            player.addStat(2, -1);
                            break;
                        case 256: // int
                            if (player.getInt() < 5)
                                return;
                            player.addStat(3, -1);
                            break;
                        case 512: // luk
                            if (player.getLuk() < 5)
                                return;
                            player.addStat(4, -1);
                            break;
                        case 2048: // HP
                            if (player.getHpApUsed() < 1 || player.getHpApUsed() == 10000)
                                return;
                            int maxhp = player.getMaxHp();
                            if (player.getJob().isA(MapleJob.BEGINNER))
                                maxhp -= 12;
                            else if (player.getJob().isA(MapleJob.WARRIOR)) {
                                ISkill improvingMaxHP = SkillFactory.getSkill(1000001);
                                int improvingMaxHPLevel = player.getSkillLevel(improvingMaxHP);
                                maxhp -= 24;
                                if (improvingMaxHPLevel >= 1)
                                    maxhp -= improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
                            } else if (player.getJob().isA(MapleJob.MAGICIAN))
                                maxhp -= 10;
                            else if (player.getJob().isA(MapleJob.BOWMAN))
                                maxhp -= 20;
                            else if (player.getJob().isA(MapleJob.THIEF))
                                maxhp -= 20;
                            else if (player.getJob().isA(MapleJob.PIRATE)) {
                                ISkill improvingMaxHP = SkillFactory.getSkill(5100000);
                                int improvingMaxHPLevel = player.getSkillLevel(improvingMaxHP);
                                maxhp -= 20;
                                if (improvingMaxHPLevel >= 1)
                                    maxhp -= improvingMaxHP.getEffect(improvingMaxHPLevel).getY();
                            }
                            if (maxhp < player.getLevel() * 2 + 148)
                                return;
                            player.setHpApUsed(player.getHpApUsed() - 1);
                            player.setHp(maxhp);
                            player.setMaxHp(maxhp);
                            statupdate.add(new Pair<MapleStat, Integer>(MapleStat.HP, player.getMaxHp()));
                            statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, player.getMaxHp()));
                            break;
                        case 8192: // MP
                            if (player.getHpApUsed() <= 0 || player.getMpApUsed() == 10000)
                                return;
                            int maxmp = player.getMaxMp();
                            if (player.getJob().isA(MapleJob.BEGINNER))
                                maxmp -= 8;
                            else if (player.getJob().isA(MapleJob.WARRIOR))
                                maxmp -= 4;
                            else if (player.getJob().isA(MapleJob.MAGICIAN)) {
                                ISkill improvingMaxMP = SkillFactory.getSkill(2000001);
                                int improvingMaxMPLevel = player.getSkillLevel(improvingMaxMP);
                                maxmp -= 20;
                                if (improvingMaxMPLevel > 0)
                                    maxmp -= improvingMaxMP.getEffect(improvingMaxMPLevel).getY();
                            } else if (player.getJob().isA(MapleJob.BOWMAN))
                                maxmp -= 12;
                            else if (player.getJob().isA(MapleJob.THIEF))
                                maxmp -= 12;
                            else if (player.getJob().isA(MapleJob.PIRATE))
                                maxmp -= 16;
                            if (maxmp < player.getLevel() * 2 + 148)
                                return;
                            player.setMpApUsed(player.getMpApUsed() - 1);
                            player.setMp(maxmp);
                            player.setMaxMp(maxmp);
                            statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MP, player.getMaxMp()));
                            statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, player.getMaxMp()));
                            break;
                        default:
                            c.getSession().write(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true));
                            return;
                    }
                    switch (APTo) {
                        case 64: // str
                            if (player.getStr() >= max)
                                return;
                            player.addStat(1, 1);
                            break;
                        case 128: // dex
                            if (player.getDex() >= max)
                                return;
                            player.addStat(2, 1);
                            break;
                        case 256: // int
                            if (player.getInt() >= max)
                                return;
                            player.addStat(3, 1);
                            break;
                        case 512: // luk
                            if (player.getLuk() >= max)
                                return;
                            player.addStat(4, 1);
                            break;
                        case 2048: // hp
                            DistributeAPHandler.addHP(c);
                            break;
                        case 8192: // mp
                            DistributeAPHandler.addMP(c);
                            break;
                        default:
                            c.getSession().write(MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true));
                            return;
                    }
                    c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true));
                }
                remove(c, itemId);
            } else if (itemType == 506) {
                slea.readInt();
                byte slot = slea.readByte();
                Equip nEquip = (Equip) player.getInventory(MapleInventoryType.EQUIP).getItem(slot);
                nEquip.setLocked((byte) 1);
                remove(c, itemId);
            } else if (itemType == 507) {
                switch (itemId / 1000 % 10) {
                    case 1: // Megaphone
                        if (player.getLevel() > 9)
                            player.getClient().getChannelServer().broadcastPacket(MaplePacketCreator.serverNotice(2, player.getName() + " : " + slea.readMapleAsciiString()));
                        else
                            player.dropMessage(1, "You may not use this until you're level 10");
                        break;
                    case 2: // Super megaphone
                        c.getChannelServer().getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(3, c.getChannel(), player.getName() + " : " + slea.readMapleAsciiString(), (slea.readByte() != 0)).getBytes());
                        break;
                    case 5: // Maple TV
                        int tvType = itemId % 10;
                        boolean megassenger = false;
                        boolean ear = false;
                        MapleCharacter victim = null;
                        if (tvType != 1) {
                            if (tvType >= 3) {
                                megassenger = true;
                                if (tvType == 3)
                                    slea.readByte();
                                ear = 1 == slea.readByte();
                            } else if (tvType != 2)
                                slea.readByte();
                            if (tvType != 4)
                                victim = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
                        }
                        List<String> messages = new LinkedList<String>();
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < 5; i++) {
                            String message = slea.readMapleAsciiString();
                            if (megassenger) {
                                builder.append(" ");
                                builder.append(message);
                            }
                            messages.add(message);
                        }
                        slea.readInt();
                        if (megassenger)
                            c.getChannelServer().getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(3, c.getChannel(), player.getName() + " : " + builder.toString(), ear).getBytes());
                        if (!MapleTVEffect.isActive()) {
                            new MapleTVEffect(player, victim, messages, tvType);
                            remove(c, itemId);
                        } else {
                            player.dropMessage(1, "MapleTV is already in use.");
                            return;
                        }
                        break;
                }
                remove(c, itemId);
            } else if (itemType == 509) {
                String sendTo = slea.readMapleAsciiString();
                String msg = slea.readMapleAsciiString();
                try {
                    player.sendNote(sendTo, msg);
                } catch (Exception e) {
                }
                remove(c, itemId);
            } else if (itemType == 510) {
                player.getMap().broadcastMessage(MaplePacketCreator.musicChange("Jukebox/Congratulation"));
                remove(c, itemId);
            } else if (itemType == 512) {
                player.getMap().startMapEffect(ii.getMsg(itemId).replaceFirst("%s", player.getName()).replaceFirst("%s", slea.readMapleAsciiString()), itemId);
                remove(c, itemId);
            } else if (itemType == 517) {
                MaplePet pet = player.getPet(0);
                if (pet == null) {
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                String newName = slea.readMapleAsciiString();
                pet.setName(newName);
                c.getSession().write(MaplePacketCreator.updatePet(pet));
                c.getSession().write(MaplePacketCreator.enableActions());
                player.getMap().broadcastMessage(player, MaplePacketCreator.changePetName(player, newName, 1), true);
                remove(c, itemId);
            } else if (itemType == 504) { // vip teleport rock
                String error1 = "Either the player could not be found or you were trying to teleport to an illegal location.";
                byte rocktype = slea.readByte();
                remove(c, itemId);
                c.getSession().write(MaplePacketCreator.TrockRefreshMapList(player.getId(), rocktype));
                if (rocktype == 0) {
                    int mapId = slea.readInt();
                    if (c.getChannelServer().getMapFactory().getMap(mapId).getForcedReturnId() == 999999999) //Makes sure this map doesn't have a forced return map
                        player.changeMap(c.getChannelServer().getMapFactory().getMap(mapId));
                    else {
                        MapleInventoryManipulator.addById(c, itemId, (short) 1, "");
                        c.getPlayer().dropMessage(1, error1);
                        c.getSession().write(MaplePacketCreator.enableActions());
                    }
                } else {
                    String name = slea.readMapleAsciiString();
                    MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(name);
                    boolean success = false;
                    if (victim != null) {
                        MapleMap target = victim.getMap();
                        if (c.getChannelServer().getMapFactory().getMap(c.getChannelServer().getWorldInterface().getLocation(name).map).getForcedReturnId() == 999999999 || victim.getMapId() < 100000000)//This doesn't allow tele to GM map, zakum and etc...
                            if (!victim.isHidden() && victim.gmLevel() < 1)
                                if (itemId == 5041000) { //viprock
                                    player.changeMap(target, target.findClosestSpawnpoint(victim.getPosition()));
                                    success = true;
                                } else if (victim.getMapId() / player.getMapId() == 1) { //same continent
                                    player.changeMap(target, target.findClosestSpawnpoint(victim.getPosition()));
                                    success = true;
                                } else
                                    player.dropMessage(1, error1);
                            else
                                player.dropMessage(1, error1);
                        else
                            player.dropMessage(1, "You cannot teleport to this map.");
                    } else
                        player.dropMessage(1, "Player could not be found in this channel.");
                    if (!success) {
                        MapleInventoryManipulator.addById(c, itemId, (short) 1, null);
                        c.getSession().write(MaplePacketCreator.enableActions());
                    }
                }
            } else if (itemType == 520) {
                player.gainMeso(ii.getMeso(itemId), true, false, true);
                remove(c, itemId);
                c.getSession().write(MaplePacketCreator.enableActions());
            } else if (itemType == 524)
                for (int i = 0; i < 3; i++) {
                    MaplePet pet = player.getPet(i);
                    if (pet != null) {
                        if (pet.canConsume(itemId)) {
                            pet.setFullness(100);
                            int closeGain = 100 * c.getChannelServer().getPetExpRate();
                            if (pet.getCloseness() + closeGain > 30000)
                                pet.setCloseness(30000);
                            else
                                pet.gainCloseness(closeGain);
                            while (pet.getCloseness() >= ExpTable.getClosenessNeededForLevel(pet.getLevel() + 1)) {
                                pet.setLevel(pet.getLevel() + 1);
                                c.getSession().write(MaplePacketCreator.showOwnPetLevelUp(player.getPetIndex(pet)));
                                player.getMap().broadcastMessage(MaplePacketCreator.showPetLevelUp(c.getPlayer(), player.getPetIndex(pet)));
                            }
                            c.getSession().write(MaplePacketCreator.updatePet(pet));
                            player.getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.commandResponse(player.getId(), (byte) 1, 0, true, true), true);
                            remove(c, itemId);
                            break;
                        }
                    } else
                        break;
                }
            else if (itemType == 528) {
                if (itemId == 5281000) {
                    MapleStatEffect mse = new MapleStatEffect();
                    mse.setSourceId(2111003);
                    player.getMap().spawnMist(new MapleMist(new Rectangle((int) player.getPosition().getX(), (int) player.getPosition().getY(), 1, 1), player, mse), 10000, false, true);
                    player.getMap().broadcastMessage(MaplePacketCreator.getChatText(player.getId(), "Oh no, I farted!", false, 1));
                    c.getSession().write(MaplePacketCreator.enableActions());
                }
            } else if (itemType == 530) {
                ii.getItemEffect(itemId).applyTo(player);
                remove(c, itemId);
            } else if (itemType == 533)
                NPCScriptManager.getInstance().start(c, 9010009);
            else if (itemType == 537) {
                player.setChalkboard(slea.readMapleAsciiString());
                player.getMap().broadcastMessage(MaplePacketCreator.useChalkboard(player, false));
                player.getClient().getSession().write(MaplePacketCreator.enableActions());
            } else if (itemType == 539) {
                long time = c.getPlayer().lastSmega;
                if (System.currentTimeMillis() - time > 1000) {
                    List<String> lines = new LinkedList<String>();
                    for (int i = 0; i < 4; i++)
                        lines.add(slea.readMapleAsciiString());
                    c.getChannelServer().getWorldInterface().broadcastMessage(null, MaplePacketCreator.getAvatarMega(player, c.getChannel(), itemId, lines, (slea.readByte() != 0)).getBytes());
                    c.getPlayer().lastSmega = System.currentTimeMillis();
                }
                remove(c, itemId);
            }
        } catch (Exception e) {
            c.getChannelServer().reconnectWorld();
            e.printStackTrace();
        }
    }

    private void remove(MapleClient c, int itemId) {
        MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
    }
}