/***********
@Author Jvlaple
***********/

function enter(pi) {
	var nextMap = 925100100;
	var eim = pi.getPlayer().getEventInstance();
	var party = eim.getPlayers();
	var target = eim.getMapInstance(nextMap);
	var targetPortal = target.getPortal("sp");
	var mobCount = pi.countMonster();
	var playerS = pi.isLeader();
	// only let people through if the eim is ready
	if (playerS == false) {
		// do nothing; send message to player
		pi.getPlayer().dropMessage(6, "Only the party leader may enter this portal.");
		return false;
	}else if (mobCount < 1) {
		eim.setProperty("entryTimeStamp", 1000 * 60 * 6);
		for(var g=0; g<party.size(); g++) {
			party.get(g).changeMap(target, targetPortal);
			party.get(g).getClient().sendClock(party.get(g).getClient(), 300);
		}
		return true;
	}else {
		pi.getPlayer().dropMessage(6, "Please kill all monsters before proceeding.");
		return false;
	}
}