

package net.sf.odinms.client.messages.commands;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.messages.Command;
import net.sf.odinms.client.messages.CommandDefinition;
import net.sf.odinms.client.messages.IllegalCommandSyntaxException;
import net.sf.odinms.client.messages.MessageCallback;

public class ArrayCommand implements Command {
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splittedLine) throws Exception, IllegalCommandSyntaxException {
		mc.dropMessage("Array");
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("array", "", "Shows array", 0),
		};
	}

}
