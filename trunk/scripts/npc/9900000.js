/*
 * @Name         KIN
 * @Author:      Signalize
 * @NPC:         9900000
 * @Purpose:     Levels people up.
 */
function start() {
    cm.sendYesNo("Do you want to level up?");
}

function action(i, am, pro) {
    if (i > 0)
        if (cm.getPlayer().gmLevel() > 0)
            cm.getPlayer().levelUp();
    cm.dispose();
}