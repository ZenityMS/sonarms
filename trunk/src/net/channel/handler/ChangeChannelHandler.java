package net.channel.handler;

import java.net.InetAddress;
import java.rmi.RemoteException;
import client.MapleBuffStat;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import net.world.MapleMessengerCharacter;
import server.MapleTrade;
import server.maps.HiredMerchant;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public class ChangeChannelHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int channel = slea.readByte() + 1;
        if (!c.getPlayer().isAlive()) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        String[] socket = c.getChannelServer().getIP(channel).split(":");
        if (c.getPlayer().getTrade() != null)
            MapleTrade.cancelTrade(c.getPlayer());
        c.getPlayer().cancelMagicDoor();
        if (c.getPlayer().getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null)
            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        HiredMerchant merchant = c.getPlayer().getHiredMerchant();
        if (merchant != null)
            if (merchant.isOwner(c.getPlayer()))
                merchant.setOpen(true);
            else
                merchant.removeVisitor(c.getPlayer());
        if (c.getPlayer().getBuffedValue(MapleBuffStat.PUPPET) != null)
            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
        try {
            c.getChannelServer().getWorldInterface().addCooldownsToStorage(c.getPlayer().getId(), c.getPlayer().getAllCooldowns());
        } catch (RemoteException e) {
            c.getChannelServer().reconnectWorld();
        }
        c.getPlayer().saveToDB(true);
        if (c.getPlayer().getCheatTracker() != null)
            c.getPlayer().getCheatTracker().dispose();
        if (c.getPlayer().getMessenger() != null) {
            MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(c.getPlayer());
            try {
                c.getChannelServer().getWorldInterface().silentLeaveMessenger(c.getPlayer().getMessenger().getId(), messengerplayer);
            } catch (RemoteException e) {
                c.getChannelServer().reconnectWorld();
            }
        }
        c.getPlayer().getMap().removePlayer(c.getPlayer());
        c.getChannelServer().removePlayer(c.getPlayer());
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
        try {
            c.getSession().write(MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
