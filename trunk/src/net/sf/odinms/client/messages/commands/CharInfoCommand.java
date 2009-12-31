package net.sf.odinms.client.messages.commands;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.messages.Command;
import net.sf.odinms.client.messages.CommandDefinition;
import net.sf.odinms.client.messages.IllegalCommandSyntaxException;
import net.sf.odinms.client.messages.MessageCallback;

public class CharInfoCommand implements Command {
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splittedLine) throws Exception,
																					IllegalCommandSyntaxException {
		StringBuilder builder = new StringBuilder();
		MapleCharacter other = c.getChannelServer().getPlayerStorage().getCharacterByName(splittedLine[1]);

		builder.append(MapleClient.getLogMessage(other, ""));
		builder.append(" at ");
		builder.append(other.getPosition().x);
		builder.append("/");
		builder.append(other.getPosition().y);
		builder.append(" ");
		builder.append(other.getHp());
		builder.append("/");
		builder.append(other.getCurrentMaxHp());
		builder.append("hp ");
		builder.append(other.getMp());
		builder.append("/");
		builder.append(other.getCurrentMaxMp());
		builder.append("mp ");
		builder.append(other.getExp());
		builder.append("exp hasParty: ");
		builder.append(other.getParty() != null);
		builder.append(" hasTrade: ");
		builder.append(other.getTrade() != null);
		builder.append(" remoteAddress: ");
		builder.append(other.getClient().getSession().getRemoteAddress());
		mc.dropMessage(builder.toString());
		other.getClient().dropDebugMessage(mc);
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("charinfo", "charname", "Shows info about the charcter with the given name", 50),
		};
	}
}
