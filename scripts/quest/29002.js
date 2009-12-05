/*      
        Author : Generic (http://cronusemu.net)
        NPC Name:               Dalair
        Map(s):                 Every town
        Description:            Quest - Title Challenge - Celebrity!
*/

importPackage(Packages.server.quest);

var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
		if(mode == 0 && status == 2){
		    status -= 2;
		}else{
	        if(mode == 0)
                qm.sendNext("Come back when you're ready.");
            qm.dispose();
		    return;
		}
    }
    if (status == 0)
        qm.sendAcceptDecline("#v1142003# #e#b#t1142003##k \r\n- Time Limit 30 Days \r\n- Popularity 1000Increase \r\n#nDo you want to test your skills to see if you're worthy of this title?");
    else if (status == 1) 
        qm.sendNextPrev("I'll give you 30 days to reach your goal.  Once you're finished, come back and see me.  Remember that you have to come back and see me within the time limit in order for it to be approved.  Also, unless you complete this challenge or quit first, you can't try out for another title.");
	else if (status == 2){
        qm.forceStartQuest();
		qm.dispose();
    }
}

function end(mode, type, selection) {
    status++;
    if(mode != 1){
	    if(mode == 0)
	        qm.sendNext("That's the attitude!\r\nRemember you have a limit period of time to accomplish this.");
	    qm.dispose();
		return;
	}
	if(status == 0){
        if(qm.c.getPlayer().getFame() > 999){
	        qm.sendNext("Congratulations, you're worthy to have this.");
		    qm.gainItem(1142003,1);
			qm.dispose();
	    }else
	        qm.sendAcceptDecline("In order to earn the Celebrity title, you must raise 1,000 popularity points within a given time.  If you think that's too hard, why don't you quit and try for another title?");
			return;
	}else{
	    MapleQuest.getInstance(qm.getQuest()).forfeit(qm.c.getPlayer());
		qm.sendNext("Come back again when you wants to pick up a new challenge.");
		qm.dispose();
	}
}