package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import java.rmi.RemoteException;

public final class EnterCashShopHandler extends AbstractMaplePacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer().getNoPets() > 0)
            c.getPlayer().unequipAllPets();
        try {
            c.getChannelServer().getWorldInterface().addBuffsToStorage(c.getPlayer().getId(), c.getPlayer().getAllBuffs());
        } catch (RemoteException e) {
            c.getChannelServer().reconnectWorld();
        }
        c.getPlayer().cancelAllBuffs();
        c.getPlayer().getMap().removePlayer(c.getPlayer());
        c.getSession().write(MaplePacketCreator.warpCS(c));
        c.getPlayer().setInCS(true);
        c.getSession().write(MaplePacketCreator.enableCSUse0());
        c.getSession().write(MaplePacketCreator.enableCSUse1());
        c.getSession().write(MaplePacketCreator.enableCSUse2());
        c.getSession().write(MaplePacketCreator.enableCSUse3());
        c.getSession().write(MaplePacketCreator.showNXMapleTokens(c.getPlayer()));
        c.getSession().write(MaplePacketCreator.sendWishList(c.getPlayer().getId()));
        c.getPlayer().saveToDB(true);
    }
}