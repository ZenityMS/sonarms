///**
//    Cesar
//    Ariant Coliseum
//**/
//
//
//function start() {
//    cm.sendYesNo("Would you like to go to #bAriant Coliseum#k? You must be level 20 to 30 to participate.");
//}
//
//function action(mode, type, selection) {
//    if (mode < 1)
//        cm.dispose();
//    else {
//        if(cm.getPlayer().getLevel() > 19 && cm.getPlayer().getLevel() < 31 || cm.getPlayer().gmLevel() > 0) {
//            cm.getPlayer().saveLocation(SavedLocationType.FREE_MARKET);
//            cm.warp(980010000, 3);
//            cm.dispose();
//        } else {
//            cm.sendOk("You're not between level 20 and 30. Sorry, you may not participate.");
//            cm.dispose();
//        }
//    }
//} 