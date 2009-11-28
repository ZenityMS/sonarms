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

package net.sf.odinms.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.odinms.tools.HexTool;

import net.sf.odinms.client.IEquip;
import net.sf.odinms.client.IItem;
import net.sf.odinms.client.ISkill;
import net.sf.odinms.client.Item;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleInventory;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MapleJob;
import net.sf.odinms.client.MaplePet;
import net.sf.odinms.client.MapleQuestStatus;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.tools.data.output.MaplePacketLittleEndianWriter;

public class MaplePacketHelper {
	private final static byte[] ITEM_MAGIC = new byte[] { (byte) 0x80, 5 };
	private final static long FT_UT_OFFSET = 116444592000000000L;

	//credit goes to Simon for this function, copied (with slight variation) out of his tempban
	//convert to EDT
	public static long getKoreanTimestamp(long realTimestamp) {
		long time = (realTimestamp / 1000 / 60); //convert to minutes
		return ((time * 600000000) + FT_UT_OFFSET);
	}

    public static long getTime(long realTimestamp) {
        long time = (realTimestamp / 1000); // convert to seconds
        return ((time * 10000000) + FT_UT_OFFSET);
    }
    
    /**
	 * Adds character stats to an existing MaplePacketLittleEndianWriter.
	 * 
	 * @param mplew The MaplePacketLittleEndianWrite instance to write the stats
	 *            to.
	 * @param chr The character to add the stats of.
	 */
	public static void addCharStats(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
		mplew.writeInt(chr.getId()); // character id
		mplew.writeAsciiString(chr.getName());
		for (int x = chr.getName().length(); x < 13; x++) { // fill to maximum
			// name length
			mplew.write(0);
		}
		
		mplew.write(chr.getGender()); // gender (0 = male, 1 = female)
        mplew.write(chr.getSkinColor().getId()); // skin color
        mplew.writeInt(chr.getFace()); // face
        mplew.writeInt(chr.getHair()); // hair
        mplew.writeLong(0);
        mplew.writeLong(0);
        mplew.writeLong(0);
        mplew.write(chr.getLevel()); // level
        mplew.writeShort(chr.getJob().getId()); // job
        mplew.writeShort(chr.getStr()); // str
        mplew.writeShort(chr.getDex()); // dex
        mplew.writeShort(chr.getInt()); // int
        mplew.writeShort(chr.getLuk()); // luk
        mplew.writeShort(chr.getHp()); // hp (?)
        mplew.writeShort(chr.getMaxHp()); // maxhp
        mplew.writeShort(chr.getMp()); // mp (?)
        mplew.writeShort(chr.getMaxMp()); // maxmp
        mplew.writeShort(chr.getRemainingAp()); // remaining ap
        mplew.writeShort(chr.getRemainingSp()); // remaining sp
        mplew.writeInt(chr.getExp()); // current exp
        mplew.writeShort(chr.getFame()); // fame
        mplew.writeInt(0); //Gachapon EXP (thx Diamondo25)
        mplew.writeInt(chr.getMapId()); // current map id
        mplew.write(chr.getInitialSpawnpoint()); // spawnpoint
        mplew.writeInt(0);
	}
	
	/**
	 * Adds the aesthetic aspects of a character to an existing
	 * MaplePacketLittleEndianWriter.
	 * 
	 * @param mplew The MaplePacketLittleEndianWrite instance to write the stats
	 *            to.
	 * @param chr The character to add the looks of.
	 * @param mega Unknown
	 */
	public static void addCharLook(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean mega) {
		mplew.write(chr.getGender());
		mplew.write(chr.getSkinColor().getId()); // skin color

		mplew.writeInt(chr.getFace()); // face
		// variable length

		mplew.write(mega ? 0 : 1);
		mplew.writeInt(chr.getHair()); // hair

		MapleInventory equip = chr.getInventory(MapleInventoryType.EQUIPPED);
		// Map<Integer, Integer> equipped = new LinkedHashMap<Integer,
		// Integer>();
		Map<Byte, Integer> myEquip = new LinkedHashMap<Byte, Integer>();
		Map<Byte, Integer> maskedEquip = new LinkedHashMap<Byte, Integer>();
		for (IItem item : equip.list()) {
			byte pos = (byte) (item.getPosition() * -1);
			if (pos < 100 && myEquip.get(pos) == null) {
				myEquip.put(pos, item.getItemId());
			} else if (pos > 100 && pos != 111) { // don't ask. o.o

				pos -= 100;
				if (myEquip.get(pos) != null) {
					maskedEquip.put(pos, myEquip.get(pos));
				}
				myEquip.put(pos, item.getItemId());
			} else if (myEquip.get(pos) != null) {
				maskedEquip.put(pos, item.getItemId());
			}
		}
		for (Entry<Byte, Integer> entry : myEquip.entrySet()) {
			mplew.write(entry.getKey());
			mplew.writeInt(entry.getValue());
		}
		mplew.write(0xFF); // end of visible itens
		// masked itens
		
		for (Entry<Byte, Integer> entry : maskedEquip.entrySet()) {
			mplew.write(entry.getKey());
			mplew.writeInt(entry.getValue());
		}
		/*
		 * for (IItem item : equip.list()) { byte pos = (byte)(item.getPosition() * -1); if (pos > 100) {
		 * mplew.write(pos - 100); mplew.writeInt(item.getItemId()); } }
		 */
		// ending markers
		mplew.write(0xFF);
		IItem cWeapon = equip.getItem((byte) -111);
		if (cWeapon != null) {
			mplew.writeInt(cWeapon.getItemId());
		} else {
			mplew.writeInt(0); // cashweapon

		}
		mplew.writeInt(0);
		
		// 0.54 adds more crazy shit
		mplew.writeInt(0);
		mplew.writeInt(0);
	}

	/**
	 * Adds an entry for a character to an existing
	 * MaplePacketLittleEndianWriter.
	 * 
	 * @param mplew The MaplePacketLittleEndianWrite instance to write the stats
	 *            to.
	 * @param chr The character to add.
	 */
	public static void addCharEntry(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
		addCharStats(mplew, chr);
		addCharLook(mplew, chr, false);
		if (chr.getJob().isA(MapleJob.GM)) {
			mplew.write(0);
			return;
		}
		mplew.write(1); // world rank enabled (next 4 ints are not sent if disabled)
		mplew.writeInt(chr.getRank()); // world rank
		mplew.writeInt(chr.getRankMove()); // move (negative is downwards)
		mplew.writeInt(chr.getJobRank()); // job rank
		mplew.writeInt(chr.getJobRankMove()); // move (negative is downwards)

	}

	/**
	 * Adds a quest info entry for a character to an existing
	 * MaplePacketLittleEndianWriter.
	 * 
	 * @param mplew The MaplePacketLittleEndianWrite instance to write the stats
	 *            to.
	 * @param chr The character to add quest info about.
	 */
	public static void addQuestInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
		mplew.writeShort(0);
		List<MapleQuestStatus> started = chr.getStartedQuests();
		mplew.writeShort(started.size());
		for (MapleQuestStatus q : started) {
			mplew.writeInt(q.getQuest().getId());
		}
		List<MapleQuestStatus> completed = chr.getCompletedQuests();
		mplew.writeShort(completed.size());
		for (MapleQuestStatus q : completed) {
			mplew.writeShort(q.getQuest().getId());
			// maybe start time? no effect.
			mplew.writeInt(KoreanDateUtil.getQuestTimestamp(q.getCompletionTime()));
			// completion time - don't ask about the time format
			mplew.writeInt(KoreanDateUtil.getQuestTimestamp(q.getCompletionTime()));
		}
	}
	
	/**
	 * Adds info about an item to an existing MaplePacketLittleEndianWriter.
	 * 
	 * @param mplew The MaplePacketLittleEndianWriter to write to.
	 * @param item The item to write info about.
	 */
	public static void addItemInfo(MaplePacketLittleEndianWriter mplew, IItem item) {
		addItemInfo(mplew, item, false, false);
	}

	/**
	 * Adds expiration time info to an existing MaplePacketLittleEndianWriter.
	 * 
	 * @param mplew The MaplePacketLittleEndianWriter to write to.
	 * @param time The expiration time.
	 * @param showexpirationtime Show the expiration time?
	 */
	public static void addExpirationTime(MaplePacketLittleEndianWriter mplew, long time, boolean showexpirationtime) {
		mplew.writeInt(KoreanDateUtil.getItemTimestamp(time));
		mplew.write(showexpirationtime ? 1 : 2);
	}

	/**
	 * Adds item info to existing MaplePacketLittleEndianWriter.
	 * 
	 * @param mplew The MaplePacketLittleEndianWriter to write to.
	 * @param item The item to add info about.
	 * @param zeroPosition Is the position zero?
	 * @param leaveOut Leave out the item if position is zero?
	 */
	public static void addItemInfo(MaplePacketLittleEndianWriter mplew, IItem item, boolean zeroPosition,
			boolean leaveOut) {
		MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
		boolean ring = false;
		IEquip equip = null;
		if (item.getType() == IItem.EQUIP) {
			equip = (IEquip) item;
			if (equip.getRingId() > -1) {
				ring = true;
			}
		}
		byte pos = item.getPosition();
		boolean masking = false;
		@SuppressWarnings("unused")
		boolean equipped = false;
		if (zeroPosition) {
			if (!leaveOut) {
				mplew.write(0);
			}
		} else if (pos <= (byte) -1) {
			pos *= -1;
			if (pos > 100 || ring) {
				masking = true;
				mplew.write(0);
				mplew.write(pos - 100);
			} else {
				mplew.write(pos);
			}
			equipped = true;
		} else {
			mplew.write(item.getPosition());
		}

		if (item.getPetId() > -1) {
			mplew.write(3);
		} else {
			mplew.write(item.getType());
		}
		
		mplew.writeInt(item.getItemId());
		//1A 00 
		//00 02 03 
		//01 
		//0A 
		//00 00 01 0A 
		//00 01 
		//75 4B 0F 00 
		//00 00 
		//80 05 
		//BB 46 E6 17 02 
		//0A 00 0F 00 10 00 10 00 10 00 00 00 00 00 00 00 00 00 93 00 94 00 15 00 14 00 00 00 00 00 00 00 00 00 10 00 00 00 00 00 00 00 00 00 00 00 5B 07 00 00 5D 00 00 08 00 40 E0 FD 3B 37 4F 01 
		//FF FF FF FF
		if (ring) {
			mplew.write(1);
			mplew.writeInt(equip.getRingId());
			mplew.writeInt(0);
		}
		
		if (item.getPetId() > -1) {
			MaplePet pet = MaplePet.loadFromDb(item.getItemId(), item.getPosition(), item.getPetId());
			String petname = pet.getName();
			mplew.write(1);
			mplew.writeInt(item.getPetId());
			mplew.writeInt(0);
			mplew.write(0);
			mplew.write(ITEM_MAGIC);
			mplew.write(HexTool.getByteArrayFromHexString("BB 46 E6 17 02"));
			if (petname.length() > 13) {
				petname = petname.substring(0, 13);
			}
			mplew.writeAsciiString(petname);
			for (int i = petname.length(); i < 13; i++) {
				mplew.write(0);
			}
			mplew.write(pet.getLevel());
			mplew.writeShort(pet.getCloseness());
			mplew.write(pet.getFullness());

			mplew.writeLong(MaplePacketHelper.getKoreanTimestamp((long) (System.currentTimeMillis() * 1.2)));
			mplew.writeInt(0);
			mplew.write(HexTool.getByteArrayFromHexString("50 46 00 00")); //wonder what this is
			return;
		}

		if (masking && !ring) {
			// 07.03.2008 06:49... o.o
			mplew.write(HexTool.getByteArrayFromHexString("01 FA 96 C1 00 00 00 00 00 C0 1E CC"));
			addExpirationTime(mplew, 0, false);
		} else if (ring) {
			mplew.writeLong(MaplePacketHelper.getKoreanTimestamp((long) (System.currentTimeMillis() * 1.2)));
		} else {
			mplew.writeShort(0);
			mplew.write(ITEM_MAGIC);
			addExpirationTime(mplew, 0, false);
		}

		if (item.getType() == IItem.EQUIP) {
			//00 01 01 
			//EC 4A 0F 00 
			//01 
			//FA 96 C1 00 
			//00 00 00 00 
			//C0 1E CC C5 A4 73 CA 01 
			//00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
			//5B 00 64 9A 50 00 E8 80 01 00 B0 80 BD E1 EB 2C CA 01 FF FF FF FF
			
			//00 01 01 
			//EC 4A 0F 00 
			//01 
			//FA 96 C1 00 
			//00 00 00 00 
			//C0 1E CC 42 E7 B1 9D 02 
			//00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 
			//5B 00 64 9A 50 00 E8 80 01 00 B0 80 BD E1 EB 2C CA 01 FF FF FF FF
			
			mplew.write(equip.getUpgradeSlots());
            mplew.write(equip.getLevel());
            mplew.writeShort(equip.getStr()); // str
            mplew.writeShort(equip.getDex()); // dex
            mplew.writeShort(equip.getInt()); // int
            mplew.writeShort(equip.getLuk()); // luk
            mplew.writeShort(equip.getHp()); // hp
            mplew.writeShort(equip.getMp()); // mp
            mplew.writeShort(equip.getWatk()); // watk
            mplew.writeShort(equip.getMatk()); // matk
            mplew.writeShort(equip.getWdef()); // wdef
            mplew.writeShort(equip.getMdef()); // mdef
            mplew.writeShort(equip.getAcc()); // accuracy
            mplew.writeShort(equip.getAvoid()); // avoid
            mplew.writeShort(equip.getHands()); // hands
            mplew.writeShort(equip.getSpeed()); // speed
            mplew.writeShort(equip.getJump()); // jump
            mplew.writeMapleAsciiString(equip.getOwner());
            mplew.writeShort(equip.getFlag()); //Item Flags
            mplew.write(0);
            
			if (!masking) { //Vicious hammer stuff.
				mplew.write(0);
				mplew.writeShort(0);
                mplew.writeShort(0); // item exp
                mplew.writeInt(equip.getHammerSlots()); //vicious hammer
			}
			
			mplew.writeLong(0);
			mplew.write(HexTool.getByteArrayFromHexString("00 40 E0 FD 3B 37 4F 01"));
			
			mplew.writeInt(-1);
		} else {
			mplew.writeShort(item.getQuantity());
			mplew.writeMapleAsciiString(item.getOwner());
			mplew.writeShort(item.getFlag()); // this seems to end the item entry
			// but only if its not a THROWING STAR :))9 O.O!

			if (ii.isThrowingStar(item.getItemId()) || ii.isBullet(item.getItemId())) {
				mplew.write(HexTool.getByteArrayFromHexString("02 00 00 00 54 00 00 34"));
			}
		}
	}
	
	public static void addInventoryInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
		mplew.write(100); // equip slots
		mplew.write(100); // use slots
		mplew.write(100); // set-up slots
		mplew.write(100); // etc slots
		mplew.write(100); // cash slots

		MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
		Collection<IItem> equippedC = iv.list();
		List<Item> equipped = new ArrayList<Item>(equippedC.size());
		for (IItem item : equippedC) {
			equipped.add((Item) item);
		}
		Collections.sort(equipped);

		for (Item item : equipped) {
			addItemInfo(mplew, item);
		}
		mplew.writeShort(0); // start of equip inventory

		iv = chr.getInventory(MapleInventoryType.EQUIP);
		for (IItem item : iv.list()) {
			addItemInfo(mplew, item);
		}
		mplew.write(0); // start of use inventory
		// addItemInfo(mplew, new Item(2020028, (byte) 8, (short) 1));

		iv = chr.getInventory(MapleInventoryType.USE);
		for (IItem item : iv.list()) {
			addItemInfo(mplew, item);
		}
		mplew.write(0); // start of set-up inventory

		iv = chr.getInventory(MapleInventoryType.SETUP);
		for (IItem item : iv.list()) {
			addItemInfo(mplew, item);
		}
		mplew.write(0); // start of etc inventory

		iv = chr.getInventory(MapleInventoryType.ETC);
		for (IItem item : iv.list()) {
			addItemInfo(mplew, item);
		}
		mplew.write(0); // start of cash inventory

		iv = chr.getInventory(MapleInventoryType.CASH);
		for (IItem item : iv.list()) {
			addItemInfo(mplew, item);
		}
	}
	
	public static void addSkillInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
		mplew.write(0); // start of skills

		Map<ISkill, MapleCharacter.SkillEntry> skills = chr.getSkills();
		mplew.writeShort(skills.size());
		for (Entry<ISkill, MapleCharacter.SkillEntry> skill : skills.entrySet()) {
			mplew.writeInt(skill.getKey().getId());
			mplew.writeInt(skill.getValue().skillevel);
			if (skill.getKey().isFourthJob()) {
				mplew.writeInt(skill.getValue().masterlevel);
			}
		}
	}
	
    public static void addMonsterBookInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
    	mplew.writeInt(chr.getMonsterBookCover()); // cover
    	mplew.write(0);

    	Map<Integer, Integer> cards = chr.getMonsterBook().getCards();
    	mplew.writeShort(cards.size());

    	MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
    	for (Entry<Integer, Integer> all : cards.entrySet()) {
    	    mplew.writeShort(ii.getCardShortId(all.getKey())); // Id
    	    mplew.write(all.getValue()); // Level
    	}
    }
}
