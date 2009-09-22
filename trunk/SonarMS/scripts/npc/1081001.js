/**
-- Odin JavaScript --------------------------------------------------------------------------------
	Pison - Florina Beach(110000000)
-- By ---------------------------------------------------------------------------------------------
	Information & Xterminator
-- Version Info -----------------------------------------------------------------------------------
	1.1 - Add null map check [Xterminator]
	1.0 - First Version
---------------------------------------------------------------------------------------------------
**/
var status = 0;
var returnmap = cm.saveGetFlorina(1);

function start() {
    if (returnmap == -1)
        returnmap = 104000000;
    cm.sendNext("So you want to leave #b#m110000000##k? If you want, I can take you back to #b#m"+returnmap+"##k.");
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0) {
            cm.sendNext("You must have some business to take care of here. It's not a bad idea to take some rest at #m"+returnmap+"# Look at me; I love it here so much that I wound up living here. Hahaha anyway, talk to me when you feel like going back.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1)
            cm.sendYesNo("Are you sure you want to return to #b#m"+returnmap+"##k? Alright, we'll have to get going fast. Do you want to head back to #m"+returnmap+"# now?")
        else
            cm.warp(returnmap);
        cm.dispose();
    }
}
