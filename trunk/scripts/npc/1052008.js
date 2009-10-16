/* Shumi JQ Chest #1
*/

var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    var prizes = Array(1040045, 1040055, 1040129, 1040137, 1041109, 1041009, 1041134, 1041132, 1041005, 1041138, 1042018, 1042035, 1042038, 1042024, 1042002, 1702000, 1702027);
    var chances = Array(10, 10, 10, 5, 10, 10, 10, 10, 10, 5, 10, 10, 10, 10, 10, 5, 3);
    var totalodds = 0;
    var choice = 0;
    for (var i = 0; i < chances.length; i++) {
        var itemGender = (Math.floor(prizes[i]/1000)%10);
        if ((cm.getPlayer().getGender() != itemGender) && (itemGender != 2))
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
    if (cm.isQuestStarted(2055))
        cm.gainItem(4031039,1);
    cm.gainItem(prizes[choice],1);
    cm.warp(103000100, 0);
    cm.dispose();
}