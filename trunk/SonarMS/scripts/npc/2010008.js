/* guild emblem npc */

var status = 0;
var sel;

function start() {
    cm.sendSimple("What would you like to do?\r\n#b#L0#Create/Change your Guild Emblem#l#k");
}

function action(mode, type, selection) {
    if (mode < 1)
        cm.dispose();
    else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1) {
            sel = selection;
            if (sel == 0) {
                if (cm.getPlayer().getGuildRank() == 1)
                    cm.sendYesNo("Creating or changing Guild Emblem costs #b 5000000 mesos#k, are you sure you want to continue?");
                else
                    cm.sendOk("You must be the Guild Leader to change the Emblem. Please tell your leader to speak with me.");
            }
        }
        else if (status == 2) {
            if (sel == 0) {
                cm.getPlayer().genericGuildMessage(17);
                cm.dispose();
            }
        }
    }
}
