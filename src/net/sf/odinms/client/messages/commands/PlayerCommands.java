package net.sf.odinms.client.messages.commands;

import net.sf.odinms.client.messages.Command;
import net.sf.odinms.client.messages.IllegalCommandSyntaxException;
import net.sf.odinms.client.messages.MessageCallback;
import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.messages.CommandDefinition;

public class PlayerCommands implements Command {
        @Override
	public void execute(MapleClient c, MessageCallback mc, String[] splittedLine, String line) throws Exception,
																					IllegalCommandSyntaxException {
	MapleCharacter player = c.getPlayer();
        String[] splitted = line.split(" ");
        if (splitted[0].equals("@str") || splitted[0].equals("@dex") || splitted[0].equals("@int") || splitted[0].equals("@luk") || splitted[0].equals("@hp") || splitted[0].equals("@mp")) {
            if (splitted.length != 2) {
                mc.dropMessage("Syntax: @<Stat> <amount>");
                mc.dropMessage("Stat: <STR> <DEX> <INT> <LUK> <HP> <MP>");
                return;
            }
            int x = Integer.parseInt(splitted[1]), max = 30000;
            if (x > 0 && x <= player.getRemainingAp() && x < Short.MAX_VALUE) {
                if (splitted[0].equals("@str") && x + player.getStr() < max) {
                    player.addAP(c, 1, x);
                } else if (splitted[0].equals("@dex") && x + player.getDex() < max) {
                    player.addAP(c, 2, x);
                } else if (splitted[0].equals("@int") && x + player.getInt() < max) {
                    player.addAP(c, 3, x);
                } else if (splitted[0].equals("@luk") && x + player.getLuk() < max) {
                    player.addAP(c, 4, x);
                } else if (splitted[0].equals("@hp") && x + player.getMaxHp() < max) {
                    player.addAP(c, 5, x);
                } else if (splitted[0].equals("@mp") && x + player.getMaxMp() < max) {
                    player.addAP(c, 6, x);
                } else {
                    mc.dropMessage("Make sure the stat you are trying to raise will not be over " + Short.MAX_VALUE + ".");
                }
            } else {
                mc.dropMessage("Please make sure your AP is valid.");
            }
      }
}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
                    new CommandDefinition("str", "<amount>", "Sets your strength to a higher amount if you have enough AP or takes it away if you aren't over 32767 AP.", 0),
                    new CommandDefinition("int", "<amount>", "Sets your intelligence to a higher amount if you have enough AP or takes it away if you aren't over 32767 AP.", 0),
                    new CommandDefinition("luk", "<amount>", "Sets your luck to a higher amount if you have enough AP or takes it away if you aren't over 32767 AP.", 0),
                    new CommandDefinition("dex", "<amount>", "Sets your dexterity to a higher amount if you have enough AP or takes it away if you aren't over 32767 AP.", 0),
                    new CommandDefinition("hp", "<amount>", "Sets your hp to a higher amount if you have enough AP or takes it away if you aren't over 30000 HP.", 0),
                    new CommandDefinition("mp", "<amount>", "Sets your mp to a higher amount if you have enough AP or takes it away if you aren't over 30000 MP.", 0),
		};
        }
}