function enter(pi) {
    var papuMap = pi.getClient().getChannelServer().getMapFactory().getMap(220080001);
    if (papuMap.getCharacters().isEmpty()) {
        pi.getPlayer().dropMessage("The room is empty.  A perfect opportunity to challenge the boss.");
        papuMap.resetReactors();
    } else { // someone is inside
        var mapobjects = papuMap.getMapObjects();
        var boss = null;
        var iter = mapobjects.iterator();
        while (iter.hasNext()) {
            o = iter.next();
            if (pi.isMonster(o))
                boss = o;
        }
        if (boss != null) {
            pi.getPlayer().dropMessage("Someone is fighting " + boss.getName() + ".");
            return false;
        }
    }
    pi.warp(220080001, "st00");
    return true;
}