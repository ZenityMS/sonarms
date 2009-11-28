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
	NPC Name:	 Kiru
	Map(s):		 Sky Ferry
*/

function start() {
	cm.sendYesNo("Have you taken care of everything you needed to in #m130000000#? If you happen to be headed to #b#m200000000##k I can take you there. What do you say? Are you going to #m200000000#?\r\n\r\nYou'll have to pay a fee of #b100#k Mesos.");
}

function action(mode, type, selection) {
	if (mode == 1) {
		if (cm.getPlayer().getMeso() >= 100) {
			cm.gainMeso(-100);
			cm.warp(200000100);
		} else {
			cm.sendNext("Hey you don't have enough Mesos with you... the ride will cost you #b100#k Mesos.");
		}
	} else {
		cm.sendNext("If not, forget it.");
	}
	cm.dispose();
}