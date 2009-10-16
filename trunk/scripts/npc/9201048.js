/* @Author Jvlaple */

var status = 0;
var minLevel = 40;
var maxLevel = 255;
var minPlayers = 0;
var maxPlayers = 6;
var minMarried = 6;
var minGirls = 1;
var minBoys = 1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (cm.getParty() == null) {
        cm.sendOk("Please come back to me after you've formed a party.");
        cm.dispose();
        return;
    }
    if (!cm.isLeader()) {
        cm.sendOk("You are not the party leader.");
        cm.dispose();
    } else {
        var party = cm.getParty().getMembers();
        var mapId = cm.getPlayer().getMapId();
        var next = true;
        var levelValid = 0;
        var inMap = 0;
        if (party.size() < minPlayers || party.size() > maxPlayers)
            next = false;
        else {
            for (var i = 0; i < party.size() && next; i++) {
                if ((party.get(i).getLevel() >= minLevel) && (party.get(i).getLevel() <= maxLevel))
                    levelValid += 1;
                if (party.get(i).getMapid() == mapId)
                    inMap += 1;
            }
            if (levelValid < minPlayers || inMap < minPlayers)
                next = false;
        }
        if (next) {
            var em = cm.getEventManager("AmoriaPQ");
            if (em == null)
                cm.dispose();
            else
                em.startInstance(cm.getParty(),cm.getPlayer().getMap());
            cm.dispose();
        }
        else {
            cm.sendOk("Your party is not a party of six.  Make sure all your members are present and qualified to participate in this quest.  I see #b" + levelValid.toString() + " #kmembers are in the right level range, and #b" + inMap.toString() + "#k are in my map. If this seems wrong, #blog out and log back in,#k or reform the party.");
            cm.dispose();
        }
    }
}		
					
