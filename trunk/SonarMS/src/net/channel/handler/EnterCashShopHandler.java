package net.channel.handler;

import java.rmi.RemoteException;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Acrylic
 */
public class EnterCashShopHandler extends AbstractMaplePacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer().getNoPets() > 0)
            c.getPlayer().unequipAllPets();
        try {
            c.getChannelServer().getWorldInterface().addBuffsToStorage(c.getPlayer().getId(), c.getPlayer().getAllBuffs());
            c.getChannelServer().getWorldInterface().addCooldownsToStorage(c.getPlayer().getId(), c.getPlayer().getAllCooldowns());
        } catch (RemoteException e) {
            c.getChannelServer().reconnectWorld();
        }
        c.getSession().write(MaplePacketCreator.updateGender(c.getPlayer()));
        c.getPlayer().cancelAllBuffs();
        c.getPlayer().getMap().removePlayer(c.getPlayer());
        c.getSession().write(MaplePacketCreator.warpCS(c, false));
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