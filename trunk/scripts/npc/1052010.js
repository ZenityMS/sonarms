/* Shumi JQ Chest #1
*/

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    var prizes = Array(1050013, 1050034, 1050040, 1050116, 1050119, 1051029, 1051035, 1051049, 1051073, 1051116, 1052007, 1052027, 1052032, 1052050, 1702021, 1702050);
    var chances = Array(10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 6, 6);
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
    if (cm.isQuestStarted(2057))
        cm.gainItem(4031041,1);
    cm.gainItem(prizes[choice],1);
    cm.warp(103000100, 0);
    cm.dispose();
}