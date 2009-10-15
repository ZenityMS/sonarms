/* John JQ Flower pile #1

*/

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    var prizes = Array(1040051, 1040127, 1040128, 1040133, 1040138, 1041000, 1041001, 1041110, 1041130, 1041139, 1042015, 1042017, 1042023, 1702001, 1702025);
    var chances = Array(10, 10, 10, 10, 5, 10, 10, 10, 10, 5, 8, 8, 8, 9, 7);
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
    if (cm.isQuestStarted(2052))
        cm.gainItem(4031025,10);
    cm.gainItem(prizes[choice],1);
    cm.warp(105040300, 0);
    cm.dispose();
}