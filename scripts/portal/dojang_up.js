importPackage(net.sf.odinms.server.maps);
importPackage(net.sf.odinms.tools);

function enter(pi) {
    if (pi.getPlayer().getMap().getMonsterById(9300216) != null) {
		pi.getPlayer().addMojoPoints(1);
        pi.getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(5, "You have received 1 Training Point for a total of "+ pi.getPlayer().getMojoPoints() +""));
        pi.getPlayer().getClient().getSession().write(MaplePacketCreator.updateDojoStats(0, 1, true));
        pi.getPlayer().getClient().getSession().write(MaplePacketCreator.dojoWarpUp());
        var reactor = pi.getPlayer().getMap().getReactorByName("door");
        reactor.delayedHitReactor(pi.getC(), 500);
        return true;
    } else {
        pi.getPlayer().getClient().getSession().write(MaplePacketCreator.serverNotice(5, "There are still some monsters remaining"));
    }
    return false;
}  