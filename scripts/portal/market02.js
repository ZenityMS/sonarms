
importPackage(net.sf.odinms.server.maps);

/*
Enter Free Market script
*/

function enter(pi) {
	pi.getPlayer().saveLocation(SavedLocationType.FREE_MARKET);
	pi.warp(910000000, "out00");
	return true;
}