

importPackage(net.sf.odinms.server.maps);

/*
Stage 2: Door guarded by Dark Muscle Golems - Guild Quest

@Author Lerk
*/

function enter(pi) {
        if (pi.getPlayer().getMap().getReactorByName("metalgate").getState() == 1) {
                pi.warp(990000431);
                return true;
        }
        else {
                pi.playerMessage("This way forward is not open yet.");
                return false;
        }
}
