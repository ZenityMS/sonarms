/*	
	Author: Traitor
	NPC:    dojo guy
	Map(s):	Dojo Hall
*/
importPackage(net.sf.odinms.server.maps);

var belts = Array(1132000, 1132001, 1132002, 1132003, 1132004);
var belt_level = Array(25, 35, 45, 60, 75);
var belt_points = Array(200, 1800, 4000, 9200, 17000);

var status = -1;
var selectedMenu = -1;

function start() {
	if (isRestingSpot(cm.getPlayer().getMap().getId())) {
		cm.sendSimple("I'm surprised you made it this far! But it won't be easy from here on out. You still want the challenge?\r\n\r\n#b#L0#I want to continue#l\r\n#L1#I want to leave#l\r\n#L2#I want to record my score up to this points#l");
	} else if (cm.getPlayer().getLevel() >= 25) {
		if (cm.getPlayer().getMap().getId() == 925020001) {
			cm.sendSimple("My master is the strongest person in Mu Lung, and you want to challenge him? Fine, but you'll regret it later.\r\n\r\n#b#L0#I want to challenge him alone.#l\r\n#L1#I want to challenge him with a party.#l\r\n\r\n#L2#I want to receive a belt.#l\r\n#L3#I want to reset my training points.#l\r\n#L4#I want to receive a medal.#l\r\n#L5#What is a Mu Lung Dojo?#l");
		} else {
			cm.sendYesNo("What, you're giving up? You just need to get to the next level! Do you really want to quit and leave?");
		}
	} else {
		cm.sendOk("Hey! Are you mocking my master? Who do you think you are to challenge him? This is a joke! You should at least be level #b25#k.");
		cm.dispose();
	}
}

function action(mode, type, selection) {
	if (cm.getPlayer().getMap().getId() == 925020001) {
		if (mode >= 0) {
			if (status == -1)
				selectedMenu = selection;
			status++; //there is no prev.
			
			if (selectedMenu == 0) { //I want to challenge him alone.
				if (!cm.getPlayer().hasEntered("dojang_Msg")) { //kind of hackish...
					if (status == 0) {
						cm.sendYesNo("Hey there! You! This is your first time, huh? Well, my master doesn't just meet with anyone. He's a busy man. And judging by your looks, I don't think he'd bother. Ha! But, today's your lucky day... I tell you what, if you can defeat me, I'll allow you to see my Master. So what do you say?");
					} else if (status == 1) {
						if (mode == 0) {
							cm.sendNext("Haha! Who are you trying to impress with a heart like that?\r\nGo back home where you belong!");
						} else {
							cm.warp(925020010, 0);
						}
						cm.dispose();
					}
				//} else if (cm.getPlayer().getLastDojoStage() > 0) {
				//	if (status == 0) {
				//		cm.sendYesNo("The last time you took the challenge by yourself, you went up to level " + cm.getPlayer().getLastDojoStage() + ". I can take you there right now. Do you want to go there?");
				//	} else {
				//		if (mode == 0) {
				//			cm.warp(925020100, 0);
				//		} else {
				//			cm.warp(stage, 0);
				//		}
				//	}
				} else {
					cm.getClient().getChannelServer().getMapFactory().getMap(925020100).resetReactors();
					cm.getClient().getChannelServer().getMapFactory().getMap(925020100).killAllMonsters(false);
					cm.warp(925020100, 0);
					cm.dispose();
				}
			} else if (selectedMenu == 1) { //I want to challenge him with a party.
				var party = cm.getPlayer().getParty();
				if (party == null || party.getLeader().getId() != cm.getPlayer().getId()) {
					cm.sendNext("Where do you think you're going? You're not even the party leader! Go tell your party leader to talk to me.");
					cm.dispose();
				} else if (party.getMembers().size() == 1) {
					cm.sendNext("You're going to take on the challenge as a one-man party?");
				} else {
				
				}
				cm.dispose();
			} else if (selectedMenu == 2) { //I want to receive a belt.
				if (status == 0) {
					var selStr = "You have #b0#k training points. Master prefers those with great talent. If you obtain more points than the average, you can receive a belt depending on your score.\r\n";
					for (var i = 0; i < belts.length; i++)
						selStr += "\r\n#L" + i + "##i" + belts[i] + "# #t" + belts[i] + "#l";
					cm.sendSimple(selStr);
				} else if (status == 1) {
					var belt = belts[selection];
					var level = belt_level[selection];
					var points = belt_points[selection];
					cm.sendNext("In order to receive #i" + belt + "# #b#t" + belt + "##k, you have to be at least over level #b" + level + "#k and you need to have earned at least #b" + points + " training points#k.\r\n\r\nIf you want to obtain this belt, you need #r" + points + "#k more training points.");
					cm.dispose();
				}
			} else if (selectedMenu == 3) { //I want to reset my training points.
				if (status == 0) {
					cm.sendYesNo("You do know that if you reset your training points, it returns to 0, right? Although, that's not always a bad thing. If you can start earning training points again after you reset, you can receive the belts once more. Do you want to reset your training points now?");
				} else if (status == 1) {
					if (mode == 0) {
						cm.sendNext("Do you need to gather yourself or something? Come back after you take a deep breath.");
					} else {
						//TODO: reset points lol
						cm.sendNext("There! All your training points have been reset. Think of it as a new beginning and train hard!");
					}
					cm.dispose();
				}
			} else if (selectedMenu == 4) { //I want to receive a medal.
				if (status == 0) {
					cm.sendYesNo("You haven't attempted the medal yet? If you defeat one type of monster in Mu Lung Dojo #b100 times#k you can receive a title called #bxx Vanquisher#k. It looks like you haven't even earned the #b#t1142033##k... Do you want to try out for the #b#t1142033##k?");
				} else if (status == 1) {
					if (mode == 0) {
						cm.sendNext("If you don't want to, that's fine.");
						cm.dispose();
					} else {
					
					}
					cm.dispose();
				}
			} else if (selectedMenu == 5) { //What is a Mu Lung Dojo?
				cm.sendNext("Our master is the strongest person in Mu Lung. The place he built is called the Mu Lung Dojo, a building that is 38 stories tall! You can train yourself as you go up each level. Of course, it'll be heard for someone at your level to reach the top.");
				cm.dispose();
			}
		} else
			cm.dispose();
	} else if (isRestingSpot(cm.getPlayer().getMap().getId())) {
		if (selectedMenu == -1)
			selectedMenu = selection;
		status++;
		
		if (selectedMenu == 0) {
			cm.warp(cm.getPlayer().getMap().getId() + 100, 0);
			cm.dispose();
		} else if (selectedMenu == 1) { //I want to leave
			if (status == 0) {
				cm.sendAcceptDecline("So, you're giving up? You're really going to leave?");
			} else {
				if (mode == 0) {
					//warp them out?
				} else {
					//what.
				}
				cm.dispose();
			}
		} else if (selectedMenu == 2) { //I want to record my score up to this point
			if (status == 0) {
				cm.sendYesNo("If you record your score, you can start where you left off the next time. Isn't that convenient? Do you want to record your current score?");
			} else {
				if (mode == 0) {
					cm.sendNext("You think you can go even higher? Good luck!");
				} else {
					cm.sendNext("I recorded your score. If you tell me the next time you go up, you'll be able to start where you left off.");
					//cm.getPlayer().recordDojoScore(stage);
				}
				cm.dispose();
			}
		}
	} else {
		if (mode <= 0) {
			cm.sendNext("Stop changing your mind! Soon, you'll be crying, begging me to go back.");
		} else {
			cm.warp(925020001, 0);
		}
		cm.dispose();
	}
}

function isRestingSpot(id) {
    // Resting rooms :
    // 925020600 ~ 925020609
    // 925021200 ~ 925021209
    // 925021800 ~ 925021809
    // 925022400 ~ 925022409
    // 925023000 ~ 925023009
    // 925023600 ~ 925023609
    var shortid = id / 100;

    switch (shortid) {
	case 9250206:
	case 9250212:
	case 9250218:
	case 9250224:
	case 9250230:
	case 9250236:
	    return true;
    }
    return false;
}