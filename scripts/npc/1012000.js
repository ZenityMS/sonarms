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

/* Regular Cab */

var status = 0;
var maps = [104000000, 102000000, 101000000, 100000000, 103000000, 120000000];
var cost = [120, 120, 80, 100, 100, 120];
var selectedMap = -1;

function start() {
    cm.sendNext("Hello, I drive the Regular Cab. If you want to go from town to town safely and fast, then ride our cab. We'll glady take you to your destination with an affordable price.");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1){
	    if((mode == 0 && !town) || mode == -1){
	        if(type == 1 && mode != -1)
		        cm.sendNext("There's a lot to see in this town, too. Come back and find us when you need to go to a different town.");
            cm.dispose();
		    return;
		}else
		    status -= 2;
	}
    if (status == 1){
        var selStr = cm.getJobId() == 0 ? "There's a special 90% discount for all beginners. Alright, where would you want to go?#b" : "Choose your destination, for fees will change from place to place.#b";
        for (var i = 0; i < maps.length; i++)
            selStr += "\r\n#L" + i + "##m" + maps[i] + "# (" + (cost[i] * (cm.getJobId() == 0 ? 1 : 10)) + " mesos)#l";
        cm.sendSimple(selStr);
    }else if(status == 3){
	    selectedMap = selection;
        cm.sendYesNo("You don't have anything else to do here, huh? Do you really want to go to #b#m" + maps[selection] + "##k? Well it'll cost you #b" + (cost[selection] * (cm.getJobId() == 0 ? 1 : 10)) + " mesos#k.");
    }else if (status == 4) {
        if (cm.getMeso() < (cost[selectedMap] * (cm.getJobId() == 0 ? 1 : 10)))
            cm.sendNext("You don't have enough mesos. Sorry to say this, but without them, you won't be able to ride the cab.");
        else {
            cm.gainMeso(-(cost[selectedMap] * (cm.getJobId() == 0 ? 1 : 10)));
            cm.warp(maps[selectedMap]);
        }
        cm.dispose();
    }
}