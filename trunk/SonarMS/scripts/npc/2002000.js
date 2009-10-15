/* Rupi by Moogra
Happyville Warp NPC
*/

function start() {
    cm.sendSimple("Do you want to go to Happyville or get out of Happyville ?\r\n#L0#I want to go to Happyville!#l\r\n\#L1#I want to get out of Happyville#l");
}

function action(mode, type, selection) {
    if (selection == 0)
        cm.warp(209000000, 0);
    else if (selection == 1)
        cm.warp(101000000, 0);
    cm.dispose();
}