/**
	Assistant Travis
-- By ---------------------------------------------------------------------------------------------
	Angel (get31720 ragezone)
-- Extra Info -------------------------------------------------------------------------------------
	Fixed by  [happydud3] & [XotiCraze]
---------------------------------------------------------------------------------------------------
**/

var status;

function start() {
    if (cm.haveItem(4000313)) {
        cm.sendOk("You are a guest. Please continue with the wedding. I only warp out people who are here by accident.");
        cm.dispose();
    } else
        cm.sendNext("I warp people out. If you are the newly wed don't click next or you will not be able to collect your prize at the end.");
}

function action(mode, type, selection) {
    if (mode > 1)
        cm.warp(680000000);
    cm.dispose();
}