var status = 0;
var zones = 0;
var names = Array("Deep Forest of Patience 1", "Deep Forest of Patience 2", "Deep Forest of Patience 3");
var maps = Array(105040310, 105040312, 105040314);
var selectedMap = -1;

function start() {
    cm.sendNext("You feel a mysterious force surrounding this statue.");
    if (cm.isQuestStarted(2054))
        zones = 3;
    else if (cm.isQuestStarted(2053))
        zones = 2;
    else if (cm.isQuestStarted(2052))
        zones = 1;
    else
        zones = 0;
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
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
            if (zones == 0)
                cm.dispose();
            else {
                var selStr = "Its power allows you to will yourself deep inside the forest.#b";
                for (var i = 0; i < zones; i++)
                    selStr += "\r\n#L" + i + "#" + names[i] + "#l";
                cm.sendSimple(selStr);
            }
        } else if (status == 2) {
            cm.warp(maps[selection],0);
            cm.dispose();
        }
    }
}	