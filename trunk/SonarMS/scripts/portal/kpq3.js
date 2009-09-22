/*
Kerning PQ: 3rd stage to 4th stage portal
*/

function enter(pi) {
	var nextMap = 103000803;
	var eim = pi.getPlayer().getEventInstance()
	var target = eim.getMapInstance(nextMap);
	var targetPortal = target.getPortal("st00");
	// only let people through if the eim is ready
	var avail = eim.getProperty("3stageclear");
	if (avail == null || pi.getPlayer().gmLevel() > 0) {
		// do nothing; send message to player
		pi.getPlayer().dropMessage(6, "The warp is currently unavailable.");
		return false;
	}
	else {
		pi.getPlayer().changeMap(target, targetPortal);
		return true;
	}
}