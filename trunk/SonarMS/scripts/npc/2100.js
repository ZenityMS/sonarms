/* Sera
By Tommy of SonarMS
*/

function start() {
    cm.sendSimple("#bWelcome to SonarMS. What job do you wish to be? You will gain items according to your job choice, and will be leveled to 10. Talk to Cody to make your first job advancement and enjoy your stay in SonarMS!#k \r\n#L0#Beginner#l \r\n\ #L1#Warrior#l \r\n\ #L2#Magician#k#l \r\n\ #L3#Bowman#l \r\n\ #L4#Thief#l \r\n\ #L5#Pirate#l");
}

function action(mode, type, selection) {
    if (mode < 1)
        cm.dispose();
    else {
        cm.resetStats();
	    cm.setLevel(10);
        cm.warp(100000000,0);
        cm.dispose();
    }
}