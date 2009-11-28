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
	Map(s):		 Sky Ferry
*/

function start() {
	cm.sendYesNo("Ummm, are you trying to leave #m130000000# again? I can take you to #b#m101000000##k if you want...\r\n\r\nYou'll have to pay a fee of #b100#k Mesos.");
}

function action(mode, type, selection) {
	if (mode == 1) {
		if (cm.getPlayer().getMeso() >= 100) {
			cm.gainMeso(-100);
			cm.warp(101000400); //ellinia station
		} else {
			cm.sendNext("Hey you don't have enough Mesos with you... the ride will cost you #b100#k Mesos.");
		}
	} else {
		cm.sendNext("If not, forget it.");
	}
	cm.dispose();
}