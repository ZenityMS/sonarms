/*
@Author Jvlaple
*/

function enter(pi) {
	var nextMap = 240050101;
	var eim = pi.getPlayer().getEventInstance()
	var target = eim.getMapInstance(nextMap);
	var targetPortal = target.getPortal("sp");
	// only let people through if the eim is ready
	var avail = eim.getProperty("1stageclear");
	if (!pi.haveItem(4001092, 1)) {
		// do nothing; send message to player
		pi.getPlayer().dropMessage(6, "Horntail\'s Seal is Blocking this Door.");
		return false;
	}else {
		pi.gainItem(4001092, -1);
		pi.getPlayer().dropMessage(6, "The key disentegrates as Horntail\'s Seal is broken for a flash...");
		pi.getPlayer().changeMap(target, targetPortal);
		return true;
	}
}