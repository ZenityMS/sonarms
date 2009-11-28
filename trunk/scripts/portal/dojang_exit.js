/*	
	Author: Traitor
*/
importPackage(net.sf.odinms.server.maps);

function enter(pi) {
	var returnMap = pi.getPlayer().getSavedLocation(SavedLocationType.DOJO);
	if (returnMap == null) {
		returnMap = new SavedLocation(100000000, 0);
	}
	pi.getPlayer().clearSavedLocation(SavedLocationType.DOJO);
	pi.warp(returnMap.getMapId(), true);
	return true;
}