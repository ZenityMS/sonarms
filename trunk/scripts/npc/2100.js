/* Sera
By Tommy of SonarMS
*/

var jobid = Array(0, 100, 200, 300, 400, 500);

function start() {
    cm.sendSimple("#bWelcome to SonarMS. Which job do you wish to be? You will be leveled to 10, so be sure to talk to Cody to make your first job advancement and enjoy your stay in SonarMS!#k \r\n#L0#Beginner#l \r\n\ #L1#Warrior#l \r\n\ #L2#Magician#k#l \r\n\ #L3#Bowman#l \r\n\ #L4#Thief#l \r\n\ #L5#Pirate#l");
}

function action(mode, type, selection) {
    if (mode < 1)
        cm.dispose();
    else {
        cm.resetStats();
	    cm.gainExp(3347);
		cm.gainMeso(100000000);
        cm.warp(100000000,0);
        cm.dispose();
    }
}