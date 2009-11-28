/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/*	Author: 	 Traitor
	NPC Name:	 Kiriru
	Map(s):		 Sky Ferry <To Ereve>
*/

function start() {
	cm.sendYesNo("Oh, and... so... this ship will take you to #b#m130000000##k, the place where you'll find crimson leaves soaking up the sun, the gentle breeze that glides past the stream, and the Empress of Maple, Cygnus. Would you like to head over to #m130000000#?\r\n\r\nThis trip costs #b100#k Mesos.");
}

function action(mode, type, selection) {
	if (mode == 1) {
		if (cm.getPlayer().getMeso() >= 100) {
			cm.gainMeso(-100);
			cm.warp(130000210); //ereve sky ferry
		} else {
			cm.sendNext("Hey, you don't have enough Mesos with you... the ride will cost you #b100#k Mesos. Check your inventory and see if you have enough money with you.");
		}
	} else {
		cm.sendNext("If you're not interested, then oh well...");
	}
	cm.dispose();
}