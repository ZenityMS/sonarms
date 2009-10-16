//Author: Moogra
//Edited by: Tommy
var mob = Array(8500001, 8510000, 9400014, 9400121, 9400112);

function start() {
    cm.sendSimple("I am the boss summoner for SonarMS! Would you like me to spawn some bosses for you? \r\n Please choose the monster you would like summoned. #b\r\n#L0#Papulatus clock#l\r\n#L1#Pianus#l\r\n#L2#Black Crow#l\r\n#L3#Anego#l\r\n#L4#BodyGuard A#l#k");
}

function action(mode, type, selection) {
        cm.summonMob(mob[selection]);
    cm.dispose();
}