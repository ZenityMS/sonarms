/*      
        Author : Generic (http://cronusemu.net)
        NPC Name:               Dalair
        Map(s):                 Every Town
        Description:            Quest - Title Challenge - PQ Mania!
        Quest ID :              29400
*/

function start(mode, type, selection) {
        if (mode == -1) {
                qm.sendNext("Come back when you're ready.");
                qm.dispose();
        } else {
                if (mode > 0)
                        status++;
                else
                        status--;
                if (status == 0) {
                        qm.sendAcceptDecline("#v1142004# #e#b#t1142004##k\r\n - Time Limit 30 Days \r\n - Hunt 100,000Monsters \r\n#n *Only monsters that are at your level or higher are approved\r\n   (A character that is level 120 or higher will only count monsters that are level 120 or higher) \r\n \r\nDo you want to test your skills to see if you're worthy of this title?");
                } else if (status == 1) {
                        qm.sendNext("Current Ranking\r\n \r\n 1. #blRichaDK#k : #r116,725#k monsters \r\n \r\nDon't forget that the record resets at the beginning of each month."); // TODO Info automation.
                } else if (status == 2) {
                        qm.sendNextPrev("I'll give you 30 days to reach your hunting goal.  Once you are finished, come back and see me.  Remember that you have to come back and see me within the time limit in order for it to be approved.  Also, unless you complete this challenge or quit first, you can't try out for another title.");
                }

        }
        
}