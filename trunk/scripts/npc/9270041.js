/* 	
Irene - Warp to Singapore (from Kerning City)
By Moogra
*/

function start() {
    cm.sendYesNo("Do you want to go to Singapore?");
}

function action(mode, type, selection) {
    if (mode > 0)
        cm.warp(540010000, 0);
    cm.dispose();
}