package net.sf.odinms.net.channel.handler;

import java.net.InetAddress;
import java.rmi.RemoteException;

import net.sf.odinms.client.MapleBuffStat;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.net.world.MapleMessengerCharacter;
import net.sf.odinms.server.MapleTrade;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public final class ChangeChannelHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
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
        if (c.getPlayer().getBuffedValue(MapleBuffStat.PUPPET) != null)
            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
        if (c.getPlayer().getParty() != null)
            c.getPlayer().setParty(c.getPlayer().getParty());
        try {
            c.getChannelServer().getWorldInterface().addBuffsToStorage(c.getPlayer().getId(), c.getPlayer().getAllBuffs());
        } catch (RemoteException e) {
            c.getChannelServer().reconnectWorld();
        }
        c.getPlayer().saveToDB(true);
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
        }
    }
}
