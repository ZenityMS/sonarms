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

package net.sf.odinms.client.messages.commands;

import static net.sf.odinms.client.messages.CommandProcessor.getNamedIntArg;
import static net.sf.odinms.client.messages.CommandProcessor.joinAfterString;

import java.text.DateFormat;
import java.util.Calendar;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.messages.Command;
import net.sf.odinms.client.messages.CommandDefinition;
import net.sf.odinms.client.messages.IllegalCommandSyntaxException;
import net.sf.odinms.client.messages.MessageCallback;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.tools.StringUtil;

public class BanningCommands implements Command {
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception {
		ChannelServer cserv = c.getChannelServer();
		if (splitted[0].equals("/tempban")) {
			Calendar tempB = Calendar.getInstance();
			String originalReason = joinAfterString(splitted, ":");

			if (splitted.length < 4 || originalReason == null) {
				// mc.dropMessage("Syntax helper: /tempban <name> [i / m / w / d / h] <amount> [r [reason id] : Text
				// Reason");
				throw new IllegalCommandSyntaxException(4);
			}

			int yChange = getNamedIntArg(splitted, 1, "y", 0);
			int mChange = getNamedIntArg(splitted, 1, "m", 0);
			int wChange = getNamedIntArg(splitted, 1, "w", 0);
			int dChange = getNamedIntArg(splitted, 1, "d", 0);
			int hChange = getNamedIntArg(splitted, 1, "h", 0);
			int iChange = getNamedIntArg(splitted, 1, "i", 0);
			int gReason = getNamedIntArg(splitted, 1, "r", 7);

			String reason = c.getPlayer().getName() + " tempbanned " + splitted[1] + ": " + originalReason;

			if (gReason > 14) {
				mc.dropMessage("You have entered an incorrect ban reason ID, please try again.");
				return;
			}

			DateFormat df = DateFormat.getInstance();
			tempB.set(tempB.get(Calendar.YEAR) + yChange, tempB.get(Calendar.MONTH) + mChange, tempB.get(Calendar.DATE) +
				(wChange * 7) + dChange, tempB.get(Calendar.HOUR_OF_DAY) + hChange, tempB.get(Calendar.MINUTE) +
				iChange);

			MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);

			if (victim == null) {
				int accId = MapleClient.findAccIdForCharacterName(splitted[1]);
				if (accId >= 0 && MapleCharacter.tempban(reason, tempB, gReason, accId)) {
					mc.dropMessage("The character " + splitted[1] + " has been successfully offline-tempbanned till " +
						df.format(tempB.getTime()) + ".");
				} else {
					mc.dropMessage("There was a problem offline banning character " + splitted[1] + ".");
				}
			} else {
				victim.tempban(reason, tempB, gReason);
				mc.dropMessage("The character " + splitted[1] + " has been successfully tempbanned till " +
					df.format(tempB.getTime()));
			}
		} else if (splitted[0].equals("/dc")) {
			int level = 0;
			MapleCharacter victim;
			if (splitted[1].charAt(0) == '-') {
				level = StringUtil.countCharacters(splitted[1], 'f');
				victim = cserv.getPlayerStorage().getCharacterByName(splitted[2]);
			} else {
				victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
			}
			victim.getClient().getSession().close();
			if (level >= 1) {
				victim.getClient().disconnect();
			}
			if (level >= 2) {
				victim.saveToDB(true);
				cserv.removePlayer(victim);
			}
		} else if (splitted[0].equals("/unban")) {
			if (MapleCharacter.unban(splitted[1], false)) {
				mc.dropMessage("Unbanned " + splitted[1]);
			} else {
				mc.dropMessage("Failed to unban " + splitted[1]);
			}
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("/ban", "<charname>", "Permanently ip, mac and accountbans the given character", 1),
			new CommandDefinition("/tempban", "<name> [i / m / w / d / h] <amount> [r  [reason id] : Text Reason", "Tempbans the given account", 1),
			new CommandDefinition("/unban", "<name>", "Unbans the given character", 1),
			new CommandDefinition("/dc", "<name>", "Disconnects the player with the given name", 2),
		};
	}

}
