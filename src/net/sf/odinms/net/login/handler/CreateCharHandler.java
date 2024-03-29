package net.sf.odinms.net.login.handler;

import net.sf.odinms.client.IItem;
import net.sf.odinms.client.Item;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleCharacterUtil;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.MapleInventory;
import net.sf.odinms.client.MapleInventoryType;
import net.sf.odinms.client.MapleSkinColor;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.MapleItemInformationProvider;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class CreateCharHandler extends AbstractMaplePacketHandler {

	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CreateCharHandler.class);

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		String name = slea.readMapleAsciiString();
		int face = slea.readInt();
		int hair = slea.readInt();
		int hairColor = slea.readInt();
		int skinColor = slea.readInt();
		int top = slea.readInt();
		int bottom = slea.readInt();
		int shoes = slea.readInt();
		int weapon = slea.readInt();
		int gender = slea.readByte();

		MapleCharacter newchar = MapleCharacter.getDefault(c);
		if (c.isGm()) {
			newchar.setGM(1);
		}
		newchar.setWorld(c.getWorld());
		newchar.setFace(face);
		newchar.setHair(hair + hairColor);
		newchar.setGender(gender);
		newchar.setStr(4);
		newchar.setDex(4);
		newchar.setInt(4);
		newchar.setLuk(4);
		newchar.setRemainingAp(9);
		newchar.setName(name);
		newchar.setSkinColor(MapleSkinColor.getById(skinColor));

		MapleInventory equip = newchar.getInventory(MapleInventoryType.EQUIPPED);
		IItem eq_top = MapleItemInformationProvider.getInstance().getEquipById(top);
		eq_top.setPosition((byte) -5);
		equip.addFromDB(eq_top);
		IItem eq_bottom = MapleItemInformationProvider.getInstance().getEquipById(bottom);
		eq_bottom.setPosition((byte) -6);
		equip.addFromDB(eq_bottom);
		IItem eq_shoes = MapleItemInformationProvider.getInstance().getEquipById(shoes);
		eq_shoes.setPosition((byte) -7);
		equip.addFromDB(eq_shoes);
		IItem eq_weapon = MapleItemInformationProvider.getInstance().getEquipById(weapon);
		eq_weapon.setPosition((byte) -11);
		equip.addFromDB(eq_weapon);

		MapleInventory etc = newchar.getInventory(MapleInventoryType.ETC);
		etc.addItem(new Item(4161001, (byte) 0, (short) 1));

		boolean charok = true;

		if (gender == 0) {
			if (face != 20000 && face != 20001 && face != 20002) {
				charok = false;
			}
			if (hair != 30000 && hair != 30020 && hair != 30030) {
				charok = false;
			}
			if (top != 1040002 && top != 1040006 && top != 1040010) {
				charok = false;
			}
			if (bottom != 1060006 && bottom != 1060002) {
				charok = false;
			}
		} else if (gender == 1) {
			if (face != 21000 && face != 21001 && face != 21002) {
				charok = false;
			}
			if (hair != 31000 && hair != 31040 && hair != 31050) {
				charok = false;
			}
			if (top != 1041002 && top != 1041006 && top != 1041010 && top != 1041011) {
				charok = false;
			}
			if (bottom != 1061002 && bottom != 1061008) {
				charok = false;
			}
		} else {
			charok = false;
		}
		if (skinColor < 0 || skinColor > 3) {
			charok = false;
		}
		if (weapon != 1302000 && weapon != 1322005 && weapon != 1312004) {
			charok = false;
		}
		if (shoes != 1072001 && shoes != 1072005 && shoes != 1072037 && shoes != 1072038) {
			charok = false;
		}
		if (hairColor != 0 && hairColor != 2 && hairColor != 3 && hairColor != 7) {
			charok = false;
		}

		if (charok && MapleCharacterUtil.canCreateChar(name, c.getWorld())) {
			newchar.saveToDB(false);
			c.getSession().write(MaplePacketCreator.addNewCharEntry(newchar, charok));
		} else {
			log.warn(MapleClient.getLogMessage(c, "Trying to create a character with a name: {}", name));
		}
	}
}
