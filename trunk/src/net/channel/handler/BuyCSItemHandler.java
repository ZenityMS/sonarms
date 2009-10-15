package net.channel.handler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import client.MapleCharacter;
import client.MapleClient;
import client.MaplePet;
import client.MapleRing;
import tools.DatabaseConnection;
import net.AbstractMaplePacketHandler;
import server.CashItemFactory;
import server.CashItemInfo;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class BuyCSItemHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (slea.available() > 22) {
            slea.skip(1);
            slea.readInt();
            int payment = slea.readByte();
            slea.skip(3);
            int snCS = slea.readInt();
            CashItemInfo ring = CashItemFactory.getItem(snCS);
            int userLength = slea.readByte();
            slea.skip(1);
            String partner = slea.readAsciiString(userLength);
            slea.skip(2);
            int left = (int) slea.available();
            String text = slea.readAsciiString(left);
            MapleCharacter partnerChar = c.getChannelServer().getPlayerStorage().getCharacterByName(partner);
            if (partnerChar == null)
                c.getSession().write(MaplePacketCreator.serverNotice(1, "The partner you specified cannot be found.\r\nPlease make sure your partner is online and in the same channel."));
            else {
                c.getSession().write(MaplePacketCreator.showBoughtCSItem(ring.getId()));
                c.getPlayer().modifyCSPoints(payment, -ring.getPrice());
                MapleRing.createRing(c, ring.getId(), c.getPlayer().getId(), c.getPlayer().getName(), partnerChar.getId(), partnerChar.getName(), text);
            }
            c.getSession().write(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
            c.getSession().write(MaplePacketCreator.enableCSUse0());
            c.getSession().write(MaplePacketCreator.enableCSUse1());
            c.getSession().write(MaplePacketCreator.enableCSUse2());
            c.getSession().write(MaplePacketCreator.enableCSUse3());
        } else {
            int action = slea.readByte();
            if (action == 3) {
                slea.skip(1);
                int useNX = slea.readInt();
                int snCS = slea.readInt();
                CashItemInfo item = CashItemFactory.getItem(snCS);
                if (c.getPlayer().getCSPoints(useNX) >= item.getPrice())
                    c.getPlayer().modifyCSPoints(useNX, -item.getPrice());
                else {
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                if (item.getId() >= 5000000 && item.getId() <= 5000100) {
                    int petId = MaplePet.createPet(item.getId());
                    if (petId == -1)
                        return;
                    MapleInventoryManipulator.addById(c, item.getId(), (short) 1, null, petId);
                } else
                    MapleInventoryManipulator.addById(c, item.getId(), (short) item.getCount(), "");
                c.getSession().write(MaplePacketCreator.showBoughtCSItem(item.getId()));
                c.getSession().write(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                c.getSession().write(MaplePacketCreator.enableCSUse0());
                c.getSession().write(MaplePacketCreator.enableCSUse1());
                c.getSession().write(MaplePacketCreator.enableCSUse2());
                c.getSession().write(MaplePacketCreator.enableActions());
            } else if (action == 5) {
                try {
                    PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("DELETE FROM wishlist WHERE charid = ?");
                    ps.setInt(1, c.getPlayer().getId());
                    ps.executeUpdate();
                    ps.close();
                    int i = 10;
                    while (i > 0) {
                        int sn = slea.readInt();
                        if (sn != 0) {
                            ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO wishlist(charid, sn) VALUES(?, ?) ");
                            ps.setInt(1, c.getPlayer().getId());
                            ps.setInt(2, sn);
                            ps.executeUpdate();
                            ps.close();
                        }
                        i--;
                    }
                } catch (SQLException se) {
                }
                c.getSession().write(MaplePacketCreator.sendWishList(c.getPlayer().getId(), true));
            } else if (action == 7) {
                slea.skip(1);
                byte toCharge = slea.readByte();
                int toIncrease = slea.readInt();
                if (c.getPlayer().getCSPoints(toCharge) >= 4000 && c.getPlayer().getStorage().getSlots() < 48) { // 48 is max.
                    c.getPlayer().modifyCSPoints(toCharge, -4000);
                    if (toIncrease == 0)
                        c.getPlayer().getStorage().gainSlots((byte) 4);
                    c.getSession().write(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                    c.getSession().write(MaplePacketCreator.enableCSUse0());
                    c.getSession().write(MaplePacketCreator.enableCSUse1());
                    c.getSession().write(MaplePacketCreator.enableCSUse2());
                    c.getSession().write(MaplePacketCreator.enableCSUse3());
                    c.getSession().write(MaplePacketCreator.enableActions());
                }
            } else if (action == 28) { // Package
                slea.skip(1);
                int useNX = slea.readInt();
                int snCS = slea.readInt();
                CashItemInfo item = CashItemFactory.getItem(snCS);
                if (c.getPlayer().getCSPoints(useNX) < item.getPrice()) {
                    c.getSession().write(MaplePacketCreator.enableActions());
                    return;
                }
                c.getPlayer().modifyCSPoints(useNX, -item.getPrice());
                for (int i : CashItemFactory.getPackageItems(item.getId()))
                    if (i >= 5000000 && i <= 5000100) {
                        int petId = MaplePet.createPet(i);
                        if (petId == -1) {
                            c.getSession().write(MaplePacketCreator.enableActions());
                            return;
                        }
                        MapleInventoryManipulator.addById(c, i, (short) 1, null, petId);
                    } else
                        MapleInventoryManipulator.addById(c, i, (short) item.getCount(), "Cash Item was purchased.");
                c.getSession().write(MaplePacketCreator.showBoughtCSItem(item.getId()));
                c.getSession().write(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
                c.getSession().write(MaplePacketCreator.enableCSUse0());
                c.getSession().write(MaplePacketCreator.enableCSUse1());
                c.getSession().write(MaplePacketCreator.enableCSUse2());
                c.getSession().write(MaplePacketCreator.enableActions());
            } else if (action == 30) {
                CashItemInfo item = CashItemFactory.getItem(slea.readInt());
                if (c.getPlayer().getMeso() >= item.getPrice() && (item.getId() == 4031180 || item.getId() == 4031192)) {
                    c.getPlayer().gainMeso(-item.getPrice(), false);
                    MapleInventoryManipulator.addById(c, item.getId(), (short) item.getCount(), "");
                }
            }
        }
    }
}