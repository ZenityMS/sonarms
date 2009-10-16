var status = 0;
var menu;
var cost = 10000;

function start() {
    cm.sendYesNo("Will you move to #b#m230000000##k now? The price is #b"+cost+" mesos#k.");
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0) {
            cm.sendNext("Hmmm ... too busy to do it right now? If you feel like doing it, though, come back and find me.");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1) {
            if(cm.getPlayer().getMeso() < cost)
                cm.sendOk("I don't think you have enough money...");
            else {
                cm.gainMeso(-cost);
                cm.warp(230000000);
            }
            cm.dispose();
        }
    }
}