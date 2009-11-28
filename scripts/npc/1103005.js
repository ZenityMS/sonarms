/*
	NPC Name: 		Neinheart
	Map(s): 		-Ereve Cygnus Intro-
	Description: 	warpout, accept cygnus help request
*/
importPackage(net.sf.odinms.server.maps);

function start() {
    cm.sendAcceptDecline("Becoming a Knight of Cygnus requires talent, faith, courage, and will power... and it looks like you are more than qualified to become a Knight of Cygnus. What do you think? If you wish to become one right this minute, I'll take you straight to Ereve. Would you like to head over to Erev right now?");
}

function action(mode, type, selection) {
    var returnmap = cm.getPlayer().getSavedLocation(SavedLocationType.CYGNUSINTRO);
	
    if (returnmap == null) {
        cm.warp(130000000, 0);
    } else {
        if (mode == 1 && returnmap != null) {
            cm.warp(returnmap.getMapId(), returnmap.getPortal());
        } else {
            cm.warp(130000000, 0);
        }
        cm.getPlayer().clearSavedLocation(SavedLocationType.CYGNUSINTRO);
    }
	
    cm.dispose();
}
