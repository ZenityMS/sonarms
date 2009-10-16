/* Exit
	Warp NPC to Subway Ticketing Booth (103000100)
	located in B1 <Area 1> (103000900)
	located in B1 <Area 2> (103000901)
	located in B2 <Area 1> (103000903)
	located in B2 <Area 2> (103000904)
	located in B3 <Area 1> (103000906)
	located in B3 <Area 2> (103000907)
	located in B3 <Area 3> (103000908)
*/

var status = 0;

function start() {
    cm.sendNext("This device is connected to outside.");
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status >= 2 && mode == 0) {
            cm.sendOk("Alright, see you next time.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1) {
            cm.sendNextPrev("Are you going to give up and leave this place?")
        } else if (status == 2) {
            cm.sendYesNo("You'll have to start from scratch the next time you come in...");
        } else if (status == 3) {
            cm.warp(103000100, 0);
            cm.dispose();
        }
    }
}