/*
 *@Author:  Moogra
 *@NPC:     4th Job Advancement NPC
 *@Purpose: Handles 4th job.
 */

function start() {
    if (cm.getLevel() < 120) {
        cm.sendOk("Sorry, but you have to be at least level 120 to make the 4th Job Advancement.");
        cm.dispose();
    } else if (cm.getLevel() >=120)
        cm.sendNext("Do you want to get your 4th Job Advancement?");
}

function action(mode, type, selection) {
    if (mode > 1)
        cm.getPlayer().changeJob(MapleJob.getById(jobId + 1));
    cm.dispose();
}