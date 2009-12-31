importPackage(net.sf.odinms.client);

var status = 0;
var jobName;
var job;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.sendOk("Well okay then. Come back if you change your mind.\r\n\r\nGood luck on your training.");
        cm.dispose();
    } else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            cm.sendNext("Hello, I'm in charge of Job Advancing.");
        } else if (status == 1) {
            if (cm.getLevel() < 200 && cm.getJob().equals(net.sf.odinms.client.MapleJob.BEGINNER)) {
                if (cm.getLevel() < 8) {
                    cm.sendNext("Sorry, but you have to be at least level 8 to use my services.");
                    status = 98;
                } else if (cm.getLevel() < 10) {
                    cm.sendYesNo("Congratulations of reaching such a high level. Would you like to make the #rFirst Job Advancement#k as a #rMagician#k?");
                    status = 150;
                } else {
                    cm.sendYesNo("Congratulations on reaching such a high level. Would you like to make the #rFirst Job Advancement#k?");
                    status = 153;
                }
            } else if (cm.getLevel() < 30) {
                cm.sendNext("Sorry, but you have to be at least level 30 to make the #rSecond Job Advancement#k.");
                status = 98;
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.THIEF)) {
                cm.sendSimple("Congratulations on reaching such a high level. Which would you like to be? #b\r\n#L0#Assassin#l\r\n#L1#Bandit#l#k");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.WARRIOR)) {
                cm.sendSimple("Congratulations on reaching such a high level. Which would you like to be? #b\r\n#L2#Fighter#l\r\n#L3#Page#l\r\n#L4#Spearman#l#k");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.MAGICIAN)) {
                cm.sendSimple("Congratulations on reaching such a high level. Which would you like to be? #b\r\n#L5#Ice Lightning Wizard#l\r\n#L6#Fire Poison Wizard#l\r\n#L7#Cleric#l#k");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.BOWMAN)) {
                cm.sendSimple("Congratulations on reaching such a high level. Which would you like to be? #b\r\n#L8#Hunter#l\r\n#L9#Crossbowman#l#k");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.PIRATE)) {
                cm.sendSimple("Congratulations on reaching such a high level. Which would you like to be? #b\r\n#L10#Brawler#l\r\n#L11#Gunslinger#l#k");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.BLAZEWIZARD1)) {
                cm.sendSimple("Congratulations on reaching such a high level. Do you want to job advance? #b\r\n#L12#Yes#l\r\n#L13#No#l#k");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.DAWNWARRIOR1)) {
                cm.sendSimple("Congratulations on reaching such a high level. Do you want to job advance? #b\r\n#L14#Yes#l\r\n#L15#No#l#k");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.NIGHTWALKER1)) {
                cm.sendSimple("Congratulations on reaching such a high level. Do you want to job advance? #b\r\n#L16#Yes#l\r\n#L17#No#l#k");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.WINDARCHER1)) {
                cm.sendSimple("Congratulations on reaching such a high level. Do you want to job advance? #b\r\n#L18#Yes#l\r\n#L19#No#l#k");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.THUNDERBREAKER1)) {
                cm.sendSimple("Congratulations on reaching such a high level. Do you want to job advance? #b\r\n#L20#Yes#l\r\n#L21#No#l#k");
            } else if (cm.getLevel() < 70) {
                cm.sendNext("Sorry, but you have to be at least level 70 to make the #rThird Job Advancement#k.");
                status = 98;
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.ASSASSIN)) {
                status = 63;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.BANDIT)) {
                status = 66;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.HUNTER)) {
                status = 69;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.CROSSBOWMAN)) {
                status = 72;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.FP_WIZARD)) {
                status = 75;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.IL_WIZARD)) {
                status = 78;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.CLERIC)) {
                status = 81;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.FIGHTER)) {
                status = 84;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.PAGE)) {
                status = 87;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.SPEARMAN)) {
                status = 90;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.GUNSLINGER)) {
                status = 95;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.BRAWLER)) {
                status = 92;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.BLAZEWIZARD2)) {
                status = 169;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.DAWNWARRIOR2)) {
                status = 172;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.NIGHTWALKER2)) {
                status = 175;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.WINDARCHER2)) {
                status = 178;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.THUNDERBREAKER2)) {
                status = 181;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getLevel() < 120) {
                cm.sendNext("Sorry, but you have to be at least level 120 to make the #rForth Job Advancement#k.");
                status = 98;
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.HERMIT)) {
                status = 105;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.CHIEFBANDIT)) {
                status = 108;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.RANGER)) {
                status = 111;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.SNIPER)) {
                status = 114;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.FP_MAGE)) {
                status = 117;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.IL_MAGE)) {
                status = 120;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.PRIEST)) {
                status = 123;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.CRUSADER)) {
                status = 126;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.WHITEKNIGHT)) {
                status = 129;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.DRAGONKNIGHT)) {
                status = 132;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.MARAUDER)) {
                status = 133;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.OUTLAW)) {
                status = 134;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getLevel() < 200) {
                cm.sendNext("Sorry, but you have already attained the highest level of your job's mastery. \r\n\r\nHowever, you can #rrebirth#k when you are level 200.");
                status = 98;
            } else if (cm.getLevel() >= 200) {
                cm.sendYesNo("Hello, its so good to see you again! Wow, you have already reached your maximum level. \r\n But, to continue to become stronger, you will have to reborn. \r\n This decision is crucial, do you want to do it?");
                status = 160;
            } else {
                cm.dispose();
            }
        } else if (status == 2) {
            if (selection == 0) {
                jobName = "Assassin";
                job = net.sf.odinms.client.MapleJob.ASSASSIN;
            }
            if (selection == 1) {
                jobName = "Bandit";
                job = net.sf.odinms.client.MapleJob.BANDIT;
            }
            if (selection == 2) {
                jobName = "Fighter";
                job = net.sf.odinms.client.MapleJob.FIGHTER;
            }
            if (selection == 3) {
                jobName = "Page";
                job = net.sf.odinms.client.MapleJob.PAGE;
            }
            if (selection == 4) {
                jobName = "Spearman";
                job = net.sf.odinms.client.MapleJob.SPEARMAN;
            }
            if (selection == 5) {
                jobName = "Ice Lightning Wizard";
                job = net.sf.odinms.client.MapleJob.IL_WIZARD;
            }
            if (selection == 6) {
                jobName = "Fire Poison Wizard";
                job = net.sf.odinms.client.MapleJob.FP_WIZARD;
            }
            if (selection == 7) {
                jobName = "Cleric";
                job = net.sf.odinms.client.MapleJob.CLERIC;
            }
            if (selection == 8) {
                jobName = "Hunter";
                job = net.sf.odinms.client.MapleJob.HUNTER;
            }
            if (selection == 9) {
                jobName = "Crossbowman";
                job = net.sf.odinms.client.MapleJob.CROSSBOWMAN;
            }
            if (selection == 10) {
                jobName = "Brawler";
                job = net.sf.odinms.client.MapleJob.BRAWLER;
            }
            if (selection == 11) {
                jobName = "Gunslinger";
                job = net.sf.odinms.client.MapleJob.GUNSLINGER;
            }
            if (selection == 12) {
                jobName = "Level 2 Blaze Wizard";
                job = net.sf.odinms.client.MapleJob.BLAZEWIZARD2;
            }
            if (selection == 13) {
                cm.sendOk("Come back to me when you are ready.");
                cm.dispose();
            }
            if (selection == 14) {
                jobName = "Level 2 Dawn Warrior";
                job = net.sf.odinms.client.MapleJob.DAWNWARRIOR2;
            }
            if (selection == 15) {
                cm.sendOk("Come back to me when you are ready.");
                cm.dispose();
            }
            if (selection == 16) {
                jobName = "Level 2 Night Walker";
                job = net.sf.odinms.client.MapleJob.NIGHTWALKER2;
            }
            if (selection == 17) {
                cm.sendOk("Come back to me when you are ready.");
                cm.dispose();
            }
            if (selection == 18) {
                jobName = "Level 2 Wind Archer";
                job = net.sf.odinms.client.MapleJob.WINDARCHER2;
            }
            if (selection == 19) {
                cm.sendOk("Come back to me when you are ready.");
                cm.dispose();
            }
            if (selection == 20) {
                jobName = "Level 2 Thunder Breaker";
                job = net.sf.odinms.client.MapleJob.THUNDERBREAKER2;
            }
            if (selection == 21) {
                cm.sendOk("Come back to me when you are ready.");
                cm.dispose();
            }            cm.sendYesNo("Do you want to become a #r" + jobName + "#k?");
        } else if (status == 3) {
            cm.changeJob(job);
            cm.sendOk("There you go. Hope you enjoy it. See you around in the future maybe :)");
            cm.dispose();
        } else if (status == 61) {
            if (cm.getJob().equals(net.sf.odinms.client.MapleJob.ASSASSIN)) {
                status = 63;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.BANDIT)) {
                status = 66;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.HUNTER)) {
                status = 69;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.CROSSBOWMAN)) {
                status = 72;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.FP_WIZARD)) {
                status = 75;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.IL_WIZARD)) {
                status = 78;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.CLERIC)) {
                status = 81;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.FIGHTER)) {
                status = 84;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.PAGE)) {
                status = 87;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.SPEARMAN)) {
                status = 90;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.GUNSLINGER)) {
                status = 98;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.BRAWLER)) {
                status = 93;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.BLAZEWIZARD2)) {
                status = 170;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.DAWNWARRIOR2)) {
                status = 173;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.NIGHTWALKER2)) {
                status = 176;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.WINDARCHER2)) {
                status = 179;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.THUNDERBREAKER2)) {
                status = 182;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else {
                cm.dispose();
            }
        } else if (status == 64) {
            cm.changeJob(MapleJob.HERMIT);
            cm.sendOk("There you go. Hope you enjoy it. See you around in the future maybe :)");
            cm.dispose();
        } else if (status == 67) {
            cm.changeJob(MapleJob.CHIEFBANDIT);
            cm.sendOk("There you go. Hope you enjoy it. See you around in the future maybe :)");
            cm.dispose();
        } else if (status == 70) {
            cm.changeJob(MapleJob.RANGER);
            cm.sendOk("There you go. Hope you enjoy it. See you around in the future maybe :)");
            cm.dispose();
        } else if (status == 73) {
            cm.changeJob(MapleJob.SNIPER);
            cm.sendOk("There you go. Hope you enjoy it. See you around in the future maybe :)");
            cm.dispose();
        } else if (status == 76) {
            cm.changeJob(MapleJob.FP_MAGE);
            cm.sendOk("There you go. Hope you enjoy it. See you around in the future maybe :)");
            cm.dispose();
        } else if (status == 79) {
            cm.changeJob(MapleJob.IL_MAGE);
            cm.sendOk("There you go. Hope you enjoy it. See you around in the future maybe :)");
            cm.dispose();
        } else if (status == 82) {
            cm.changeJob(MapleJob.PRIEST);
            cm.sendOk("There you go. Hope you enjoy it. See you around in the future maybe :)");
            cm.dispose();
        } else if (status == 85) {
            cm.changeJob(MapleJob.CRUSADER);
            cm.sendOk("There you go. Hope you enjoy it. See you around in the future maybe :)");
            cm.dispose();
        } else if (status == 88) {
            cm.changeJob(MapleJob.WHITEKNIGHT);
            cm.sendOk("There you go. Hope you enjoy it. See you around in the future maybe :)");
            cm.dispose();
        } else if (status == 91) {
            cm.changeJob(MapleJob.DRAGONKNIGHT);
            cm.sendOk("There you go. Hope you enjoy it. See you around in the future maybe :)");
            cm.dispose();
        } else if (status == 93) {
            cm.changeJob(MapleJob.MARAUDER);
            cm.sendOk("There you go. Hope you enjoy it. See you around in the future maybe :)");
            cm.dispose();
        } else if (status == 96) {
            cm.changeJob(MapleJob.OUTLAW);
            cm.sendOk("There you go. Hope you enjoy it. See you around in the future maybe :)");
            cm.dispose();
        } else if (status == 170) {
            cm.changeJob(MapleJob.BLAZEWIZARD3);
            cm.sendOk("There you go. Hope you enjoy it. See you around in the future maybe :)");
            cm.dispose();
        } else if (status == 173) {
            cm.changeJob(MapleJob.DAWNWARRIOR3);
            cm.sendOk("There you go. Hope you enjoy it. See you around in the future maybe :)");
            cm.dispose();
        } else if (status == 176) {
            cm.changeJob(MapleJob.NIGHTWALKER3);
            cm.sendOk("There you go. Hope you enjoy it. See you around in the future maybe :)");
            cm.dispose();
        } else if (status == 179) {
            cm.changeJob(MapleJob.WINDARCHER3);
            cm.sendOk("There you go. Hope you enjoy it. See you around in the future maybe :)");
            cm.dispose();
        } else if (status == 182) {
            cm.changeJob(MapleJob.THUNDERBREAKER3);
            cm.sendOk("There you go. Hope you enjoy it. See you around in the future maybe :)");
            cm.dispose();
        } else if (status == 99) {
            cm.sendOk("Good luck on your training.");
            cm.dispose();
        } else if (status == 102) {
            if (cm.getJob().equals(net.sf.odinms.client.MapleJob.HERMIT)) {
                status = 105;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.CHIEFBANDIT)) {
                status = 108;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.RANGER)) {
                status = 111;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.SNIPER)) {
                status = 114;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.FP_MAGE)) {
                status = 117;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.IL_MAGE)) {
                status = 120;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.PRIEST)) {
                status = 123;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.CRUSADER)) {
                status = 126;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.WHITEKNIGHT)) {
                status = 129;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.DRAGONKNIGHT)) {
                status = 132;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.MARAUDER)) {
                status = 134;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else if (cm.getJob().equals(net.sf.odinms.client.MapleJob.OUTLAW)) {
                status = 136;
                cm.sendYesNo("Congratulations on reaching such a high level. Do you want to Job Advance now?");
            } else {
                cm.dispose();
            }
        } else if (status == 106) {
            cm.changeJob(MapleJob.NIGHTLORD);
	    cm.teachSkill(4121009, 0, 5); //Hero's Will
			cm.teachSkill(4120002, 0, 30); //Shadow Shifter
			cm.teachSkill(4121000, 0, 30); //Maple Warrior
			cm.teachSkill(4121004, 0, 30); //Ninja Ambush
			cm.teachSkill(4121008, 0, 30); //Ninja Storm
			cm.teachSkill(4121003, 0, 30); //Taunt
			cm.teachSkill(4121006, 0, 30); //Spirit Claw
			cm.teachSkill(4121007, 0, 30); //Triple Throw
			cm.teachSkill(4120005, 0, 30); //Venomous Star
            cm.sendOk("There you go. Hope you enjoy it.");
            cm.dispose();
        } else if (status == 109) {
            cm.changeJob(MapleJob.SHADOWER);
	    cm.teachSkill(4221001, 0, 30); //Assassinate
			cm.teachSkill(4221008, 0, 5); //Hero's Will
			cm.teachSkill(4221007, 0, 30); //Boomerang Step
			cm.teachSkill(4220002, 0, 30); //Shadow Shifter
			cm.teachSkill(4221000, 0, 30); //Maple Warrior
			cm.teachSkill(4221004, 0, 30); //Ninja Ambush
			cm.teachSkill(4221003, 0, 30); //Taunt
			cm.teachSkill(4221006, 0, 30); //Smokescreen
			cm.teachSkill(4220005, 0, 30); //Venomous Dagger
            cm.sendOk("There you go. Hope you enjoy it.");
            cm.dispose();
        } else if (status == 112) {
            cm.changeJob(MapleJob.BOWMASTER);
	    cm.teachSkill(3121009, 5); //Hero's Will
			cm.teachSkill(3120005, 30); //Bow Expert
			cm.teachSkill(3121008, 30); //Concentration
			cm.teachSkill(3121003, 30); //Dragon Pulse
			cm.teachSkill(3121007, 30); //Hamstring
			cm.teachSkill(3121000, 30); //Maple Warrior
			cm.teachSkill(3121006, 30); //Phoenix
			cm.teachSkill(3121002, 30); //Sharp Eyes
			cm.teachSkill(3121004, 30); //Storm Arrow
            cm.sendOk("There you go. Hope you enjoy it.");
            cm.dispose();
        } else if (status == 115) {
            cm.changeJob(MapleJob.MARKSMAN);
	    cm.teachSkill(3221008, 0, 5); //Hero's Will
			cm.teachSkill(3221006, 0, 30); //Blind
			cm.teachSkill(3220004, 0, 30); //Crossbow Expert
			cm.teachSkill(3221003, 0, 30); //Dragon Pulse
			cm.teachSkill(3221005, 0, 30); //Freezer
			cm.teachSkill(3221000, 0, 30); //Maple Warrior
			cm.teachSkill(3221001, 0, 30); //Piercing
			cm.teachSkill(3221002, 0, 30); //Sharp Eyes
			cm.teachSkill(3221007, 0, 30); //Sniping
            cm.sendOk("There you go. Hope you enjoy it.");
            cm.dispose();
        } else if (status == 118) {
            cm.changeJob(MapleJob.FP_ARCHMAGE);
	    cm.teachSkill(2121008, 5); //Hero's Will
			cm.teachSkill(2121001, 0, 30); //Big Bang
			cm.teachSkill(2121005, 0, 30); //Elquines
			cm.teachSkill(2121003, 0, 30); //Fire Demon
			cm.teachSkill(2121004, 0, 30); //Infinity
			cm.teachSkill(2121002, 0, 30); //Mana Reflection
			cm.teachSkill(2121000, 0, 30); //Maple Warrior
			cm.teachSkill(2121007, 0, 30); //Meteo
			cm.teachSkill(2121006, 0, 30); //Paralyze
            cm.sendOk("There you go. Hope you enjoy it.");
            cm.dispose();
        } else if (status == 121) {
            cm.changeJob(MapleJob.IL_ARCHMAGE);
	    cm.teachSkill(2221008, 0, 30); //Hero's Will
			cm.teachSkill(2221001, 0, 30); //Big Bang
			cm.teachSkill(2221007, 0, 30); //Blizzard
			cm.teachSkill(2221006, 0, 30); //Chain Lightning
			cm.teachSkill(2221003, 0, 30); //Ice Demon
			cm.teachSkill(2221005, 0, 30); //Ifrit
			cm.teachSkill(2221004, 0, 30); //Infinity
			cm.teachSkill(2221002, 0, 30); //Mana Reflection
			cm.teachSkill(2221000, 0, 30); //Maple Warrior
            cm.sendOk("There you go. Hope you enjoy it.");
            cm.dispose();
        } else if (status == 124) {
            cm.changeJob(MapleJob.BISHOP);
		cm.teachSkill(2321007, 0, 30); //Angel's Ray
			cm.teachSkill(2321009, 0, 5); //Hero's Will
			cm.teachSkill(2321003, 0, 30); //Bahamut
			cm.teachSkill(2321001, 0, 30); //Big Bang
			cm.teachSkill(2321008, 0, 30); //Genesis
			cm.teachSkill(2321005, 0, 30); //Holy Shield
			cm.teachSkill(2321004, 0, 30); //Infinity
			cm.teachSkill(2321002, 0, 30); //Mana Reflection
			cm.teachSkill(2321000, 0, 30); //Maple Warrior
			cm.teachSkill(2321006, 0, 10); //Resurrection
            cm.sendOk("There you go. Hope you enjoy it.");
            cm.dispose();
        } else if (status == 127) {
            cm.changeJob(MapleJob.HERO);
	    cm.teachSkill(1120004, 0, 30); //Achilles
			cm.teachSkill(1120003, 0, 30); //Advanced Combo
			cm.teachSkill(1121011, 0, 5); //Hero's Will
			cm.teachSkill(1120005, 0, 30); //Blocking
			cm.teachSkill(1121008, 0, 30); //Brandish
			cm.teachSkill(1121010, 0, 30); //Enrage
			cm.teachSkill(1121000, 0, 30); //Maple Warrior
			cm.teachSkill(1121001, 0, 30); //Monster Magnet
			cm.teachSkill(1121006, 0, 30); //Rush
			cm.teachSkill(1121002, 0, 30); //Stance
            cm.sendOk("There you go. Hope you enjoy it.");
            cm.dispose();
        } else if (status == 130) {
            cm.changeJob(MapleJob.PALADIN);
	    cm.teachSkill(1220005, 0, 30); //Achilles
			cm.teachSkill(1220010, 0, 10); //Advanced Charge
			cm.teachSkill(1221012, 0, 5); //Hero's Will
			cm.teachSkill(1221009, 0, 30); //Blast
			cm.teachSkill(1220006, 0, 30); //Blocking
			cm.teachSkill(1221004, 0, 20); //Divine Charge: Mace
			cm.teachSkill(1221003, 0, 20); //Holy Charge: Sword
			cm.teachSkill(1221000, 0, 30); //Maple Warrior
			cm.teachSkill(1221001, 0, 30); //Monster Magnet
			cm.teachSkill(1221007, 0, 30); //Rush
			cm.teachSkill(1221011, 0, 30); //Sanctuary
			cm.teachSkill(1221002, 0, 30); //Stance
            cm.sendOk("There you go. Hope you enjoy it.");
            cm.dispose();
        } else if (status == 133) {
            cm.changeJob(MapleJob.DARKKNIGHT);
	   cm.teachSkill(1320005, 0, 30); //Achilles
			cm.teachSkill(1321010, 0, 5); //Hero's Will
			cm.teachSkill(1321007, 0, 10); //Beholder
			cm.teachSkill(1320009, 0, 25); //Beholder's Buff
			cm.teachSkill(1320008, 0, 25); //Beholder's Healing
			cm.teachSkill(1320006, 0, 30); //Berserk
			cm.teachSkill(1321000, 0, 30); //Maple Warrior
			cm.teachSkill(1321001, 0, 30); //Monster Magnet
			cm.teachSkill(1321003, 0, 30); //Rush
			cm.teachSkill(1321002, 0, 30); //Stance
            cm.sendOk("There you go. Hope you enjoy it.");
            cm.dispose();
        } else if (status == 134) {
            cm.changeJob(MapleJob.BUCCANEER);
	    cm.teachSkill(5121000, 30); //Maple Warrior
			cm.teachSkill(5121001, 30); //Dragon Strike
			cm.teachSkill(5121002, 30); //Energy Orb
			cm.teachSkill(5121003, 20); //Super Transformation
			cm.teachSkill(5121004, 30); //Demolition
			cm.teachSkill(5121005, 30); //Snatch
			cm.teachSkill(5121007, 30); //Barrage
			cm.teachSkill(5121008, 5); //Pirate's Rage
			cm.teachSkill(5121009, 20); //Speed Infusion
			cm.teachSkill(5121010, 30); //Time Leap
            cm.sendOk("There you go. Hope you enjoy it.");
            cm.dispose();
        } else if (status == 135) {
            cm.changeJob(MapleJob.CORSAIR);
	    cm.teachSkill(5220001, 0, 30); //Elemental Boost
			cm.teachSkill(5220002, 0, 20); //Wrath of the Octopi
			cm.teachSkill(5220011, 0, 20); //Bullseye
			cm.teachSkill(5221000, 0, 30); //Maple Warrior
			cm.teachSkill(5221003, 0, 30); //Aerial Strike
			cm.teachSkill(5221004, 0, 30); //Rapid Fire
			cm.teachSkill(5221006, 0, 10); //Battleship
			cm.teachSkill(5221007, 0, 30); //Battleship Cannon
			cm.teachSkill(5221008, 0, 30); //Battleship Torpedo
			cm.teachSkill(5221009, 0, 20); //Hypnotize
			cm.teachSkill(5221010, 0, 5); //Speed Infusion
            cm.sendOk("There you go. Hope you enjoy it.");
            cm.dispose();
        } else if (status == 151) {
            if (cm.c.getPlayer().getInt() >= 20) {
                cm.sendSimple("Which do you prefer? #b\r\n#L0#Magician#l\r\n#L1#Blaze Wizard#l#k");
                status = 200;
            } else {
                cm.sendOk("You did not meet the requirement of #r20 INT#k.");
            }
        } else if (status == 154) {
            cm.sendSimple("Which would you like to be? #b\r\n#L0#Warrior#l\r\n#L1#Magician#l\r\n#L2#Bowman#l\r\n#L3#Thief#l\r\n#L4#Pirate#l\r\nOr Do you prefer Knights Of Cygnus?\r\n#L5#Dawn Warrior#l\r\n#L6#Night Walker#l\r\n#L7#Blaze Wizard#l\r\n#L8#Wind Archer#l\r\n#L9#Thunder Breaker#l#k");
        } else if (status == 155) {
            if (selection == 0) {
                jobName = "Warrior";
                job = net.sf.odinms.client.MapleJob.WARRIOR;
            }
            if (selection == 1) {
                jobName = "Magician";
                job = net.sf.odinms.client.MapleJob.MAGICIAN;
            }
            if (selection == 2) {
                jobName = "Bowman";
                job = net.sf.odinms.client.MapleJob.BOWMAN;
            }
            if (selection == 3) {
                jobName = "Thief";
                job = net.sf.odinms.client.MapleJob.THIEF;
            }
            if (selection == 4) {
                jobName = "Pirate";
                job = net.sf.odinms.client.MapleJob.PIRATE;
            }
            if (selection == 5) {
                jobName = "Dawn Warrior";
                job = net.sf.odinms.client.MapleJob.DAWNWARRIOR1;
            }
            if (selection == 6) {
                jobName = "Night Walker";
                job = net.sf.odinms.client.MapleJob.NIGHTWALKER1;
            }
            if (selection == 7) {
                jobName = "Blaze Wizard";
                job = net.sf.odinms.client.MapleJob.BLAZEWIZARD1;
            }
            if (selection == 8) {
                jobName = "Wind Archer";
                job = net.sf.odinms.client.MapleJob.WINDARCHER1;
            }
            if (selection == 9) {
                jobName = "Thunder Breaker";
                job = net.sf.odinms.client.MapleJob.THUNDERBREAKER1;
            }
            cm.sendYesNo("Do you want to become a #r" + jobName + "#k?");
        } else if (status == 156) {
            if (job == net.sf.odinms.client.MapleJob.WARRIOR && cm.c.getPlayer().getStr() < 35) {
                cm.sendOk("You did not meet the minimum requirement of #r35 STR#k.");
                cm.dispose();
            } else if (job == net.sf.odinms.client.MapleJob.MAGICIAN && cm.c.getPlayer().getInt() < 20) {
                cm.sendOk("You did not meet the minimum requirement of #r20 INT#k.");
                cm.dispose();
            } else if (job == net.sf.odinms.client.MapleJob.BOWMAN && cm.c.getPlayer().getDex() < 25) {
                cm.sendOk("You did not meet the minimum requirement of #r25 DEX#k.");
                cm.dispose();
            } else if (job == net.sf.odinms.client.MapleJob.THIEF && cm.c.getPlayer().getDex() < 25) {
                cm.sendOk("You did not meet the minimum requirement of #r25 DEX#k.");
                cm.dispose();
            } else if (job == net.sf.odinms.client.MapleJob.PIRATE && cm.c.getPlayer().getDex() < 20) {
                cm.sendOk("You did not meet the minimum requirement of #r20 DEX#k.");
                cm.dispose();
            } else if (job == net.sf.odinms.client.MapleJob.DAWNWARRIOR1 && cm.c.getPlayer().getStr() < 35) {
                cm.sendOk("You did not meet the minimum requirement of #r35 STR#k.");
                cm.dispose();
            } else if (job == net.sf.odinms.client.MapleJob.NIGHTWALKER1 && cm.c.getPlayer().getDex() < 25) {
                cm.sendOk("You did not meet the minimum requirement of #r25 DEX#k.");
                cm.dispose();
            } else if (job == net.sf.odinms.client.MapleJob.BLAZEWIZARD1 && cm.c.getPlayer().getInt() < 20) {
                cm.sendOk("You did not meet the minimum requirement of #r20 INT#k.");
                cm.dispose();
            } else if (job == net.sf.odinms.client.MapleJob.WINDARCHER1 && cm.c.getPlayer().getDex() < 25) {
                cm.sendOk("You did not meet the minimum requirement of #r25 DEX#k.");
                cm.dispose();
            } else if (job == net.sf.odinms.client.MapleJob.THUNDERBREAKER1 && cm.c.getPlayer().getDex() < 20) {
                cm.sendOk("You did not meet the minimum requirement of #r20 DEX#k.");
                cm.dispose();
            } else {
                cm.changeJob(job);
                cm.sendOk("There you go. Hope you enjoy it. See you around in the future maybe :)");
                cm.dispose();
            }
        } else if (status == 161) {
            cm.doReborn();
            cm.sendOk("You have been reborn! Good luck on your next journey!");
            cm.dispose();
        } else if (status == 201) {
            if (selection == 0) {
                cm.changeJob(net.sf.odinms.client.MapleJob.MAGICIAN);
                cm.sendOk("There you go. Hope you enjoy it. See you around in the future maybe :)");
                cm.dispose();
            }
            if (selection == 1) {
                cm.changeJob(net.sf.odinms.client.MapleJob.BLAZEWIZARD1);
                cm.sendOk("There you go. Hope you enjoy it. See you around in the future maybe :)");
                cm.dispose();
            }
        } else {
            cm.dispose();
        }
    }
}
