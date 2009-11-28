importPackage(net.sf.odinms.server.maps);
importPackage(net.sf.odinms.scripting.AbstractPlayerInteraction);

function enter(pi) {
	if (pi.haveItem(4031346)) {
		if (pi.getPlayer().getMapId() == 240010100) {
			pi.warp(101010000, "minar00");
			pi.gainItem(4031346, -1);
			pi.playerMessage("The Magical Seed is spent and you are transferred to somewhere.");
		} else {
			pi.warp(240010100, "elli00");
			pi.gainItem(4031346, -1);
			pi.playerMessage("The Magical Seed is spent and you are transferred to somewhere.");
		}
		return true;
	}
	pi.playerMessage("Magic Seed is needed to go through the portal.");
	return false;
}