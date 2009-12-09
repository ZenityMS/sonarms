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
function act() {
    //var rand = Math.random()
    var q = 0;
    var q2 = 0;
    if (Math.random() < .8){
        q = 2;
        q2 = 3;
    } else {
        q = 2;
        q2 = 3;
    }
    if (rm.getPlayer().getMapId() == 809050000) {
        rm.spawnMonster(9400209, q * 3);
        rm.spawnMonster(9400210, q2 * 3);
        rm.mapMessage(5, "Some monsters are summoned.");
    } else if (rm.getPlayer().getMapId() == 809050001) {
        rm.spawnMonster(9400211, q);
        rm.spawnMonster(9400212, q2);
        rm.mapMessage(5, "Some monsters are summoned.");
    } else if (rm.getPlayer().getMapId() == 809050002) {
        rm.spawnMonster(9400213, q);
        rm.spawnMonster(9400214, q2);
        rm.mapMessage(5, "Some monsters are summoned.");
    } else if (rm.getPlayer().getMapId() == 809050003) {
        rm.spawnMonster(9400213, q);
        rm.spawnMonster(9400214, q2);
        rm.mapMessage(5, "Some monsters are summoned.");
    } else if (rm.getPlayer().getMapId() == 809050004) {
        rm.spawnMonster(9400215, q);
        rm.spawnMonster(9400216, q2);
        rm.mapMessage(5, "Some monsters are summoned.");
    } else if (rm.getPlayer().getMapId() == 809050005) {
        rm.spawnMonster(9400217, q);
        rm.spawnMonster(9400218, q2);
        rm.mapMessage(5, "Some monsters are summoned.");
    } else if (rm.getPlayer().getMapId() == 809050006) {
        rm.spawnMonster(9400217, q);
        rm.spawnMonster(9400218, q2);
        rm.mapMessage(5, "Some monsters are summoned.");
    } else if (rm.getPlayer().getMapId() == 809050007) {
        rm.spawnMonster(9400215, q);
        rm.spawnMonster(9400216, q2);
        rm.mapMessage(5, "Some monsters are summoned.");
    } else if (rm.getPlayer().getMapId() == 809050008) {
        rm.spawnMonster(9400213, q);
        rm.spawnMonster(9400214, q2);
        rm.mapMessage(5, "Some monsters are summoned.");
    } else if (rm.getPlayer().getMapId() == 809050009) {
        rm.spawnMonster(9400211, q);
        rm.spawnMonster(9400212, q2);
        rm.mapMessage(5, "Some monsters are summoned.");
    } else if (rm.getPlayer().getMapId() == 809050010) {
        rm.spawnMonster(9400209, q * 3);
        rm.spawnMonster(9400210, q2 * 3);
        rm.mapMessage(5, "Some monsters are summoned.");
    }
}