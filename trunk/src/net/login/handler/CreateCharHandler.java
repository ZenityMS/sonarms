package net.login.handler;

import client.IItem;
import client.Item;
import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.MapleInventoryType;
import client.MapleSkinColor;
import net.AbstractMaplePacketHandler;
import net.login.LoginServer;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class CreateCharHandler extends AbstractMaplePacketHandler {
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
        int str = slea.readByte();
        int dex = slea.readByte();
        int _int = slea.readByte();
        int luk = slea.readByte();
        MapleCharacter newchar = MapleCharacter.getDefault(c);
        newchar.setWorld(c.getWorld());
        newchar.setFace(face);
        newchar.setHair(hair + hairColor);
        newchar.setGender(gender);
        if (LoginServer.getInstance().getResetStats()) {
            newchar.setStr(4);
            newchar.setDex(4);
            newchar.setInt(4);
            newchar.setLuk(4);
            newchar.setRemainingAp(9);
        } else {
            newchar.setStr(str);
            newchar.setDex(dex);
            newchar.setInt(_int);
            newchar.setLuk(luk);
        }
        newchar.setName(name);
        newchar.setSkinColor(MapleSkinColor.getById(skinColor));
        MapleItemInformationProvider mi = MapleItemInformationProvider.getInstance();
        byte[] array = {-5, -6, -7, -11};
        int[] ids = {top, bottom, shoes, weapon};
        for (int i = 0; i < 4; i++) {
            IItem ii = mi.getEquipById(ids[i]);
            ii.setPosition(array[i]);
            newchar.getInventory(MapleInventoryType.EQUIPPED).addFromDB(ii);
        }
        newchar.getInventory(MapleInventoryType.ETC).addItem(new Item(4161001, (byte) 0, (short) 1));
        if (MapleCharacterUtil.canCreateChar(name, c.getWorld())) {
            newchar.saveToDB(false);
            c.getSession().write(MaplePacketCreator.addNewCharEntry(newchar));
        }
    }
}