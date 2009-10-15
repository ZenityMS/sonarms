/* Olson the Toy Soldier
	2040002

map: 922000010
quest: 3230
escape: 2040028
*/
var status = 0;

function start() {
    if (cm.isQuestStarted(3230))
        cm.sendNext("The pendulum is hidden inside a dollhouse that looks different than the others.");
    else {
        cm.sendOk("Hello there.");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (status >= 1 && mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1) 
            cm.sendYesNo("Are you ready to enter the dollhouse map?");
        else if (status == 2) {
            cm.warp(922000010,0);
            cm.dispose();
        }
    }
}	