package net.channel.handler;

import java.rmi.RemoteException;
import client.*;
import net.AbstractMaplePacketHandler;
import net.world.remote.WorldChannelInterface;
import server.MTSItemInfo;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.DatabaseConnection;
import java.sql.*;
import java.util.*;
import server.maps.SavedLocationType;

public class EnterMTSHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.getChannelServer().allowMTS()) {
            if (c.getPlayer().getNoPets() > 0)
                c.getPlayer().unequipAllPets();
            try {
                WorldChannelInterface wci = c.getChannelServer().getWorldInterface();
                wci.addBuffsToStorage(c.getPlayer().getId(), c.getPlayer().getAllBuffs());
                wci.addCooldownsToStorage(c.getPlayer().getId(), c.getPlayer().getAllCooldowns());
            } catch (RemoteException e) {
                c.getChannelServer().reconnectWorld();
            }
            c.getPlayer().saveToDB(true);
            c.getPlayer().getMap().removePlayer(c.getPlayer());
            c.getSession().write(MaplePacketCreator.warpCS(c, true));
            c.getPlayer().setInMTS(true);
            c.getSession().write(MaplePacketCreator.enableCSUse0());
            c.getSession().write(MaplePacketCreator.MTSWantedListingOver(0, 0));
            c.getSession().write(MaplePacketCreator.showMTSCash(c.getPlayer()));
            List<MTSItemInfo> items = new ArrayList<MTSItemInfo>();
            int pages = 0;
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM mts_items WHERE tab = 1 AND transfer = 0 ORDER BY id DESC LIMIT ?, 16");
                ps.setInt(1, 0);
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                    if (rs.getInt("type") != 1) {
                        Item i = new Item(rs.getInt("itemid"), (byte) 0, (short) rs.getInt("quantity"));
                        i.setOwner(rs.getString("owner"));
                        items.add(new MTSItemInfo(i, rs.getInt("price") + 100 + (int) (rs.getInt("price") * 0.1), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"), rs.getString("sell_ends")));
                    } else {
                        Equip equip = new Equip(rs.getInt("itemid"), (byte) rs.getInt("position"), -1);
                        equip.setOwner(rs.getString("owner"));
                        equip.setQuantity((short) 1);
                        equip.setAcc((short) rs.getInt("acc"));
                        equip.setAvoid((short) rs.getInt("avoid"));
                        equip.setDex((short) rs.getInt("dex"));
                        equip.setHands((short) rs.getInt("hands"));
                        equip.setHp((short) rs.getInt("hp"));
                        equip.setInt((short) rs.getInt("int"));
                        equip.setJump((short) rs.getInt("jump"));
                        equip.setLuk((short) rs.getInt("luk"));
                        equip.setMatk((short) rs.getInt("matk"));
                        equip.setMdef((short) rs.getInt("mdef"));
                        equip.setMp((short) rs.getInt("mp"));
                        equip.setSpeed((short) rs.getInt("speed"));
                        equip.setStr((short) rs.getInt("str"));
                        equip.setWatk((short) rs.getInt("watk"));
                        equip.setWdef((short) rs.getInt("wdef"));
                        equip.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                        equip.setLocked((byte) rs.getInt("locked"));
                        equip.setLevel((byte) rs.getInt("level"));
                        items.add(new MTSItemInfo((IItem) equip, rs.getInt("price") + 100 + (int) (rs.getInt("price") * 0.1), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"), rs.getString("sell_ends")));
                    }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT COUNT(*) FROM mts_items");
                rs = ps.executeQuery();
                if (rs.next())
                    pages = (int) Math.ceil(rs.getInt(1) / 16);
                rs.close();
                ps.close();
            } catch (SQLException e) {
            }
            c.getSession().write(MaplePacketCreator.sendMTS(items, 1, 0, 0, pages));
            c.getSession().write(MaplePacketCreator.TransferInventory(getTransfer(c.getPlayer().getId())));
            c.getSession().write(MaplePacketCreator.NotYetSoldInv(getNotYetSold(c.getPlayer().getId())));
        } else
            if (!(c.getPlayer().isAlive()))
                c.getSession().write(MaplePacketCreator.enableActions());
            else
                if (c.getPlayer().getMapId() != 910000000) {
                    c.getPlayer().dropMessage("You will be transported to the Free Market Entrance.");
                    c.getSession().write(MaplePacketCreator.enableActions());
                    c.getPlayer().saveLocation(SavedLocationType.FREE_MARKET);
                    c.getPlayer().changeMap(c.getChannelServer().getMapFactory().getMap(910000000), c.getChannelServer().getMapFactory().getMap(910000000).getPortal("out00"));
                } else {
                    c.getPlayer().dropMessage("You are already in the Free Market Entrance.");
                    c.getSession().write(MaplePacketCreator.enableActions());
                }
    }

    private List<MTSItemInfo> getNotYetSold(int cid) {
        List<MTSItemInfo> items = new ArrayList<MTSItemInfo>();
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM mts_items WHERE seller = ? AND transfer = 0 ORDER BY id DESC");
            ps.setInt(1, cid);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                if (rs.getInt("type") != 1) {
                    Item i = new Item(rs.getInt("itemid"), (byte) 0, (short) rs.getInt("quantity"));
                    i.setOwner(rs.getString("owner"));
                    items.add(new MTSItemInfo((IItem) i, rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"), rs.getString("sell_ends")));
                } else {
                    Equip equip = new Equip(rs.getInt("itemid"), (byte) rs.getInt("position"), -1);
                    equip.setOwner(rs.getString("owner"));
                    equip.setQuantity((short) 1);
                    equip.setAcc((short) rs.getInt("acc"));
                    equip.setAvoid((short) rs.getInt("avoid"));
                    equip.setDex((short) rs.getInt("dex"));
                    equip.setHands((short) rs.getInt("hands"));
                    equip.setHp((short) rs.getInt("hp"));
                    equip.setInt((short) rs.getInt("int"));
                    equip.setJump((short) rs.getInt("jump"));
                    equip.setLuk((short) rs.getInt("luk"));
                    equip.setMatk((short) rs.getInt("matk"));
                    equip.setMdef((short) rs.getInt("mdef"));
                    equip.setMp((short) rs.getInt("mp"));
                    equip.setSpeed((short) rs.getInt("speed"));
                    equip.setStr((short) rs.getInt("str"));
                    equip.setWatk((short) rs.getInt("watk"));
                    equip.setWdef((short) rs.getInt("wdef"));
                    equip.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                    equip.setLocked((byte) rs.getInt("locked"));
                    equip.setLevel((byte) rs.getInt("level"));
                    items.add(new MTSItemInfo((IItem) equip, rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"), rs.getString("sell_ends")));
                }
            rs.close();
            ps.close();
        } catch (SQLException e) {
        }
        return items;
    }

    private List<MTSItemInfo> getTransfer(int cid) {
        List<MTSItemInfo> items = new ArrayList<MTSItemInfo>();
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM mts_items WHERE transfer = 1 AND seller = ? ORDER BY id DESC");
            ps.setInt(1, cid);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                if (rs.getInt("type") != 1) {
                    Item i = new Item(rs.getInt("itemid"), (byte) 0, (short) rs.getInt("quantity"));
                    i.setOwner(rs.getString("owner"));
                    items.add(new MTSItemInfo((IItem) i, rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"), rs.getString("sell_ends")));
                } else {
                    Equip equip = new Equip(rs.getInt("itemid"), (byte) rs.getInt("position"), -1);
                    equip.setOwner(rs.getString("owner"));
                    equip.setQuantity((short) 1);
                    equip.setAcc((short) rs.getInt("acc"));
                    equip.setAvoid((short) rs.getInt("avoid"));
                    equip.setDex((short) rs.getInt("dex"));
                    equip.setHands((short) rs.getInt("hands"));
                    equip.setHp((short) rs.getInt("hp"));
                    equip.setInt((short) rs.getInt("int"));
                    equip.setJump((short) rs.getInt("jump"));
                    equip.setLuk((short) rs.getInt("luk"));
                    equip.setMatk((short) rs.getInt("matk"));
                    equip.setMdef((short) rs.getInt("mdef"));
                    equip.setMp((short) rs.getInt("mp"));
                    equip.setSpeed((short) rs.getInt("speed"));
                    equip.setStr((short) rs.getInt("str"));
                    equip.setWatk((short) rs.getInt("watk"));
                    equip.setWdef((short) rs.getInt("wdef"));
                    equip.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                    equip.setLocked((byte) rs.getInt("locked"));
                    equip.setLevel((byte) rs.getInt("level"));
                    items.add(new MTSItemInfo((IItem) equip, rs.getInt("price"), rs.getInt("id"), rs.getInt("seller"), rs.getString("sellername"), rs.getString("sell_ends")));
                }
            rs.close();
            ps.close();
        } catch (SQLException e) {
        }
        return items;
    }
}