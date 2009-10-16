/* Shumi JQ Chest #1
*/

var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    var prizes = Array(1060055, 1060120, 1062003, 1062026, 1062033, 1062044, 1062062, 1061007, 1061072, 1061108, 1061141, 1702042, 1702072);
    var chances = Array(14, 7, 10, 10, 10, 10, 10, 9, 9, 9, 6, 5, 5);
    var totalodds = 0;
    var choice = 0;
    for (var i = 0; i < chances.length; i++) {
        var itemGender = (Math.floor(prizes[i]/1000)%10);
        if (cm.getPlayer().getGender() != itemGender && itemGender != 2)
            chances[i] = 0;
    }
    for (var i = 0; i < chances.length; i++)
        totalodds += chances[i];
    var randomPick = Math.floor(Math.random()*totalodds)+1;
    for (var i = 0; i < chances.length; i++) {
        randomPick -= chances[i];
        if (randomPick <= 0) {
            choice = i;
            randomPick = totalodds + 100;
        }
    }
    if (cm.isQuestStarted(2056))
        cm.gainItem(4031040,1);
    cm.gainItem(prizes[choice],1);
    cm.warp(103000100, 0);
    cm.dispose();
}