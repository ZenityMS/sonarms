
/*
MiniDungeon - Rabbit
*/ 

var baseid = 221023400;
var dungeonid = 221023401;
var dungeons = 30;

function enter(pi) {
	if (pi.getMapId() == baseid) {
	    for(var i = 0; i < dungeons; i++) {
		if (pi.getPlayerCount(dungeonid + i) == 0) {
		    pi.warp(dungeonid + i, 0);
		    return true;
		}
	    }
	    pi.playerMessage(5, "All of the Mini-Dungeons are in use right now, please try again later.");
	} else {
	pi.warp(baseid, "MD00");
	}
	return true;
}
