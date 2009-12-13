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
package client.command;

import client.MapleClient;

public final class CommandProcessor {
    public static int getOptionalIntArg(String splitted[], int position, int def) {
		if (splitted.length > position) {
			try {
				return Integer.parseInt(splitted[position]);
			} catch (NumberFormatException nfe) {
				return def;
			}
		}
		return def;
	}
    public static final boolean processCommand(final MapleClient c, final String s) {
        if (s.charAt(0) == '/' && c.getPlayer().isGM()) {
            String[] sp = s.split(" ");
            sp[0] = sp[0].toLowerCase().substring(1);
            c.getPlayer().addCommandToList(s);
            if (GMCommand.execute(c, sp, '/'))
                return true;
            else if (c.getPlayer().gmLevel() > 1)
                AdminCommand.execute(c, sp, '/');
            return true;
        }
        return false;
    }
}