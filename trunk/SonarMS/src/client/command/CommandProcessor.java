package client.command;

public class CommandProcessor {
    public static boolean processCommand(client.MapleClient c, String s) {
        String[] sp = s.split(" ");
        sp[0] = sp[0].toLowerCase();
        if (s.charAt(0) == '!' && c.getPlayer().gmLevel() > 0) {
            c.getPlayer().addCommandToList(s);
            if (GMCommand.execute(c, sp))
                return true;
            else if (c.getPlayer().gmLevel() > 1)
                AdminCommand.execute(c, sp);
            return true;
        } else if (s.charAt(0) == '@' && c.getChannelServer().allowPlayerCommands()) {
            PlayerCommand.execute(c, sp);
            return true;
        }
        return false;
    }
}