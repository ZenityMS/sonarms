/* Credits: Mikethemak of Ragezone
    Modified by iGoofy
    Modified by JakesDexless
	Modified by Tommy of SonarMS
*/
var maps = [[100000005, 105070002, 105090900, 230040420, 280030000, 220080001, 240020402, 240020**** 801040**** 240060200],[100040001, 101010**** 104040000, 103000**** 103000**** 101030**** 106000002, 101030103, 101040001, 101040003, 101030001, 104010001, 105070001, 105090300, 105040306, 230020000, 230010400, 211041400, 222010000, 220080000, 220070301, 220070201, 220050300, 220010500, 250020000, 251010000, 200040000, 200010301, 240020**** 240040500, 240040000, 600020300, 801040004, 800020130],[100000000, 680000000, 230000000, 101000000, 211000000, 100000000, 100000000, 251000000, 103000000, 222000000, 104000000, 240000000, 220000000, 250000000, 800000000, 600000000, 221000000, 200000000, 102000000, 801000000, 105040300, 100000000]]
var maptype;
var chosen;
var status = 0;

function start() {
    cm.sendNext("Hey! I'm the SonarMS warper. What can I do for you?");
}

function action(mode, type, selection) {
    if (mode < 0) {
        cm.dispose();
    } else {
        if (status >= 2 && mode == 0) {
            cm.sendOk("Well, okay.");
            cm.dispose();
            return;                    
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1) {
            cm.sendSimple("#L0#Teleport#l\r\n#L1#Nevermind#l");
        } else if (status == 2) {
            if (selection == 0)
                cm.sendSimple("#L0#Towns#l\r\n#L1#Bosses#l\r\n#L2#MonsterMaps#l\r\n#L3#Nevermind#l");
            else
                cm.dispose();
        } else if (status == 3) {
            var text = "Just type in the number of the map you wish to go to!";
            maptype = selection;
            if (maptype < 3) {
                for (var i = 0; i < maps[maptype].length; i++) {
                    text += "\r\n (" + i + ") #m" + maps[maptype][i] + "#";
                }
                cm.sendGetNumber(text, 0, 0, maps[maptype].length);
            } else
                cm.dispose();
        } else if (status == 4) {
            chosen = selection;
            cm.sendYesNo("Are you sure you want to go to #b#m" + maps[maptype][chosen] + "##k?");
        } else if (status == 5) {
            cm.warp(maps[maptype][chosen]);
            cm.dispose();
        }
    }
}  