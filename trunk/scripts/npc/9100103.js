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
 * Gachapon
 * @NPC : Gachapon - Kerning City
 * @NPC ID : 9100103
 * @author Moogra
 * Item IDs by Sam
*/

function start() {
    if (cm.haveItem(5220000))
        cm.sendYesNo("You may use Gachapon. Would you like to use your Gachapon ticket?");
    else {
        cm.sendOk("Here is Gachapon.");
        cm.dispose();
    }
}

function action(mode, type, selection){
    if(mode > 0) {
        var ids = [1002130,1040042,1040094,1322052,2000005,2022113,1302001,2040106,1332002];
        cm.processGachapon(ids);
    }
    cm.dispose();
}