

importPackage(net.sf.odinms.server.maps);

/*
Stage 5: Door before Ergoth - Guild Quest

@Author Lerk
*/

function enter(pi) {
        if (pi.getPlayer().getMap().getReactorByName("kinggate").getState() == 1) {
                pi.warp(990000900, 2);
                if (pi.getPlayer().getEventInstance().getProperty("boss") != null && pi.getPlayer().getEventInstance().getProperty("boss").equals("true")) {
                        pi.changeMusic("Bgm10/Eregos");
                }
                return true;
        }
        else {
                pi.playerMessage("This crack appears to be blocked off by the door nearby.");
                return false;
        }
}
