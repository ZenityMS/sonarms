/* Amon
 * 
 * @Author Stereo
 * Adobis's Mission I : Breath of Lava <Level 1> (280020000)
 * Adobis's Mission I : Breath of Lava <Level 2> (280020001)
 * Last Mission : Zakum's Altar (280030000)
 * Zakum Quest NPC 
 * Helps players leave the map
 */
 
function start() {
    cm.sendYesNo("If you leave now, you'll have to start over. Are you sure you want to leave?");
}
 
function action(mode, type, selection) {
    if (mode < 1)
        cm.dispose();
    else {
        if (cm.getPlayer().getMap().getCharacters().size() < 2){
            cm.getPlayer().getMap().killAllMonster(false);
            cm.getPlayer().getMap().resetReactors();
        }
        var eim = cm.getPlayer().getEventInstance();
        if (eim != null)
            eim.removePlayer(cm.getPlayer());
        else
            cm.warp(211042300);
        cm.dispose();
    }
}