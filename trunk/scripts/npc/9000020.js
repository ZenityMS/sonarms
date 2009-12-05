var bossmaps = Array(100000005, 105070002, 105090900, 230040420, 280030000, 220080001, 240020402, 240020101, 801040100, 240060200, 610010005, 610010012, 610010013, 610010100, 610010101, 610010102, 610010103, 610010104); 
var monstermaps = Array(100040001, 101010100, 104040000, 103000101, 103000105, 101030110, 106000002, 101030103, 101040001, 101040003, 101030001, 104010001, 105070001, 105090300, 105040306, 230020000, 230010400, 211041400, 222010000, 220080000, 220070301, 220070201, 220050300, 220010500, 250020000, 251010000, 200040000, 200010301, 240020100, 240040500, 240040000, 600020300, 801040004, 800020130); 
var townmaps = Array(1010000, 680000000, 230000000, 101000000, 211000000, 0, 100000000, 251000000, 103000000, 222000000, 104000000, 240000000, 220000000, 250000000, 800000000, 600000000, 221000000, 200000000, 102000000, 801000000, 105040300, 60000, 610010004, 260000000, 540010000, 120000000); 
var chosenMap = -1;
var monsters = 0;
var towns = 0;
var bosses = 0;

importPackage(net.sf.odinms.client);

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    }
    else {
        if (status >= 3 && mode == 0) {
            cm.sendOk("See you next time!.");
            cm.dispose();
            return;    
        }
        if (mode == 1) {
            status++;
        }
        else {
            status--;
        }
        if (status == 0) {
            cm.sendNext("Hey I'm the Travel NPC what can I do for you?");
        }
        if (status == 1) {
            cm.sendSimple("#fUI/UIWindow.img/QuestIcon/3/0#\r\n#L0#World Tour#l\r\n#L1#Leave#l\r\n#L2#Job Advancer#l\r\n#L3#AIO Shop#l\r\n#L4#Reward NPC#l");
        }
        else if (status == 2) {
            if (selection == 0) {
                cm.sendSimple("#fUI/UIWindow.img/QuestIcon/3/0#\r\n#L0#Towns#l\r\n#L1#Monstermaps#l\r\n#L2#Bossmaps#l");
            }
            else if (selection == 1) {
                cm.dispose();
            }
			
			else if (selection == 2) {
			cm.dispose();
               	cm.openNpc(9001001);
            }
			else if (selection == 3) {
			cm.dispose();
                cm.openNpc(9001003);
            }
			else if (selection == 4) {
		cm.dispose();
                cm.openNpc(9001002);
            }
        }
        else if (status == 3) {
            if (selection == 0) {
                var selStr = "Select your destination.#b";
                for (var i = 0; i < townmaps.length; i++) {
                    selStr += "\r\n#L" + i + "##m" + townmaps[i] + "#";
                }
                cm.sendSimple(selStr);
                towns = 1;
            }
            if (selection == 1) {
                var selStr = "Select your destination.#b";
                for (var i = 0; i < monstermaps.length; i++) {
                    selStr += "\r\n#L" + i + "##m" + monstermaps[i] + "#";
                }
                cm.sendSimple(selStr);
                monsters = 1;
            }
            if (selection == 2) {
                var selStr = "Select your destination.#b";
                for (var i = 0; i < bossmaps.length; i++) {
                    selStr += "\r\n#L" + i + "##m" + bossmaps[i] + "#";
                }
                cm.sendSimple(selStr);
                bosses = 1;
            }
        }
        else if (status == 4) {
            if (towns == 1) {
                cm.sendYesNo("Do you want to go to #m" + townmaps[selection] + "#?");
                chosenMap = selection;
                towns = 2;
            }
            else if (monsters == 1) {
                cm.sendYesNo("Do you want to go to #m" + monstermaps[selection] + "#?");
                chosenMap = selection;
                monsters = 2;
            }
            else if (bosses == 1) {
                cm.sendYesNo("Do you want to go to #m" + bossmaps[selection] + "#?");
                chosenMap = selection;
                bosses = 2;
            }
        }
        else if (status == 5) {
            if (towns == 2) {
                cm.warp(townmaps[chosenMap], 0);
                cm.dispose();
            }
            else if (monsters == 2) {
                cm.warp(monstermaps[chosenMap], 0);
                cm.dispose();
            }
            else if (bosses == 2) {
                cm.warp(bossmaps[chosenMap], 0);
                cm.dispose();
            }
        }
              
    }
}