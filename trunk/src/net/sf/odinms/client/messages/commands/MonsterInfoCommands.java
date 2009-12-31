

package net.sf.odinms.client.messages.commands;

import java.util.Arrays;
import java.util.List;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.messages.Command;
import net.sf.odinms.client.messages.CommandDefinition;
import net.sf.odinms.client.messages.IllegalCommandSyntaxException;
import net.sf.odinms.client.messages.MessageCallback;
import net.sf.odinms.server.life.MapleMonster;
import net.sf.odinms.server.maps.MapleMap;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.server.maps.MapleMapObjectType;

public class MonsterInfoCommands implements Command {
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,
																					IllegalCommandSyntaxException {
		if (splitted[0].equals("!killall") || splitted[0].equals("!monsterdebug")) {
			MapleMap map = c.getPlayer().getMap();
			double range = Double.POSITIVE_INFINITY;
			if (splitted.length > 1) {
				int irange = Integer.parseInt(splitted[1]);
				range = irange * irange;
			}
			List<MapleMapObject> monsters = map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Arrays
				.asList(MapleMapObjectType.MONSTER));
			boolean kill = splitted[0].equals("!killall");
			for (MapleMapObject monstermo : monsters) {
				MapleMonster monster = (MapleMonster) monstermo;
				if (kill) {
					map.killMonster(monster, c.getPlayer(), false);
				} else {
					mc.dropMessage("Monster " + monster.toString());
				}
			}
			if (kill) {
				mc.dropMessage("Killed " + monsters.size() + " monsters <3");
			}
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("killall", "[range]", "", 100),
			new CommandDefinition("monsterdebug", "[range]", "", 100),
		};
	}

}
