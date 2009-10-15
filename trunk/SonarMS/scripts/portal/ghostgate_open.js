/*
Sharen III's Grave Exit - Guild Quest

@Author Lerk
*/

function enter(pi) {
    if (pi.getPlayer().getMap().getReactorByName("ghostgate").getState() == 1) {
        pi.warp(990000800);
        return true;
    }
    else {
        pi.playerMessage("This way forward is not open yet.");
        return false;
    }
}