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

import java.sql.PreparedStatement;
import client.MapleCharacter;
import client.MapleClient;
import java.util.Map.Entry;
import tools.DatabaseConnection;
import net.channel.ChannelServer;
import scripting.map.MapScriptManager;
import server.MapleOxQuiz;
import server.life.MapleLifeFactory;
import server.life.MapleNPC;
import server.maps.MapleMap;
import tools.MaplePacketCreator;

class AdminCommand {
    static void execute(MapleClient c, String[] splitted, char heading) {
        MapleCharacter player = c.getPlayer();
        if (splitted[0].equals("gc"))
            System.gc();
        else if (splitted[0].equals("horntail"))
            for (int i = 8810002; i < 8810010; i++)
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(i), player.getPosition());
        else if (splitted[0].equals("npc")) {
            MapleNPC npc = MapleLifeFactory.getNPC(Integer.parseInt(splitted[1]));
            if (npc != null) {
                npc.setPosition(player.getPosition());
                npc.setCy(player.getPosition().y);
                npc.setRx0(player.getPosition().x + 50);
                npc.setRx1(player.getPosition().x - 50);
                npc.setFh(player.getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
                player.getMap().addMapObject(npc);
                player.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
            }
        } else if (splitted[0].equals("ox"))
            if (splitted[1].equals("on") && player.getMapId() == 109020001) {
                player.getMap().setOx(new MapleOxQuiz(player.getMap()));
                player.getMap().getOx().sendQuestion();
                player.getMap().setOxQuiz(true);
            } else {
                player.getMap().setOxQuiz(false);
                player.getMap().setOx(null);
            }
        else if (splitted[0].equals("pinkbean"))
            player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(8820001), player.getPosition());
        else if (splitted[0].equals("playernpc"))
            player.playerNPC(c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]), Integer.parseInt(splitted[2]));
        else if (splitted[0].equals("reloadmapscripts"))
            MapScriptManager.getInstance().clearScripts();
        else if (splitted[0].equals("reloadmapspawns"))
            for (Entry<Integer, MapleMap> map : c.getChannelServer().getMapFactory().getMaps().entrySet())
                map.getValue().respawn();
        else if (splitted[0].equals("setgmlevel")) {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
            victim.setGM(Integer.parseInt(splitted[2]));
            player.message("Done.");
            victim.getClient().disconnect();
        } else if (splitted[0].equals("shutdown") || splitted[0].equals("shutdownnow")) {
            int time = 60000;
            if (splitted.length > 1)
                time *= Integer.parseInt(splitted[1]);
            if (splitted[0].equals("shutdownnow"))
                time = 1;
            for (ChannelServer cs : ChannelServer.getAllInstances())
                cs.shutdown(time);
        } else if (splitted[0].equals("sql")) {
            final String query = GMCommand.joinStringFrom(splitted, 1);
            try {
                PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(query);
                ps.executeUpdate();
                ps.close();
                player.message("Done " + query);
            } catch (Exception e) {
                player.message("Query Failed: " + query);
            }
        } else if (splitted[0].equals("zakum")) {
            player.getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800000), player.getPosition());
            for (int x = 8800003; x < 8800011; x++)
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(x), player.getPosition());
        } else
            player.message("Command " + heading + splitted[0] + " does not exist.");
    }
}
