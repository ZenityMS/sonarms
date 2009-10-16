/*
    Zakum Entrance
*/

function enter(pi) {
    if (!pi.haveItem(4001017)) {
        pi.getPlayer().dropMessage(6,"You do not have the Eye of Fire.  You may not face the boss.");
        return false;
    } else
        pi.warp(211042400,"west00");
    return true;
}