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

/*
 * MapleItemInformationProvider.java
 * 
 * Created on 26. November 2007, 21:58
 * 
 * To change this template, choose Tools | Template Manager and open the template in the editor.
 */

package net.sf.odinms.server;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import net.sf.odinms.client.Equip;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.ItemFlag;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MapleWeaponType;
import net.sf.odinms.database.DatabaseConnection;
import net.sf.odinms.provider.MapleData;
import net.sf.odinms.provider.MapleDataDirectoryEntry;
import net.sf.odinms.provider.MapleDataFileEntry;
import net.sf.odinms.provider.MapleDataProvider;
import net.sf.odinms.provider.MapleDataProviderFactory;
import net.sf.odinms.provider.MapleDataTool;
import net.sf.odinms.tools.Pair;

/**
 * 
 * @author Matze
 * 
 * TODO: make faster
 * 
 */
public class MapleItemInformationProvider {
	private static MapleItemInformationProvider instance = null;
	protected MapleDataProvider itemData;
	protected MapleDataProvider equipData;
	protected MapleDataProvider stringData;
	protected MapleData cashStringData;
	protected MapleData consumeStringData;
	protected MapleData eqpStringData;
	protected MapleData etcStringData;
	protected MapleData insStringData;
	protected MapleData petStringData;
	protected Map<Integer, MapleInventoryType> inventoryTypeCache = new HashMap<Integer, MapleInventoryType>();
	protected Map<Integer, Short> slotMaxCache = new HashMap<Integer, Short>();
	protected Map<Integer, Boolean> isCashCache = new HashMap<Integer, Boolean>();
	protected Map<Integer, MapleStatEffect> itemEffects = new HashMap<Integer, MapleStatEffect>();
	protected Map<Integer, Map<String, Integer>> equipStatsCache = new HashMap<Integer, Map<String, Integer>>();
	protected Map<Integer, Equip> equipCache = new HashMap<Integer, Equip>();
	protected Map<Integer, Double> priceCache = new HashMap<Integer, Double>();
	protected Map<Integer, Integer> wholePriceCache = new HashMap<Integer, Integer>();
	protected Map<Integer, Integer> projectileWatkCache = new HashMap<Integer, Integer>();
	protected Map<Integer, Integer> scriptedItemCache = new HashMap<Integer, Integer>();
	protected Map<Integer, String> nameCache = new HashMap<Integer, String>();
	protected Map<Integer, String> descCache = new HashMap<Integer, String>();
	protected Map<Integer, String> msgCache = new HashMap<Integer, String>();
	protected Map<Integer, Boolean> dropRestrictionCache = new HashMap<Integer, Boolean>();
	protected Map<Integer, Boolean> pickupRestrictionCache = new HashMap<Integer, Boolean>();
	protected List<Pair<Integer, String>> itemNameCache = new ArrayList<Pair<Integer, String>>();
	protected Map<Integer, Integer> monsterBookID = new HashMap<Integer, Integer>();
	private static Random rand = new Random();
	

	/** Creates a new instance of MapleItemInformationProvider */
	protected MapleItemInformationProvider() {
		loadCardIdData();
		itemData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Item.wz"));
		equipData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Character.wz"));
		stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/String.wz"));
		cashStringData = stringData.getData("Cash.img");
		consumeStringData = stringData.getData("Consume.img");
		eqpStringData = stringData.getData("Eqp.img");
		etcStringData = stringData.getData("Etc.img");
		insStringData = stringData.getData("Ins.img");
		petStringData = stringData.getData("Pet.img");
	}

	public static MapleItemInformationProvider getInstance() {
		if (instance == null)
			instance = new MapleItemInformationProvider();
		return instance;
	}

	/* returns the inventory type for the specified item id */
	public MapleInventoryType getInventoryType(int itemId) {
		if (inventoryTypeCache.containsKey(itemId))
			return inventoryTypeCache.get(itemId);
		MapleInventoryType ret;
		String idStr = "0" + String.valueOf(itemId);
		// first look in items...
		MapleDataDirectoryEntry root = itemData.getRoot();
		for (MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
			// we should have .img files here beginning with the first 4 IID
			for (MapleDataFileEntry iFile : topDir.getFiles()) {
				if (iFile.getName().equals(idStr.substring(0, 4) + ".img")) {
					ret = MapleInventoryType.getByWZName(topDir.getName());
					inventoryTypeCache.put(itemId, ret);
					return ret;
				} else if (iFile.getName().equals(idStr.substring(1) + ".img")) {
					ret = MapleInventoryType.getByWZName(topDir.getName());
					inventoryTypeCache.put(itemId, ret);
					return ret;
				}
			}
		}
		// not found? maybe its equip...
		root = equipData.getRoot();
		for (MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
			for (MapleDataFileEntry iFile : topDir.getFiles()) {
				if (iFile.getName().equals(idStr + ".img")) {
					ret = MapleInventoryType.EQUIP;
					inventoryTypeCache.put(itemId, ret);
					return ret;
				}
			}
		}
		ret = MapleInventoryType.UNDEFINED;
		inventoryTypeCache.put(itemId, ret);
		return ret;
	}

	protected MapleData getStringData(int itemId) {
		String cat = "null";
		MapleData theData;
		if (itemId >= 5010000) {
			theData = cashStringData;
		} else if (itemId >= 2000000 && itemId < 3000000) {
			theData = consumeStringData;
		} else if (itemId >= 1010000 && itemId < 1040000 || itemId >= 1122000 && itemId < 1123000 || itemId >= 1142000 && itemId < 1150000) {
			theData = eqpStringData;
			cat = "Eqp/Accessory";
		} else if (itemId >= 1000000 && itemId < 1010000) {
			theData = eqpStringData;
			cat = "Eqp/Cap";
		} else if (itemId >= 1102000 && itemId < 1103000) {
			theData = eqpStringData;
			cat = "Eqp/Cape";
		} else if (itemId >= 1040000 && itemId < 1050000) {
			theData = eqpStringData;
			cat = "Eqp/Coat";
		} else if (itemId >= 20000 && itemId < 22000) {
			theData = eqpStringData;
			cat = "Eqp/Face";
		} else if (itemId >= 1080000 && itemId < 1090000) {
			theData = eqpStringData;
			cat = "Eqp/Glove";
		} else if (itemId >= 30000 && itemId < 32000) {
			theData = eqpStringData;
			cat = "Eqp/Hair";
		} else if (itemId >= 1050000 && itemId < 1060000) {
			theData = eqpStringData;
			cat = "Eqp/Longcoat";
		} else if (itemId >= 1060000 && itemId < 1070000) {
			theData = eqpStringData;
			cat = "Eqp/Pants";
		} else if (itemId >= 1802000 && itemId < 1810000) {
			theData = eqpStringData;
			cat = "Eqp/PetEquip";
		} else if (itemId >= 1112000 && itemId < 1120000) {
			theData = eqpStringData;
			cat = "Eqp/Ring";
		} else if (itemId >= 1092000 && itemId < 1100000) {
			theData = eqpStringData;
			cat = "Eqp/Shield";
		} else if (itemId >= 1070000 && itemId < 1080000) {
			theData = eqpStringData;
			cat = "Eqp/Shoes";
		} else if (itemId >= 1900000 && itemId < 2000000) {
			theData = eqpStringData;
			cat = "Eqp/Taming";
		} else if (itemId >= 1300000 && itemId < 1800000) {
			theData = eqpStringData;
			cat = "Eqp/Weapon";
		} else if (itemId >= 4000000 && itemId < 5000000) {
			theData = etcStringData;
		} else if (itemId >= 3000000 && itemId < 4000000) {
			theData = insStringData;
		} else if (itemId >= 5000000 && itemId < 5010000) {
			theData = petStringData;
		} else {
			return null;
		}

		if (cat.equalsIgnoreCase("null")) {
			return theData.getChildByPath(String.valueOf(itemId));
		} else {
			return theData.getChildByPath(cat + "/" + itemId);
		}
	}

	protected MapleData getItemData(int itemId) {
		MapleData ret = null;
		String idStr = "0" + String.valueOf(itemId);
		MapleDataDirectoryEntry root = itemData.getRoot();
		for (MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
			// we should have .img files here beginning with the first 4 IID
			for (MapleDataFileEntry iFile : topDir.getFiles()) {
				if (iFile.getName().equals(idStr.substring(0, 4) + ".img")) {
					ret = itemData.getData(topDir.getName() + "/" + iFile.getName());
					if (ret == null)
						return null;
					ret = ret.getChildByPath(idStr);
					return ret;
				} else if (iFile.getName().equals(idStr.substring(1) + ".img")) {
					return itemData.getData(topDir.getName() + "/" + iFile.getName());
				}
			}
		}
		root = equipData.getRoot();
		for (MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
			for (MapleDataFileEntry iFile : topDir.getFiles()) {
				if (iFile.getName().equals(idStr + ".img")) {
					return equipData.getData(topDir.getName() + "/" + iFile.getName());
				}
			}
		}
		return ret;
	}
	
	public int getScriptedItemNpc(int itemId) {
		if (scriptedItemCache.containsKey(itemId)) {
			return scriptedItemCache.get(itemId);
		}
		MapleData data = getItemData(itemId);
		int npcId = MapleDataTool.getInt("spec/npc", data, 0);
		scriptedItemCache.put(itemId, npcId);
		
		return scriptedItemCache.get(itemId);
	}

	/** returns the maximum of items in one slot */
	public short getSlotMax(int itemId) {
		if (slotMaxCache.containsKey(itemId))
			return slotMaxCache.get(itemId);
		short ret = 0;
		MapleData item = getItemData(itemId);
		if (item != null) {
			MapleData smEntry = item.getChildByPath("info/slotMax");
			if (smEntry == null) {
				if (getInventoryType(itemId).getType() == MapleInventoryType.EQUIP.getType())
					ret = 1;
				else
					ret = 100;
			} else {
				if (isThrowingStar(itemId) || isBullet(itemId))
					ret = 1;
				else if (MapleDataTool.getInt(smEntry) == 0)
					ret = 1;
				ret = (short) MapleDataTool.getInt(smEntry);
			}
		}
		slotMaxCache.put(itemId, ret);
		return ret;
	}
	
	public int getWholePrice(int itemId) {
		if (wholePriceCache.containsKey(itemId))
			return wholePriceCache.get(itemId);
		MapleData item = getItemData(itemId);
		if (item == null)
			return -1;
		
		int pEntry = 0;
		MapleData pData = item.getChildByPath("info/price");
		if (pData == null)
			return -1;
		pEntry = MapleDataTool.getInt(pData);
		
		wholePriceCache.put(itemId, pEntry);
		return pEntry;		
	}

	public double getPrice(int itemId) {
		if (priceCache.containsKey(itemId))
			return priceCache.get(itemId);
		MapleData item = getItemData(itemId);
		if (item == null)
			return -1;
		
		//TODO ULTRAHACK - prevent players gaining miriads of mesars with orbis/eos scrolls
		if (itemId == 4001019 || itemId == 4001020) {
			return 0;
		}
		
		double pEntry = 0.0;
		MapleData pData = item.getChildByPath("info/unitPrice");
		if (pData != null) {
			try {
				pEntry = MapleDataTool.getDouble(pData);
			} catch (Exception e) {
				pEntry = (double) MapleDataTool.getInt(pData);
			}
		} else {
			pData = item.getChildByPath("info/price");
			if (pData == null)
				return -1;
			pEntry = (double) MapleDataTool.getInt(pData);
		}
		
		priceCache.put(itemId, pEntry);
		return pEntry;
	}
	
	public boolean isCash(int itemId) {
		if (isCashCache.containsKey(itemId))
			return isCashCache.get(itemId);
		boolean ret = false;
		MapleData item = getItemData(itemId);
		if (item != null) {
			MapleData smEntry = item.getChildByPath("info/cash");
			if (smEntry != null) {
				if (MapleDataTool.getInt(smEntry) == 1)
					ret = true;
			}
		}
		isCashCache.put(itemId, ret);
		return ret;
	}

	protected Map<String, Integer> getEquipStats(int itemId) {
		if (equipStatsCache.containsKey(itemId))
			return equipStatsCache.get(itemId);
		Map<String, Integer> ret = new LinkedHashMap<String, Integer>();
		MapleData item = getItemData(itemId);
		if (item == null)
			return null;
		MapleData info = item.getChildByPath("info");
		if (info == null)
			return null;
		for (MapleData data : info.getChildren()) {
			if (data.getName().startsWith("inc")) {
				ret.put(data.getName().substring(3), MapleDataTool.getIntConvert(data));
			}
		}
		ret.put("tuc", MapleDataTool.getInt("tuc", info, 0));
		ret.put("reqLevel", MapleDataTool.getInt("reqLevel", info, 0));
		ret.put("cursed", MapleDataTool.getInt("cursed", info, 0));
		ret.put("success", MapleDataTool.getInt("success", info, 0));
		equipStatsCache.put(itemId, ret);
		return ret;
	}
	
	public int getReqLevel (int itemId) {
		final Integer req = getEquipStats(itemId).get("reqLevel");
		return req == null ? 0 : req.intValue();
	}

	public List<Integer> getScrollReqs(int itemId) { 
		List<Integer> ret = new ArrayList<Integer>(); 
		MapleData data = getItemData(itemId); 
		data = data.getChildByPath("req"); 
		if (data == null) { 
			return ret; 
		} 
		for (MapleData req : data.getChildren()) { 
			ret.add(MapleDataTool.getInt(req)); 
		} 
		return ret; 
	} 

	public MapleWeaponType getWeaponType (int itemId) {
		int cat = itemId / 10000;
		cat = cat % 100;
		switch (cat) {
			case 30:
				return MapleWeaponType.SWORD1H;
			case 31:
				return MapleWeaponType.AXE1H;
			case 32:
				return MapleWeaponType.BLUNT1H;
			case 33:
				return MapleWeaponType.DAGGER;
			case 37:
				return MapleWeaponType.WAND;
			case 38:
				return MapleWeaponType.STAFF;
			case 40:
				return MapleWeaponType.SWORD2H;
			case 41:
				return MapleWeaponType.AXE2H;
			case 42:
				return MapleWeaponType.BLUNT2H;
			case 43:
				return MapleWeaponType.SPEAR;
			case 44:
				return MapleWeaponType.POLE_ARM;
			case 45:
				return MapleWeaponType.BOW;
			case 46:
				return MapleWeaponType.CROSSBOW;
			case 47:
				return MapleWeaponType.CLAW;
			case 48:
				return MapleWeaponType.KNUCKLE;
			case 49:
				return MapleWeaponType.GUN;

		}
		return MapleWeaponType.NOT_A_WEAPON;
	}
	
	public boolean isShield(int itemId) {
		int cat = itemId / 10000;
		cat = cat % 100;
		return cat == 9;
	}
	
	public boolean isEquip (int itemId) {
		return itemId / 1000000 == 1;
	}
	
	public boolean isCleanSlate(int scrollId) {
		switch (scrollId) {
			case 2049000:
			case 2049001:
			case 2049002:
			case 2049003:
				return true;
		}
		return false;
	}
	
	public boolean isFlagScroll(int scrollId) {
		switch (scrollId) {
			case 2040727:
			case 2041058:
				return true;
		}
		return false;
	}
	
    public IItem scrollEquipWithId(IItem equip, int scrollId, boolean usingWhiteScroll) {
        if (equip instanceof Equip) {
            Equip nEquip = (Equip) equip;
            Map<String, Integer> stats = this.getEquipStats(scrollId);
            Map<String, Integer> eqstats = this.getEquipStats(equip.getItemId());
            if ((nEquip.getUpgradeSlots() > 0 || isCleanSlate(scrollId)) && Math.ceil(Math.random() * 100.0) <= stats.get("success")) {
                byte flag = nEquip.getFlag();
                switch (scrollId) {
                    case 2040727:
                        flag |= ItemFlag.SPIKES.getValue();
                        nEquip.setFlag(flag);
                        break;
                    case 2041058:
                        flag |= ItemFlag.COLD.getValue();
                        nEquip.setFlag(flag);
                        break;
                    case 2049000:
                    case 2049001:
                    case 2049002:
                    case 2049003:
                        if (nEquip.getLevel() + nEquip.getUpgradeSlots() < eqstats.get("tuc")) {
                            byte newSlots = (byte) (nEquip.getUpgradeSlots() + 1);
                            nEquip.setUpgradeSlots(newSlots);
                        }
                        break;
                    case 2049100:
                    case 2049101:
                    case 2049102:
                        int increase = 1;
                        if (Math.ceil(Math.random() * 100.0) <= 50) {
                            increase = increase * -1;
                        }
                        if (nEquip.getStr() > 0) {
                            short newStat = (short) (nEquip.getStr() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setStr(newStat);
                        }
                        if (nEquip.getDex() > 0) {
                            short newStat = (short) (nEquip.getDex() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setDex(newStat);
                        }
                        if (nEquip.getInt() > 0) {
                            short newStat = (short) (nEquip.getInt() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setInt(newStat);
                        }
                        if (nEquip.getLuk() > 0) {
                            short newStat = (short) (nEquip.getLuk() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setLuk(newStat);
                        }
                        if (nEquip.getWatk() > 0) {
                            short newStat = (short) (nEquip.getWatk() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setWatk(newStat);
                        }
                        if (nEquip.getWdef() > 0) {
                            short newStat = (short) (nEquip.getWdef() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setWdef(newStat);
                        }
                        if (nEquip.getMatk() > 0) {
                            short newStat = (short) (nEquip.getMatk() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setMatk(newStat);
                        }
                        if (nEquip.getMdef() > 0) {
                            short newStat = (short) (nEquip.getMdef() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setMdef(newStat);
                        }
                        if (nEquip.getAcc() > 0) {
                            short newStat = (short) (nEquip.getAcc() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setAcc(newStat);
                        }
                        if (nEquip.getAvoid() > 0) {
                            short newStat = (short) (nEquip.getAvoid() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setAvoid(newStat);
                        }
                        if (nEquip.getSpeed() > 0) {
                            short newStat = (short) (nEquip.getSpeed() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setSpeed(newStat);
                        }
                        if (nEquip.getJump() > 0) {
                            short newStat = (short) (nEquip.getJump() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setJump(newStat);
                        }
                        if (nEquip.getHp() > 0) {
                            short newStat = (short) (nEquip.getHp() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setHp(newStat);
                        }
                        if (nEquip.getMp() > 0) {
                            short newStat = (short) (nEquip.getMp() + Math.ceil(Math.random() * 5.0) * increase);
                            nEquip.setMp(newStat);
                        }
                        break;
                    default:
                        for (Entry<String, Integer> stat : stats.entrySet()) {
                            if (stat.getKey().equals("STR")) {
                                nEquip.setStr((short) (nEquip.getStr() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("DEX")) {
                                nEquip.setDex((short) (nEquip.getDex() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("INT")) {
                                nEquip.setInt((short) (nEquip.getInt() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("LUK")) {
                                nEquip.setLuk((short) (nEquip.getLuk() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("PAD")) {
                                nEquip.setWatk((short) (nEquip.getWatk() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("PDD")) {
                                nEquip.setWdef((short) (nEquip.getWdef() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("MAD")) {
                                nEquip.setMatk((short) (nEquip.getMatk() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("MDD")) {
                                nEquip.setMdef((short) (nEquip.getMdef() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("ACC")) {
                                nEquip.setAcc((short) (nEquip.getAcc() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("EVA")) {
                                nEquip.setAvoid((short) (nEquip.getAvoid() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("Speed")) {
                                nEquip.setSpeed((short) (nEquip.getSpeed() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("Jump")) {
                                nEquip.setJump((short) (nEquip.getJump() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("MHP")) {
                                nEquip.setHp((short) (nEquip.getHp() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("MMP")) {
                                nEquip.setMp((short) (nEquip.getMp() + stat.getValue().intValue()));
                            } else if (stat.getKey().equals("afterImage")) {
                            }
                        }
                        break;
                }
                if (!isCleanSlate(scrollId) && !isFlagScroll(scrollId)) {
                    nEquip.setUpgradeSlots((byte) (nEquip.getUpgradeSlots() - 1));
                    nEquip.setLevel((byte) (nEquip.getLevel() + 1));
                }
            } else {
                if (!usingWhiteScroll && !isCleanSlate(scrollId) && !isFlagScroll(scrollId)) {
                    nEquip.setUpgradeSlots((byte) (nEquip.getUpgradeSlots() - 1));
                }
                if (Math.ceil(1.0 + Math.random() * 100.0) < stats.get("cursed")) {
                    // DESTROY :) (O.O!)
                    return null;
                }
            }
        }
        return equip;
    }

	public IItem getEquipById(int equipId) {
		return getEquipById(equipId, -1);
	}
	
	public IItem getEquipById(int equipId, int ringId) {
		Equip nEquip;
		nEquip = new Equip(equipId, (byte) 0, ringId);
		nEquip.setQuantity((short) 1);
		Map<String, Integer> stats = this.getEquipStats(equipId);
		if (stats != null) {
			for (Entry<String, Integer> stat : stats.entrySet()) {
				if (stat.getKey().equals("STR"))
					nEquip.setStr((short) stat.getValue().intValue());
				else if (stat.getKey().equals("DEX"))
					nEquip.setDex((short) stat.getValue().intValue());
				else if (stat.getKey().equals("INT"))
					nEquip.setInt((short) stat.getValue().intValue());
				else if (stat.getKey().equals("LUK"))
					nEquip.setLuk((short) stat.getValue().intValue());
				else if (stat.getKey().equals("PAD"))
					nEquip.setWatk((short) stat.getValue().intValue());
				else if (stat.getKey().equals("PDD"))
					nEquip.setWdef((short) stat.getValue().intValue());
				else if (stat.getKey().equals("MAD"))
					nEquip.setMatk((short) stat.getValue().intValue());
				else if (stat.getKey().equals("MDD"))
					nEquip.setMdef((short) stat.getValue().intValue());
				else if (stat.getKey().equals("ACC"))
					nEquip.setAcc((short) stat.getValue().intValue());
				else if (stat.getKey().equals("EVA"))
					nEquip.setAvoid((short) stat.getValue().intValue());
				else if (stat.getKey().equals("Speed"))
					nEquip.setSpeed((short) stat.getValue().intValue());
				else if (stat.getKey().equals("Jump"))
					nEquip.setJump((short) stat.getValue().intValue());
				else if (stat.getKey().equals("MHP"))
					nEquip.setHp((short) stat.getValue().intValue());
				else if (stat.getKey().equals("MMP"))
					nEquip.setMp((short) stat.getValue().intValue());
				else if (stat.getKey().equals("tuc"))
					nEquip.setUpgradeSlots((byte) stat.getValue().intValue());
				else if (stat.getKey().equals("afterImage")) {
				}
			}
		}
		equipCache.put(equipId, nEquip);
		return nEquip.copy();
	}
	
	private short getRandStat(short defaultValue, int maxRange) {
		if (defaultValue == 0) {
			return 0;
		}

		// vary no more than ceil of 10% of stat
		int lMaxRange = (int) Math.min(Math.ceil(defaultValue * 0.1), maxRange);
		return (short) ((defaultValue - lMaxRange) + Math.floor(rand.nextDouble() * (lMaxRange * 2 + 1)));
	}
	
	public Equip randomizeStats(Equip equip) {
		equip.setStr(getRandStat(equip.getStr(), 5));
		equip.setDex(getRandStat(equip.getDex(), 5));
		equip.setInt(getRandStat(equip.getInt(), 5));
		equip.setLuk(getRandStat(equip.getLuk(), 5));
		equip.setMatk(getRandStat(equip.getMatk(), 5));
		equip.setWatk(getRandStat(equip.getWatk(), 5));
		equip.setAcc(getRandStat(equip.getAcc(), 5));
		equip.setAvoid(getRandStat(equip.getAvoid(), 5));
		equip.setJump(getRandStat(equip.getJump(), 5));
		equip.setSpeed(getRandStat(equip.getSpeed(), 5));
		equip.setWdef(getRandStat(equip.getWdef(), 10));
		equip.setMdef(getRandStat(equip.getMdef(), 10));
		equip.setHp(getRandStat(equip.getHp(), 10));
		equip.setMp(getRandStat(equip.getMp(), 10));
		return equip;
	}

	public MapleStatEffect getItemEffect(int itemId) {
		MapleStatEffect ret = itemEffects.get(Integer.valueOf(itemId));
		if (ret == null) {
			MapleData item = getItemData(itemId);
			if (item == null)
				return null;
			MapleData spec = item.getChildByPath("spec");
			ret = MapleStatEffect.loadItemEffectFromData(spec, itemId);
			itemEffects.put(Integer.valueOf(itemId), ret);
		}
		return ret;
	}

	public int[][] getSummonMobs(int itemId) {
		MapleData data = getItemData(itemId);
		int theInt = data.getChildByPath("mob").getChildren().size();
		int[][] mobs2spawn = new int[theInt][2];
		for(int x = 0; x < theInt; x++){
			mobs2spawn[x][0] = MapleDataTool.getIntConvert("mob/" + x + "/id", data);
			mobs2spawn[x][1] = MapleDataTool.getIntConvert("mob/" + x + "/prob", data);  
		}
		return mobs2spawn;
	}

	public boolean isThrowingStar(int itemId) {
		return (itemId >= 2070000 && itemId < 2080000);
	}
	
	public boolean isBullet(int itemId) {
		int id = itemId/10000;
		if (id == 233)
			return true;
		else
			return false;
	}
	
	public boolean isRechargable(int itemId) {
		int id = itemId/10000;
		if (id == 233 || id == 207)
			return true;
		else
			return false;
	}
	
	public boolean isOverall(int itemId) {
		return itemId >= 1050000 && itemId < 1060000;
	}

	public boolean isArrowForCrossBow(int itemId) {
		return itemId >= 2061000 && itemId < 2062000;
	}

	public boolean isArrowForBow(int itemId) {
		return itemId >= 2060000 && itemId < 2061000;
	}
	
	public boolean isTwoHanded(int itemId) {
		switch (getWeaponType(itemId)) {
			case AXE2H:
				return true;
			case BLUNT2H:
				return true;
			case BOW:
				return true;
			case CLAW:
				return true;
			case CROSSBOW:
				return true;
			case POLE_ARM:
				return true;
			case SPEAR:
				return true;
			case SWORD2H:
				return true;
			default:
				return false;
		}
	}
	
	public int getWatkForProjectile(int itemId) {
		Integer atk = projectileWatkCache.get(itemId);
		if (atk != null) {
			return atk.intValue();
		}
		MapleData data = getItemData(itemId);
		atk = Integer.valueOf(MapleDataTool.getInt("info/incPAD", data, 0));
		projectileWatkCache.put(itemId, atk);
		return atk.intValue();
	}
	
	public boolean canScroll (int scrollid, int itemid) {
		int scrollCategoryQualifier = (scrollid / 100) % 100;
		int itemCategoryQualifier = (itemid / 10000) % 100;
		return scrollCategoryQualifier == itemCategoryQualifier;
	}
	
	public String getName(int itemId) {
		if (nameCache.containsKey(itemId))
			return nameCache.get(itemId);
		MapleData strings = getStringData(itemId);
		if (strings == null) return null;
		String ret = MapleDataTool.getString("name", strings, null);
		nameCache.put(itemId, ret);
		return ret;
	}
	
	public String getDesc(int itemId) {
		if (descCache.containsKey(itemId))
			return descCache.get(itemId);
		MapleData strings = getStringData(itemId);
		if (strings == null) return null;
		String ret = MapleDataTool.getString("desc", strings, null);
		descCache.put(itemId, ret);
		return ret;
	}
	
	public String getMsg(int itemId) {
		if (msgCache.containsKey(itemId))
			return msgCache.get(itemId);
		MapleData strings = getStringData(itemId);
		if (strings == null) return null;
		String ret = MapleDataTool.getString("msg", strings, null);
		msgCache.put(itemId, ret);
		return ret;
	}
	
	public boolean isDropRestricted(int itemId) {
		if (dropRestrictionCache.containsKey(itemId))
			return dropRestrictionCache.get(itemId);
		
		MapleData data = getItemData(itemId);
		
		boolean bRestricted = MapleDataTool.getIntConvert("info/tradeBlock", data, 0) == 1;
		if (!bRestricted)
			bRestricted = MapleDataTool.getIntConvert("info/quest", data, 0) == 1;
		dropRestrictionCache.put(itemId, bRestricted);
		
		return bRestricted;
	}
	
	public boolean isPickupRestricted(int itemId) {
		if (pickupRestrictionCache.containsKey(itemId))
			return pickupRestrictionCache.get(itemId);
		
		MapleData data = getItemData(itemId);
		boolean bRestricted = MapleDataTool.getIntConvert("info/only", data, 0) == 1;
		
		pickupRestrictionCache.put(itemId, bRestricted);
		return bRestricted;
	}
	
	private Map<Integer, Map<String, Integer>> skillStats = new LinkedHashMap<Integer, Map<String, Integer>>();
	private Map<Integer, List<Integer>> skillStatSkills = new LinkedHashMap<Integer, List<Integer>>();
	
	public Map<String, Integer> getSkillStats(int itemId, double playerJob) {
		if (skillStats.containsKey(itemId)) {
			Map<String, Integer> ret = skillStats.get(itemId);
			for (int skill : skillStatSkills.get(itemId)) {
				double skillJob = Math.floor(skill / 10000);
				if(skillJob == playerJob) {
					ret.put("skillid", skill);
					break;
				}
			}
			return ret;
		}
		Map<String, Integer> ret = new LinkedHashMap<String, Integer>();
		Map<String, Integer> toAdd = new LinkedHashMap<String, Integer>();
		List<Integer> toAdd2 = new ArrayList<Integer>();
		
		MapleData item = getItemData(itemId);
		if (item == null)
			return null;
		
		MapleData info = item.getChildByPath("info");
		if (info == null)
			return null;
		
		for (MapleData data : info.getChildren()) {
			if (data.getName().startsWith("inc")) {
				ret.put(data.getName().substring(3), MapleDataTool.getIntConvert(data));
				toAdd.put(data.getName().substring(3), MapleDataTool.getIntConvert(data));
			}
		}
		
		ret.put("masterLevel", MapleDataTool.getInt("masterLevel", info, 0));
		ret.put("reqSkillLevel", MapleDataTool.getInt("reqSkillLevel", info, 0));
		ret.put("success", MapleDataTool.getInt("success", info, 0));
		toAdd.put("masterLevel", MapleDataTool.getInt("masterLevel", info, 0));
		toAdd.put("reqSkillLevel", MapleDataTool.getInt("reqSkillLevel", info, 0));
		toAdd.put("success", MapleDataTool.getInt("success", info, 0));
		
		for (MapleData curSkill : info.getChildByPath("skill")) {
			int curskill = MapleDataTool.getInt(curSkill, 0);
			if(curskill == 0) // end - no more;
				break;
			
			toAdd2.add(curskill);
			double skillJob = Math.floor(curskill / 10000);
			if(skillJob == playerJob) {
				ret.put("skillid", curskill);
				break;
			}
		}
		
		if(ret.get("skillid") == null)
			ret.put("skillid", 0);		
		
		skillStats.put(itemId, toAdd);
		skillStatSkills.put(itemId, toAdd2);
		
		return ret;
	}
	
	public List<Integer> petsCanConsume(int itemId) {
		List<Integer> ret = new ArrayList<Integer>();
		MapleData data = getItemData(itemId);
		int curPetId = 0;
		for (int i = 0; i < 100; i++) {
			curPetId = MapleDataTool.getInt("spec/" + Integer.toString(i), data, 0);
			if (curPetId == 0) {
				break;
			}
			ret.add(Integer.valueOf(curPetId));
		}
		return ret;
	}
	
	public boolean isMiniDungeonMap(int mapId) {
	    switch(mapId) {
		case 100020000:
		case 105040304:
		case 105050100:
		case 221023400:
		    return true;
		default:
		    return false;
	    }
	}
	
    private void loadCardIdData() {
        PreparedStatement ps = null;
        ResultSet rs = null;
		try {
		    Connection con = DatabaseConnection.getConnection();
		    ps = con.prepareStatement("SELECT cardid, mobid FROM monstercarddata");
		    rs = ps.executeQuery();
	
		    while (rs.next()) {
	                monsterBookID.put(rs.getInt(1), rs.getInt(2));
		    }
		} catch (SQLException e) {
		    System.err.println("Error loading monstercardIDs" + e);
		} finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing monstercardIDs" + e);
            }
        }
    }
    
    public List<Pair<Integer, String>> getAllItems() {
		if (itemNameCache.size() != 0) {
			return itemNameCache;
		}
		List<Pair<Integer, String>> itemPairs = new ArrayList<Pair<Integer, String>>();
		MapleData itemsData;

		itemsData = stringData.getData("Cash.img");
		for (MapleData itemFolder : itemsData.getChildren()) {
			int itemId = Integer.parseInt(itemFolder.getName());
			String itemName = MapleDataTool.getString("name", itemFolder, "NO-NAME");
			itemPairs.add(new Pair<Integer, String>(itemId, itemName));
		}

		itemsData = stringData.getData("Consume.img");
		for (MapleData itemFolder : itemsData.getChildren()) {
			int itemId = Integer.parseInt(itemFolder.getName());
			String itemName = MapleDataTool.getString("name", itemFolder, "NO-NAME");
			itemPairs.add(new Pair<Integer, String>(itemId, itemName));
		}

		itemsData = stringData.getData("Eqp.img").getChildByPath("Eqp");
		for (MapleData eqpType : itemsData.getChildren()) {
			for (MapleData itemFolder : eqpType.getChildren()) {
				int itemId = Integer.parseInt(itemFolder.getName());
				String itemName = MapleDataTool.getString("name", itemFolder, "NO-NAME");
				itemPairs.add(new Pair<Integer, String>(itemId, itemName));
			}
		}

		itemsData = stringData.getData("Etc.img").getChildByPath("Etc");
		for (MapleData itemFolder : itemsData.getChildren()) {
			int itemId = Integer.parseInt(itemFolder.getName());
			String itemName = MapleDataTool.getString("name", itemFolder, "NO-NAME");
			itemPairs.add(new Pair<Integer, String>(itemId, itemName));
		}

		itemsData = stringData.getData("Ins.img");
		for (MapleData itemFolder : itemsData.getChildren()) {
			int itemId = Integer.parseInt(itemFolder.getName());
			String itemName = MapleDataTool.getString("name", itemFolder, "NO-NAME");
			itemPairs.add(new Pair<Integer, String>(itemId, itemName));
		}

		itemsData = stringData.getData("Pet.img");
		for (MapleData itemFolder : itemsData.getChildren()) {
			int itemId = Integer.parseInt(itemFolder.getName());
			String itemName = MapleDataTool.getString("name", itemFolder, "NO-NAME");
			itemPairs.add(new Pair<Integer, String>(itemId, itemName));
		}
		itemNameCache = itemPairs;
		return itemPairs;
	}

    public boolean isMonsterCard(int id) {
    	return id / 10000 == 238;
    }

    public boolean isSpecialCard(int id) {
    	return id / 100 >= 2388;
    }

    public int getCardMobId(int id) {
    	return monsterBookID.get(id);
    }

    public int getCardShortId(int id) {
    	return id % 10000;
    }
}
