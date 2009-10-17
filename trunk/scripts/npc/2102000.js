/* Author: Xterminator
	NPC Name: 		Asesson
	Map(s): 		Ariant: Ariant Station Platform (260000100)
	Description: 	Ariant Ticketing Usher
*/

//too lazy to shorten
var status = 0;

function start() {
    cm.sendYesNo("It looks like there's plenty of room for this ride. Please have your ticket ready so I can let you in. The ride will be long but you'll get to your destination just fine. What do you think? Do you want to get on this ride?");
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status >= 0 && mode == 0) {
            cm.sendNext("You must have some business to take care of here, right?");
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 1) {
            if (cm.haveItem(4031045)) {
                cm.gainItem(4031045, -1)
                cm.warp(200000100, 0);
                cm.dispose();
            } else {
                cm.sendNext("Oh no... I don't think you have the ticket with you. I can't let you in without it. Please buy the ticket at the ticketing booth.");
                cm.dispose();
            }
        }
    }
}