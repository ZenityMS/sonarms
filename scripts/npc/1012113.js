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
/*
Tommy - 1012113.js
Warps out of HPQ
 @Author Jvlaple
*/

 
var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 1)
        cm.dispose();
    else {
        status++;
        if (cm.getPlayer().getMap().getId() == 910010300){
            if (status == 0) {
                cm.sendNext("Tough Luck eh? Try again later!");
            } else if (status == 1){
                cm.warp(100000200);
                cm.dispose();
            }
        } else if (cm.getPlayer().getMap().getId() == 910010100) {
            var eim = cm.getPlayer().getEventInstance();
            if (status == 0) {
                cm.sendYesNo("Would you like to go to #rPig Town#k? It is a town where Pigs are everywhere, you might find some valuable items there!");
            } else if (status == 1) {
                cm.mapMessage(5, "You have been warped to Pig Town.");
                var em = cm.getEventManager("PigTown");
                if (em == null) {
                    cm.dispose();
                } else {
                    em.startInstance(cm.getParty(),cm.getPlayer().getMap());
                    party = cm.getPlayer().getEventInstance().getPlayers();
                }
                cm.dispose();
            }
        }
    }
}	