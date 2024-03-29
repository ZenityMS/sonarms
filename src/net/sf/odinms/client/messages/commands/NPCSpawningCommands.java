

package net.sf.odinms.client.messages.commands;

import java.awt.Point;
import java.util.Arrays;
import java.util.List;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.messages.Command;
import net.sf.odinms.client.messages.CommandDefinition;
import net.sf.odinms.client.messages.IllegalCommandSyntaxException;
import net.sf.odinms.client.messages.MessageCallback;
import net.sf.odinms.server.life.MapleLifeFactory;
import net.sf.odinms.server.life.MapleNPC;
import net.sf.odinms.server.maps.MapleMapObject;
import net.sf.odinms.server.maps.MapleMapObjectType;
import net.sf.odinms.tools.MaplePacketCreator;

public class NPCSpawningCommands implements Command {
	@Override
	public void execute(MapleClient c, MessageCallback mc, String[] splitted) throws Exception,
																				IllegalCommandSyntaxException {
		if (splitted[0].equals("!npc")) {
			int npcId = Integer.parseInt(splitted[1]);
			MapleNPC npc = MapleLifeFactory.getNPC(npcId);
			if (npc != null && !npc.getName().equals("MISSINGNO")) {
				npc.setPosition(c.getPlayer().getPosition());
				npc.setCy(c.getPlayer().getPosition().y);
				npc.setRx0(c.getPlayer().getPosition().x + 50);
				npc.setRx1(c.getPlayer().getPosition().x - 50);
				npc.setFh(c.getPlayer().getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
				npc.setCustom(true);
				c.getPlayer().getMap().addMapObject(npc);
				c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc, false));
				// c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc, true));
			} else {
				mc.dropMessage("You have entered an invalid Npc-Id");
			}
		} else if (splitted[0].equals("!removenpcs")) {
			MapleCharacter player = c.getPlayer();
			List<MapleMapObject> npcs = player.getMap().getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.NPC));
			for (MapleMapObject npcmo : npcs) {
				MapleNPC npc = (MapleNPC) npcmo;
				if (npc.isCustom()) {
					player.getMap().removeMapObject(npc.getObjectId());
				}
			}
		} else if (splitted[0].equals("!mynpcpos")) {
		    Point pos = c.getPlayer().getPosition();
		    mc.dropMessage("CY: " + pos.y + " | RX0: " + (pos.x + 50) + " | RX1: " + (pos.x - 50) + " | FH: " + c.getPlayer().getMap().getFootholds().findBelow(pos).getId());
		}
	}

	@Override
	public CommandDefinition[] getDefinition() {
		return new CommandDefinition[] {
			new CommandDefinition("npc", "npcid", "Spawns the npc with the given id at the player position", 500),
			new CommandDefinition("removenpcs", "", "Removes all custom spawned npcs from the map - requires reentering the map", 500),
			new CommandDefinition("mynpcpos", "", "Gets the info for making an npc", 100),
		};
	}

}
