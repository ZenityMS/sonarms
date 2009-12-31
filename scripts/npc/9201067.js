/*
Base Script by Luke(Roamer in RaGEZONE)
Gambling NPC by Tommy of SonarMS
*/

importPackage(net.sf.odinms.client);

var status = 0;
var fee;
var chance = Math.floor(Math.random()*4+1);

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0) {
			cm.sendOk("Okay. See ya later!");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			cm.sendNext("Hello #h #! I am the Gambler NPC of #bSonarMS#k! If you would like to gamble, then please proceed!");
		} else if (status == 1) {
			cm.sendGetText("How much mesos would you like to gamble?");
		} else if (status == 2) {
			fee = cm.getText();
			cm.sendYesNo("Are you sure you want to gamble #r" + fee + "#k mesos?");
		} else if (status == 3) {
			if (cm.getMeso() < fee) {
				cm.sendOk("You don't have enough mesos!");
				cm.dispose();
			} else if (cm.getMeso() >= 1050000000) {
				cm.sendOk("I'm sorry but you can't gamble when you have 1,050,000,000 mesos or higher. Try trading some in and try again later.");
				cm.dispose();
			} else if (cm.getText() < 0) {
				cm.sendOk("You can't gamble if you have no mesos!");
				cm.dispose();
			} else {
				if (chance <= 1) {
					cm.gainMeso(-fee);
					cm.sendNext("Tough luck... Maybe next time you will win!");
					cm.dispose();
				}
				else if (chance == 2) {
					cm.gainMeso(-fee);
					cm.sendNext("Tough luck... Maybe next time you will win!");
					cm.dispose();
				}
				else if (chance == 3) {
					cm.gainMeso(-fee);
					cm.sendNext("Tough luck... Maybe next time you will win!");
					cm.dispose();
				}
				else if (chance >= 4) {
					cm.gainMeso(fee * 2);
					cm.sendNext("#rCONGRATULATIONS#k! You won!");
					cm.dispose();
				}
			}
		}
	}
}
