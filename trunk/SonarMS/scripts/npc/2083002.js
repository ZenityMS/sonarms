
/**
 *Crystal of Roots
 *@Author: Moogra
 *@NPC: Crystal of Roots
 */
function start() {
    cm.sendYesNo("Do you wish to leave?");
}

function action(mode, type, selection) {
    if (mode < 1)
        cm.dispose();
    else {
        cm.warp(240040700);
        if (cm.getPlayer().getMap().getCharacters().size() < 2){
            cm.getPlayer().getMap().killAllMonster(false);
            cm.getPlayer().getMap().resetReactors();
        }
        cm.dispose();
    }
}