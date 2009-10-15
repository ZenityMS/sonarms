var status = 0;

function start() {
    if (cm.getPlayer().getParty() != null)
        cm.sendCPQMapLists();
    else {
        cm.sendOk("You must be in a party!");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode < 1)
        cm.dispose();
    else {
        status++;
        if (status == 1) {
            if (cm.fieldTaken(selection)) {
                if (cm.fieldLobbied(selection)) {
                    cm.challengeParty(selection);
                    cm.dispose();
                } else {
                    cm.sendOk("The room is taken.");
                    cm.dispose();
                }
            } else {
                cm.cpqLobby(selection);
                cm.dispose();
            }
        }
    }
}