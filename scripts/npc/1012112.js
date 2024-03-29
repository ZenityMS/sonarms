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
/*Tory - [Does the Main function of HPQ]
 *@author Jvlaple
 *This file is part of Henesys Party Quest created by Jvlaple of RaGEZONE. [www.forum.ragezone.com] removing this notice means you may not use this script, or any other software released by Jvlaple.
 */
var status = 0;
var minLevel = 1;
var maxLevel = 255;
var minPlayers = 0;
var maxPlayers = 6;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if(cm.getPlayer().getMapId()==100000200){
            if (status == 0) {
                cm.sendNext("This is the #rPrimrose Hill#k. When there is a full moon the moon bunny comes to make rice cakes. Growlie wants rice cakes so you better go help him or he\'ll eat you.");
            } else if (status == 1) {
                cm.sendSimple("Would you like to go help Growlie?#b\r\n#L0#Yes, I will go.#l#k");
            } else if (status == 2) {
                if (cm.getParty() == null) {
                    cm.sendOk("You are not in a party.");
                    cm.dispose();
                    return;
                }
                if (!cm.isLeader()) {
                    cm.sendOk("You are not the party leader.");
                    cm.dispose();
                } else {
                    var party = cm.getParty().getMembers();
                    var mapId = cm.getPlayer().getMapId();
                    var next = true;
                    var levelValid = 0;
                    var inMap = 0;
                    if (next) {
                        var em = cm.getEventManager("HenesysPQ");
                        if (em == null)
                            cm.dispose();
                        else {
                            em.startInstance(cm.getParty(),cm.getPlayer().getMap());
                            party = cm.getPlayer().getEventInstance().getPlayers();
                        }
                        cm.dispose();
                    }
                    else {
                        cm.sendOk("Your party is not a party of three to six.  Make sure all your members are present and qualified to participate in this quest.  I see #b" + levelValid.toString() + " #kmembers are in the right level range, and #b" + inMap.toString() + "#k are in my map. If this seems wrong, #blog out and log back in,#k or reform the party.");
                        cm.dispose();
                    }
                }
            }
        } else if(cm.getPlayer().getMapId() == 910010400){
            if (status == 0){
                cm.warp(100000200);
                cm.playerMessage(5, "You have been warped to Henesys Park.");
                cm.dispose();
            }
        } else if (cm.getPlayer().getMapId() == 910010100) {
            if (status==0) {
                cm.sendYesNo("Would you like go to #rHenesys Park#k?");
            }else if (status == 1){
                cm.warp(100000200, 0);
                cm.dispose();
            }
        }
    }
}
					
					
