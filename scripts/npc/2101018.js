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
///**
//    Cesar
//    Ariant Coliseum
//**/
//
//
//function start() {
//    cm.sendYesNo("Would you like to go to #bAriant Coliseum#k? You must be level 20 to 30 to participate.");
//}
//
//function action(mode, type, selection) {
//    if (mode < 1)
//        cm.dispose();
//    else {
//        if(cm.getPlayer().getLevel() > 19 && cm.getPlayer().getLevel() < 31 || cm.getPlayer().gmLevel() > 0) {
//            cm.getPlayer().saveLocation(SavedLocationType.FREE_MARKET);
//            cm.warp(980010000, 3);
//            cm.dispose();
//        } else {
//            cm.sendOk("You're not between level 20 and 30. Sorry, you may not participate.");
//            cm.dispose();
//        }
//    }
//} 

function start() {
    cm.sendOk("I have better stuff to do than talk at this moment.");
    cm.dispose();
}