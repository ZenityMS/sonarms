package net.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.anticheat.CheatingOffense;
import net.AbstractMaplePacketHandler;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class GiveFameHandler extends AbstractMaplePacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int who = slea.readInt();
        int mode = slea.readByte();
        int famechange = mode == 0 ? -1 : 1;
        MapleCharacter target = (MapleCharacter) c.getPlayer().getMap().getMapObject(who);
        MapleCharacter player = c.getPlayer();
        if (target == player) {
            player.getCheatTracker().registerOffense(CheatingOffense.FAMING_SELF);
            return;
        } else if (player.getLevel() < 15) {
            player.getCheatTracker().registerOffense(CheatingOffense.FAMING_UNDER_15);
            return;
        }
        switch (player.canGiveFame(target)) {
            case OK:
                if (Math.abs(target.getFame() + famechange) < 30001) {
                    target.addFame(famechange);
                    target.updateSingleStat(MapleStat.FAME, target.getFame());
                }
                if (player.gmLevel() < 1)
                    player.hasGivenFame(target);
                c.getSession().write(MaplePacketCreator.giveFameResponse(mode, target.getName(), target.getFame()));
                target.getClient().getSession().write(MaplePacketCreator.receiveFame(mode, player.getName()));
                break;
            case NOT_TODAY:
                c.getSession().write(MaplePacketCreator.giveFameErrorResponse(3));
                break;
            case NOT_THIS_MONTH:
                c.getSession().write(MaplePacketCreator.giveFameErrorResponse(4));
                break;
        }
    }
}
