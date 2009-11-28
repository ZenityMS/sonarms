/*	
	Author: Traitor
*/
importPackage(net.sf.odinms.server.maps);
importPackage(net.sf.odinms.tools);

function enter(pi) {
	if (pi.getPlayer().getMap().getMonsterById(9300216) != null) {
		pi.getClient().getSession().write(MaplePacketCreator.updateDojoStats(0, 1, true));
		//pi.getClient().getSession().write(MaplePacketCreator.Mulung_DojoUp2());
		pi.getClient().getSession().write(MaplePacketCreator.dojoWarpUp());
		var reactor = pi.getPlayer().getMap().getReactorByName("door");
		reactor.delayedHitReactor(pi.getClient(), 500);
		return true;
	} else {
		pi.getClient().getSession().write(MaplePacketCreator.serverNotice(5, "There are still some monsters remaining"));
	}
	return false;
}