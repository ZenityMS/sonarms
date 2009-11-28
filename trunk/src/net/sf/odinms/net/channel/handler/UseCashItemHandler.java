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

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

import net.sf.odinms.client.GameConstants;
import net.sf.odinms.client.IEquip;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.ISkill;
import net.sf.odinms.client.ItemFlag;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleCharacterUtil;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MapleJob;
import net.sf.odinms.client.MaplePet;
import net.sf.odinms.client.MapleStat;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.tools.Pair;


public class UseCashItemHandler extends AbstractMaplePacketHandler {
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UseCashItemHandler.class);

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		@SuppressWarnings("unused")
		byte mode = slea.readByte();
		slea.readByte();
		int itemId = slea.readInt();
		IItem citem = c.getPlayer().getInventory(MapleInventoryType.CASH).findById(itemId);
		if (citem == null || citem.getItemId() != itemId || citem.getQuantity() < 1)
			return; //how could this go by unnoticed for this long?
		
		int itemType = itemId/10000;
		
		try {
			MaplePet pet; //yeah.
			IItem item = null;   //yeah.
			byte flag;
			
			switch (itemType) {
				case 505: //AP/SP reset
					if (itemId > 5050000) {
						MapleCharacter player = c.getPlayer();

						int SPTo = slea.readInt();
						int SPFrom = slea.readInt();

						ISkill skillSPTo = SkillFactory.getSkill(SPTo);
						ISkill skillSPFrom = SkillFactory.getSkill(SPFrom);

						int maxlevel = skillSPTo.getMaxLevel();
						int curLevel = player.getSkillLevel(skillSPTo);
						int curLevelSPFrom = player.getSkillLevel(skillSPFrom);

						if ((curLevel + 1 <= maxlevel) && curLevelSPFrom > 0) {
							player.changeSkillLevel(skillSPFrom, curLevelSPFrom - 1, player.getMasterLevel(skillSPFrom));
							player.changeSkillLevel(skillSPTo, curLevel + 1, player.getMasterLevel(skillSPTo));
						}
					} else {
						List<Pair<MapleStat, Integer>> statupdate = new ArrayList<Pair<MapleStat, Integer>>(2);
						int APTo = slea.readInt();
						int APFrom = slea.readInt();

						switch (APFrom) {
							case 64: // str
								if (c.getPlayer().getStr() <= 4)
									return;
								c.getPlayer().setStr(c.getPlayer().getStr() - 1);
								statupdate.add(new Pair<MapleStat, Integer>(MapleStat.STR, c.getPlayer().getStr()));
								break;
							case 128: // dex
								if (c.getPlayer().getDex() <= 4)
									return;
								c.getPlayer().setDex(c.getPlayer().getDex() - 1);
								statupdate.add(new Pair<MapleStat, Integer>(MapleStat.DEX, c.getPlayer().getDex()));
								break;
							case 256: // int
								if (c.getPlayer().getInt() <= 4)
									return;
								c.getPlayer().setInt(c.getPlayer().getInt() - 1);
								statupdate.add(new Pair<MapleStat, Integer>(MapleStat.INT, c.getPlayer().getInt()));
								break;
							case 512: // luk
								if (c.getPlayer().getLuk() <= 4)
									return;
								c.getPlayer().setLuk(c.getPlayer().getLuk() - 1);
								statupdate.add(new Pair<MapleStat, Integer>(MapleStat.LUK, c.getPlayer().getLuk()));
								break;
							case 2048: // HP
								if (c.getPlayer().getHpApUsed() <= 0) {
								    return;
								}
								int maxhp = 0;
								if (c.getPlayer().getJob().isA(MapleJob.BEGINNER)) {
									maxhp -= 12;
								} else if (c.getPlayer().getJob().isA(MapleJob.WARRIOR)) {
									ISkill improvingMaxHP = SkillFactory.getSkill(1000001);
									int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
									maxhp -= 24;
									maxhp -= improvingMaxHP.getEffect(improvingMaxHPLevel).getX();
								} else if (c.getPlayer().getJob().isA(MapleJob.MAGICIAN)) {
									maxhp -= 10;
								} else if (c.getPlayer().getJob().isA(MapleJob.BOWMAN)) {
									maxhp -= 20;
								} else if (c.getPlayer().getJob().isA(MapleJob.THIEF)) {
									maxhp -= 20;
								}
								if (maxhp < ((c.getPlayer().getLevel()* 2) + 148)) {
								    return;
								}
								c.getPlayer().setMaxHp(maxhp);
								c.getPlayer().setHp(c.getPlayer().getMaxHp());
								c.getPlayer().setHpApUsed(c.getPlayer().getHpApUsed() - 1);
								statupdate.add(new Pair<MapleStat, Integer>(MapleStat.HP, c.getPlayer().getMaxHp()));
								break;
							case 8192: // MP
								if (c.getPlayer().getHpApUsed() <= 0) {
								    return;
								}
								int maxmp = 0;
								if (c.getPlayer().getJob().isA(MapleJob.BEGINNER)) {
									maxmp -= 8;
								} else if (c.getPlayer().getJob().isA(MapleJob.WARRIOR)) {
									maxmp -= 4;
								} else if (c.getPlayer().getJob().isA(MapleJob.MAGICIAN)) {
									ISkill improvingMaxMP = SkillFactory.getSkill(2000001);
									int improvingMaxMPLevel = c.getPlayer().getSkillLevel(improvingMaxMP);
									maxmp -= 20;
									maxmp -= 2 * improvingMaxMP.getEffect(improvingMaxMPLevel).getX();
								} else if (c.getPlayer().getJob().isA(MapleJob.BOWMAN)) {
									maxmp -= 12;
								} else if (c.getPlayer().getJob().isA(MapleJob.THIEF)) {
									maxmp -= 12;
								}
								if (maxmp < ((c.getPlayer().getLevel()* 2) + 148)) {
								    return;
								}
								c.getPlayer().setMaxMp(maxmp);
								c.getPlayer().setMp(c.getPlayer().getMaxMp());
								c.getPlayer().setHpApUsed(c.getPlayer().getHpApUsed() - 1);
								statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MP, c.getPlayer().getMaxMp()));
								break;
							default:
								c.getSession().write(
									MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true));
								return;
						}
						switch (APTo) {
							case 64: // str
								if (c.getPlayer().getStr() >= 999)
									return;
								c.getPlayer().setStr(c.getPlayer().getStr() + 1);
								statupdate.add(new Pair<MapleStat, Integer>(MapleStat.STR, c.getPlayer().getStr()));
								break;
							case 128: // dex
								if (c.getPlayer().getDex() >= 999)
									return;
								c.getPlayer().setDex(c.getPlayer().getDex() + 1);
								statupdate.add(new Pair<MapleStat, Integer>(MapleStat.DEX, c.getPlayer().getDex()));
								break;
							case 256: // int
								if (c.getPlayer().getInt() >= 999)
									return;
								c.getPlayer().setInt(c.getPlayer().getInt() + 1);
								statupdate.add(new Pair<MapleStat, Integer>(MapleStat.INT, c.getPlayer().getInt()));
								break;
							case 512: // luk
								if (c.getPlayer().getLuk() >= 999)
									return;
								c.getPlayer().setLuk(c.getPlayer().getLuk() + 1);
								statupdate.add(new Pair<MapleStat, Integer>(MapleStat.LUK, c.getPlayer().getLuk()));
								break;
							case 2048: // hp
								int maxhp = c.getPlayer().getMaxHp();
								if (maxhp >= 30000) {
									c.getSession().write(
										MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true));
									return;
								} else {
									if (c.getPlayer().getJob().isA(MapleJob.BEGINNER)) {
										maxhp += rand(8, 12);
									} else if (c.getPlayer().getJob().isA(MapleJob.WARRIOR)) {
										ISkill improvingMaxHP = SkillFactory.getSkill(1000001);
										int improvingMaxHPLevel = c.getPlayer().getSkillLevel(improvingMaxHP);
										maxhp += rand(20, 24);
										maxhp += improvingMaxHP.getEffect(improvingMaxHPLevel).getX();
									} else if (c.getPlayer().getJob().isA(MapleJob.MAGICIAN)) {
										maxhp += rand(6, 10);
									} else if (c.getPlayer().getJob().isA(MapleJob.BOWMAN)) {
										maxhp += rand(16, 20);
									} else if (c.getPlayer().getJob().isA(MapleJob.THIEF)) {
										maxhp += rand(16, 20);
									}
									maxhp = Math.min(30000, maxhp);
									c.getPlayer().setMaxHp(maxhp);
									c.getPlayer().setHp(c.getPlayer().getMaxHp());
									c.getPlayer().setHpApUsed(c.getPlayer().getHpApUsed() - 1);
									statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MAXHP, c.getPlayer().getMaxHp()));
									break;
								}
							case 8192: // mp
								int maxmp = c.getPlayer().getMaxMp();
								if (maxmp >= 30000) {
									return;
								} else {
									if (c.getPlayer().getJob().isA(MapleJob.BEGINNER)) {
										maxmp += rand(6, 8);
									} else if (c.getPlayer().getJob().isA(MapleJob.WARRIOR)) {
										maxmp += rand(2, 4);
									} else if (c.getPlayer().getJob().isA(MapleJob.MAGICIAN)) {
										ISkill improvingMaxMP = SkillFactory.getSkill(2000001);
										int improvingMaxMPLevel = c.getPlayer().getSkillLevel(improvingMaxMP);
										maxmp += rand(18, 20);
										maxmp += 2 * improvingMaxMP.getEffect(improvingMaxMPLevel).getX();
									} else if (c.getPlayer().getJob().isA(MapleJob.BOWMAN)) {
										maxmp += rand(10, 12);
									} else if (c.getPlayer().getJob().isA(MapleJob.THIEF)) {
										maxmp += rand(10, 12);
									}
									maxmp = Math.min(30000, maxmp);
									c.getPlayer().setMaxMp(maxmp);
									c.getPlayer().setMp(c.getPlayer().getMaxMp());
									c.getPlayer().setHpApUsed(c.getPlayer().getHpApUsed() - 1);
									statupdate.add(new Pair<MapleStat, Integer>(MapleStat.MAXMP, c.getPlayer().getMaxMp()));
									break;
								}
							default:
								c.getSession().write(
									MaplePacketCreator.updatePlayerStats(MaplePacketCreator.EMPTY_STATUPDATE, true));
								return;
						}
						c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true));
					}
					MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
				break;
				case 507:
					int megaType = itemId / 1000 % 10;
					if (megaType == 2) {
						c.getChannelServer().getWorldInterface().broadcastMessage(null, MaplePacketCreator.serverNotice(3, c.getChannel(), 
								c.getPlayer().getMedal() + c.getPlayer().getName() + " : " + slea.readMapleAsciiString()).getBytes());
					} else if (megaType == 6) {
						//4E 00 
						//01 
						//00 
						//20 74 4D 00 - ignore
						//03 00 
						//6C 6F 6C - message
						//00 - ear
						//01 - ? I don't think this matters.
						//01 00 00 00 
						//02 00 00 00 
						//23 5C E8 0F
						String message = c.getPlayer().getMedal() + c.getPlayer().getName() + " : " + slea.readMapleAsciiString();
						boolean showEar = slea.readByte() == 1;
						slea.readByte();
						byte type = (byte)slea.readInt();
						if (slea.available() > 0) {
							byte slot = (byte)slea.readInt();
							item = c.getPlayer().getInventory(MapleInventoryType.getByType(type)).getItem(slot);
						}
						c.getChannelServer().getWorldInterface().broadcastMessage(null, MaplePacketCreator.getItemMegaphone(message, item, c.getChannel(), showEar).getBytes());
						//AF 42 0C 10
					} else if (megaType == 7) {
						//4E 00 
						//03 
						//00 
						//08 78 4D 00 
						//03 
						//2D 00 
						//2B 20 2D 20 2B 20 2D 20 2B 20 2D 20 2B 20 2D 20 2B 20 2D 20 2B 20 2D 20 2B 20 2D 20 2B 20 2D 20 2B 20 2D 20 2B 20 2D 20 2B 20 2D 20 2B 
						//1D 00 
						//2B 20 2D 43 4F 4E 47 52 41 54 53 20 4F 4E 20 4C 45 56 45 4C 49 4E 47 20 55 50 2D 20 2B 
						//2D 00 
						//2B 20 2D 20 2B 20 2D 20 2B 20 2D 20 2B 20 2D 20 2B 20 2D 20 2B 20 2D 20 2B 20 2D 20 2B 20 2D 20 2B 20 2D 20 2B 20 2D 20 2B 20 2D 20 2B 
						//01 - ear
						//95 31 D4 0F
						int numberOfLines = slea.readByte();
						if (numberOfLines != 3)
							return; //hacks.
						String[] messages = new String[numberOfLines];
						for (int i = 0; i < numberOfLines; i++) {
							messages[i] = c.getPlayer().getMedal() + c.getPlayer().getName() + " : " + slea.readMapleAsciiString();
						}
						boolean showEar = slea.readByte() == 1;
						c.getChannelServer().getWorldInterface().broadcastMessage(null, MaplePacketCreator.getMultiMegaphone(messages, c.getChannel(), showEar).getBytes());
					}
					MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
					break;
				case 539:
					List<String> lines = new LinkedList<String>();
					for (int i = 0; i < 4; i++) {
						lines.add(slea.readMapleAsciiString());
					}
					c.getChannelServer().getWorldInterface().broadcastMessage(null, MaplePacketCreator.getAvatarMega(c.getPlayer(), c.getChannel(), itemId, lines).getBytes());
					MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
					break;
				case 512:
					c.getPlayer().getMap().startMapEffect(ii.getMsg(itemId).replaceFirst("%s", c.getPlayer().getName()).replaceFirst("%s", slea.readMapleAsciiString()), itemId);
					MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
					break;
				case 524:
					pet = c.getPlayer().getPet(0);
					if (pet == null) {
						c.getSession().write(MaplePacketCreator.enableActions());
						return;
					}
					if (!pet.canConsume(itemId)) {
						pet = c.getPlayer().getPet(1);
						if (pet != null) {
							if (!pet.canConsume(itemId)) {
								pet = c.getPlayer().getPet(2);
								if (pet != null) {
									if (!pet.canConsume(itemId)) {
										c.getSession().write(MaplePacketCreator.enableActions());
										return;
									}
								} else {
									c.getSession().write(MaplePacketCreator.enableActions());
									return;
								}
							}
						} else {
							c.getSession().write(MaplePacketCreator.enableActions());
							return;
						}
					}
					pet.setFullness(100);
					int closeGain = 100 * c.getChannelServer().getPetExpRate();
					if (pet.getCloseness() < 30000) {
						if (pet.getCloseness() + closeGain > 30000) {
							pet.setCloseness(30000);
						} else {
							pet.setCloseness(pet.getCloseness() + closeGain);
						}
						while (pet.getCloseness() >= GameConstants.getClosenessNeededForLevel(pet.getLevel()+1)) {
							pet.setLevel(pet.getLevel()+1);
							c.getSession().write(MaplePacketCreator.showOwnPetLevelUp(c.getPlayer().getPetIndex(pet)));
							c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.showPetLevelUp(c.getPlayer(), c.getPlayer().getPetIndex(pet)));
						}
					}
					c.getSession().write(MaplePacketCreator.updatePet(pet, true));
					c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.commandResponse(c.getPlayer().getId(), (byte) 1, 0, true, true), true);

					MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
					break;
				case 517:
					// 49 00 24 00 50 E3 4E 00 05 00 4A 61 7A 7A 79 2B A3 41 01
					pet = c.getPlayer().getPet(0);
					if (pet == null) {
						c.getSession().write(MaplePacketCreator.enableActions());
						return;
					}
					String nName = slea.readMapleAsciiString();
					if (MapleCharacterUtil.canChangePetName(nName)) {
					    c.getPlayer().getPet(0).setName(nName);
					    c.getSession().write(MaplePacketCreator.updatePet(c.getPlayer().getPet(0), true));
					    c.getSession().write(MaplePacketCreator.enableActions());
					    c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.changePetName(c.getPlayer(), nName, 1), true);
					    MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
					}
					break;
				case 506:
                	MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
            		item = c.getPlayer().getInventory(type).getItem((byte) slea.readInt());
            		// another int here, lock = 5A E5 F2 0A, 7 day = D2 30 F3 0A
            		if (item == null) {
            		    return;
            		}
            		flag = item.getFlag();
            		flag |= ItemFlag.LOCK.getValue();
            		item.setFlag(flag);
            		
            		c.getSession().write(MaplePacketCreator.updateItemInSlot(item));
            		MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
            		break;
				case 509:
					// 49 00 04 00 D0 AA 4D 00 | 05 00 46 65 65 74 79 04 00 74 65 73 74 DA C6 C9 1D
					String sendTo = slea.readMapleAsciiString();
					String msg = slea.readMapleAsciiString();
					
					try {
						c.getPlayer().sendNote(sendTo, msg);
					} catch (SQLException e) {
						log.error("SAVING NOTE", e);
					}
					
					MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
					break;
				case 552: //Scissors of karma
					//01 00 00 00 
					//0F 00 00 00 
					//B5 7D 19 36
					MapleInventoryType invType = MapleInventoryType.getByType((byte)slea.readInt());
					byte slot = (byte)slea.readInt();
					item = c.getPlayer().getInventory(invType).getItem(slot);
					flag = item.getFlag();
					flag |= ItemFlag.KARMA.getValue();
					item.setFlag(flag);
					c.getSession().write(MaplePacketCreator.updateItemInSlot(item));
					
					MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
					break;
				case 557:
	                slea.readInt(); // useless... (newHammer)
	                int itemSlot = slea.readInt();
	                slea.readInt(); // could be time stamp info...
	
	                item = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte)itemSlot);
	                final IEquip equip = (IEquip) item;
	                
	                c.getSession().write(MaplePacketCreator.enableActions());
	                if (equip.getHammerSlots() >= 2 || item == null)
	                    return; // hkking
	                
	                c.getSession().write(MaplePacketCreator.sendHammerData(equip.getHammerSlots()));
	                equip.setHammerSlots((byte)(equip.getHammerSlots() + 1));
	                equip.setUpgradeSlots((byte)(equip.getUpgradeSlots() + 1));
	                MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, itemId, 1, true, false);
	                c.getSession().write(MaplePacketCreator.updateItemInSlot(equip));
	                break;
				default:
					log.info("UNHANDLED CASH ITEM (" + itemType + ", " + itemId + "): " + slea.toString());
					break;
			}
		} catch (RemoteException e) {
			c.getChannelServer().reconnectWorld();
			log.error("REMOTE ERROR", e);
		}
	}

	private static int rand(int lbound, int ubound) {
		return (int) ((Math.random() * (ubound - lbound + 1)) + lbound);
	}
}