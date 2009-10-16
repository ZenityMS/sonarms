package net.channel.handler;

import client.MapleBuffStat;
import java.net.InetAddress;
import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import server.MaplePortal;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public class ChangeMapHandler extends AbstractMaplePacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer().isActiveBuffedValue(4001003)) {
            c.getPlayer().setHide(true);
            c.getPlayer().cancelEffectFromBuffStat(MapleBuffStat.DARKSIGHT);
        }
        if (slea.available() == 0) {
            String ip = ChannelServer.getInstance(c.getChannel()).getIP(c.getChannel());
            String[] socket = ip.split(":");
            c.getPlayer().saveToDB(true);
            c.getPlayer().setInCS(false);
            ChannelServer.getInstance(c.getChannel()).removePlayer(c.getPlayer());
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
            try {
                c.getSession().write(MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
                c.getSession().close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            slea.readByte(); // 1 = from dying 2 = regular portals
            int targetid = slea.readInt(); // FF FF FF FF
            String startwp = slea.readMapleAsciiString();
            MaplePortal portal = c.getPlayer().getMap().getPortal(startwp);
            MapleCharacter player = c.getPlayer();
            if (targetid != -1 && !c.getPlayer().isAlive()) {
                boolean executeStandardPath = true;
                if (player.getEventInstance() != null)
                    executeStandardPath = player.getEventInstance().revivePlayer(player);
                if (executeStandardPath) {
                    player.cancelAllBuffs();
                    player.setHp(50);
                    player.setStance(0);
                    player.changeMap(c.getPlayer().getMap().getReturnMap());
                }
            } else if (targetid != -1 && c.getPlayer().gmLevel() > 0) {
                if (player.getChalkboard() != null)
                    player.getClient().getSession().write(MaplePacketCreator.useChalkboard(player, true));
                MapleMap to = c.getChannelServer().getMapFactory().getMap(targetid);
                player.changeMap(to, to.getPortal(0));
            } else if (targetid != -1 && c.getPlayer().gmLevel() < 0) {
            } else if (portal != null)
                portal.enterPortal(c);
            else
                c.getSession().write(MaplePacketCreator.enableActions());
        }
        if (c.getPlayer().getHide()) {//TODO make time not reset
            SkillFactory.getSkill(4001003).getEffect(c.getPlayer().getSkillLevel(SkillFactory.getSkill(4001003))).applyTo(c.getPlayer());
            c.getPlayer().setHide(false);
        }
    }
}