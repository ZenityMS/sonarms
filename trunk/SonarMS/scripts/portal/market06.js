function enter(pi) {
    if (pi.getPlayer().getMapId() != 910000000) {
        pi.saveFM();
        pi.warp(910000000, "out00");
        return true;
    }
    return false;
}