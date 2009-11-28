/*	
	Author: Traitor
	Map(s):	All Dojo maps
	Desc:   SPAWN MOBS AND ALL THAT SNAZZ YO.
*/
importPackage(net.sf.odinms.server.life);
importPackage(net.sf.odinms.tools);
importPackage(java.awt);

var monsters = Array(9300184, 9300185, 9300186, 9300187, 9300188, 0, 9300189, 9300190, 9300191, 9300192, 9300193, 0, 9300194, 9300195, 9300196, 9300197, 9300198, 0, 9300199, 9300200, 9300201, 9300202, 9300203, 0, 9300204, 9300205, 9300206, 9300207, 9300208, 0, 9300209, 9300210, 9300211, 9300212, 9300213, 0, 9300214, 9300215);

function start(ms) {
	ms.getPlayer().resetEnteredScript(); //seems to be the script to spawn the monsters.
	var temp = (ms.getPlayer().getMapId() - 925000000) / 100; //thanks lailai, you a beast.
	var stage = (temp - (Math.floor(temp / 100) * 100)) | 0; // | 0 converts it from a double to an int. p cool.
	
	ms.getClient().getSession().write(MaplePacketCreator.getClock(360)); //6 minutes.
	
	if (!isRestingSpot(ms.getPlayer().getMap().getId())) {
		ms.getClient().getSession().write(MaplePacketCreator.playSound("Dojang/start"));
		ms.getClient().getSession().write(MaplePacketCreator.showEffect("dojang/start/stage"));
		ms.getClient().getSession().write(MaplePacketCreator.showEffect("dojang/start/number/" + stage));
		ms.getClient().getSession().write(MaplePacketCreator.getEnergy(300));
	}
	
	if (monsters[stage - 1] != 0) {
		var mob = MapleLifeFactory.getMonster(monsters[stage - 1]);
		if (mob != null) {
			mob.setBoss(false); //the mobs are "bosses" but they have to use the regular way of displaying hp, so we just set that to false.
			ms.getPlayer().getMap().spawnMonsterWithEffect(mob, 15, new Point(140, 0)); //140, 0 is the coords for the second position. 15 is the animation for spawning.
			//still need to get other positions (there are 3, one close to spawn, the second one that is above, and the third that is closest to the portal.
		}
	}
}

function isRestingSpot(id) {
    // Resting rooms :
    // 925020600 ~ 925020609
    // 925021200 ~ 925021209
    // 925021800 ~ 925021809
    // 925022400 ~ 925022409
    // 925023000 ~ 925023009
    // 925023600 ~ 925023609
    var shortid = id / 100;

    switch (shortid) {
	case 9250206:
	case 9250212:
	case 9250218:
	case 9250224:
	case 9250230:
	case 9250236:
	    return true;
    }
    return false;
}