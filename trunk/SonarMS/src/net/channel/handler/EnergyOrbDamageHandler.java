package net.channel.handler;

import client.MapleClient;
import tools.data.input.SeekableLittleEndianAccessor;

public class EnergyOrbDamageHandler extends AbstractDealDamageHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer().getEnergyBar() == 10000)
            applyAttack(parseDamage(slea, false), c.getPlayer(), 999999, 1);
    }
}