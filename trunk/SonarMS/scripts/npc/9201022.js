/* NPC:     Thomas Swift
 * Maps:    100000000, 680000000
 * Author:  Moogra
 * Purpose: Amoria warper.
*/

function start() {
    if (cm.getPlayer().getMapId() < 100000001)
        cm.sendYesNo("I can take you to the Amoria Village. Are you ready to go?");
    else
        cm.sendYesNo("I can take you back to Henesys. Are you ready to go?");
}

function action(mode, type, selection) {
    if (mode == -1)
        cm.dispose();
    else {
        if (mode == 0)
            cm.sendOk("Ok, feel free to hang around until you're ready to go!");
        else {
            if (cm.getPlayer().getMapId() < 100000001)
                cm.warp(680000000, 0);
            else
                cm.warp(100000000, 5);
        }
        cm.dispose();
    }
}