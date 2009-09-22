/*
  Name:   Bell (NLC Ticket Usher and Platform Usher)
  Maps:   600010001,600010002,103000100,600010004
  Author: aexr
  Fixes:  Moogra, Signalize
*/

function start() {
    if (cm.getPlayer().getMapId() == 103000100) cm.sendYesNo("Do you wish to visit New Leaf City on the continent of Masteria?");
    else if (cm.getPlayer().getMapId() == 600010001) cm.sendYesNo("Do you wish to visit Kerning City on the continent of Masteria?");
    else if (cm.getPlayer().getMapId() == 600010004) cm.sendYesNo("Do you wish to go to back to Kerning City?");
    else cm.sendYesNo("Do you wish to go to back to New Leaf City?");
}

function action(mode, type, selection) {
    if (mode < 0) 
        cm.dispose();
    else {
        if (cm.getPlayer().getMapId() == 103000100) cm.warp(600010004, 0);
        else if (cm.getPlayer().getMapId() == 600010001) cm.warp(600010002, 0);
        else if (cm.getPlayer().getMapId() == 600010004) cm.warp(103000100, 0);
        else cm.warp(600010001, 0);
        cm.dispose();
    }
}
