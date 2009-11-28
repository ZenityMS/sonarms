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
/*
* Shinsoo - Empress' Road ( Ereve )
* Author : Generic
*/
importPackage(net.sf.odinms.client);

function start() {
	if (cm.getJob().isA(net.sf.odinms.client.MapleJob.NOBLESSE)) {
		cm.getItemEffect(2022458).applyTo(cm.getPlayer()); // Give Shinsoo's Blessing if they're a cygnus char
		cm.sendOk("Don't stop training. Every ounce of your energy is required to protect Maple World...");		
		cm.dispose();
	} else if (cm.getPlayer().getCygnusLinkId() == 0) {
		cm.sendAcceptDecline("Are you ready to become a Cygnus Knight to serve and protect Maple World? If so, then let's begin");
	} else { // linked to a cygnus char.
		cm.sendOk("You seem to be currently connected to a character that is a Cygnus Knight. If you wish to form a new connection, please log in with a different character.");
		cm.dispose();
	}
}

function action(mode, type, selection) {
	if (cm.getPlayer().getCygnusLinkId() == 0 && !cm.getJob().equals(net.sf.odinms.client.MapleJob.NOBLESSE)) { //Double checking... probably useless but I can't be sure.
		if (mode == 0) {
			cm.sendOk("If you are intent on protecting Maple World on your own accord, then there's nothing I can say that will convince you otherwise...");
		} else if (mode == 1) {
			cm.sendCygnusCharCreate();
		}
	}
	cm.dispose();
}