/**
	Dolphin - Aquarium(230000000)
**/

var status = 0;
var menu;
var payment = false;

function start() {
    if (cm.haveItem(4031242)) 
        menu = "#L0##bI will use #t4031242##k to move to #b#m230030200##k.#l\r\n#L1#Go to #b#m251000000##k after paying #b10000mesos#k.#l";
    else {
        menu = "#L0#Go to #b#m230030200##k after paying #b1000mesos#k.#l\r\n#L1#Go to #b#m251000000##k after paying #b10000mesos#k.#l";
        payment = true;
    }
    cm.sendSimple ("Ocean are all connected to each other. Place you can't reach by foot can easily reached oversea. How about taking #bDolphin Taxi#k with us today?\r\n"+menu);
}

function action(mode, type, selection) {
    if (mode < 1) 
        cm.dispose();
    else {
        if (selection == 0) {
            if(payment) {
                if(cm.getPlayer().getMeso() < 1000) {
                    cm.sendOk("I don't think you have enough money...");
                    cm.dispose();
                } else
                    cm.gainMeso(-1000);
            } else
                cm.gainItem(4031242,-1);
            cm.warp(230030200);
            cm.dispose();
            return;
        } else if (cm.getPlayer().getMeso() < 10000) {
            cm.sendOk("I don't think you have enough money...");
            cm.dispose();
            return;
        }
        cm.gainMeso(-10000);
        cm.warp(251000100);
        cm.dispose();
    }
}