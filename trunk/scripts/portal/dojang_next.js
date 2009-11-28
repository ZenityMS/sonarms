/*	
	Author: Traitor
*/
importPackage(net.sf.odinms.tools);

function enter(pi) {
	if (pi.getPlayer().getMap().getReactorByName("door").getState() == 1) {
		pi.getPlayer().getMap().resetReactors();
		var temp = (pi.getPlayer().getMapId() - 925000000) / 100; //thanks lailai, you a beast.
		var stage = (temp - (Math.floor(temp / 100) * 100)) | 0; //| 0 converts it from a double to an int. p cool.
		
		if (stage != 38) {
			pi.getClient().getChannelServer().getMapFactory().getMap(pi.getPlayer().getMap().getId() + 100).killAllMonsters(false);
			pi.warp(pi.getPlayer().getMap().getId() + 100, 0);
		} else {
			pi.warp(925020003, 0); //rooftop rofl.
		}
		return true;
	} else {
		pi.getClient().getSession().write(MaplePacketCreator.serverNotice(5, "The door is not open yet."));
		return false;
	}
}