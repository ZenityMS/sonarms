/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
var status = -1;
var zones = 0;

function start() {
    if(cm.haveItem(4031713)){
	    var em = cm.getEventManager("Subway");
	    if (em.getProperty("entry") == "true")
		    cm.sendYesNo("Do you wants to go to Masteria?");
		else{
		    cm.sendOk("The subway is ready to take off, please be patience for next one.");
			cm.dispose();
			return;
		}
	}else{
        cm.sendNext("Hi, I'm the ticket gate.");
        if (cm.isQuestStarted(2057) || cm.isQuestCompleted(2057))
            zones = 3;
        else if (cm.isQuestStarted(2056) || cm.isQuestCompleted(2056))
            zones = 2;
        else if (cm.isQuestStarted(2055) || cm.isQuestCompleted(2055))
            zones = 1;
	}
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        cm.dispose();
		return;
    }
    if (status == 0) {
	    if(type == 1){
		    cm.gainItem(4031713, -1);
			cm.warp(600010004);
			cm.dispose();
		} else if (zones == 0)
            cm.dispose();
        else {
            var selStr = "Where would you like to go?#b";
            for (var i = 0; i < zones; i++)
                selStr += "\r\n#L" + i + "#Construction site B" + (i + 1) + "#l";
            cm.sendSimple(selStr);
        }
    }else if (status == 1){
        if (!(cm.haveItem(4031036 + selection)))
            cm.sendOk("You do not have a ticket.");
        else {
            cm.gainItem(4031036 + selection,-1);
            cm.warp(103000900 + (selection * 3));
        }
	    cm.dispose();
	}
}	