package client.command;

import java.sql.PreparedStatement;
import client.MapleCharacter;
import client.MapleClient;
import tools.DatabaseConnection;
import net.channel.ChannelServer;
import server.MapleOxQuiz;
import server.life.MapleLifeFactory;
import server.life.MapleNPC;
import tools.MaplePacketCreator;
import tools.StringUtil;

class AdminCommand {
    static void execute(MapleClient c, String[] splitted) {
        ChannelServer cserv = c.getChannelServer();
        MapleCharacter player = c.getPlayer();
        if (splitted[0].equals("!horntail"))
            for (int i = 8810002; i < 8810010; i++)
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(i), player.getPosition());
        else if (splitted[0].equals("!npc")) {
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
        } else if (splitted[0].equals("!ox"))
            if (splitted[1].equals("on") && player.getMapId() == 109020001) {
                player.getMap().setOx(new MapleOxQuiz(player.getName(), player.getMap(), 1, 1));
                player.getMap().getOx().scheduleOx(player.getMap());
                player.getMap().setOxQuiz(true);
            } else {
                player.getMap().setOxQuiz(false);
                player.getMap().setOx(null);
            }
        else if (splitted[0].equals("!playernpc"))
            player.playerNPC(cserv.getPlayerStorage().getCharacterByName(splitted[1]), Integer.parseInt(splitted[2]));
        else if (splitted[0].equals("!saveall")) {
            for (ChannelServer ch : ChannelServer.getAllInstances())
                for (MapleCharacter chr : ch.getPlayerStorage().getAllCharacters())
                    chr.saveToDB(true);
            player.message("Done.");
        } else if (splitted[0].equals("!setgmlevel")) {
            cserv.getPlayerStorage().getCharacterByName(splitted[1]).setGMLevel(Integer.parseInt(splitted[2]));
            player.message("Done.");
        } else if (splitted[0].equals("!shutdown") || splitted[0].equals("!shutdownnow")) {
            int time = 60000;
            if (splitted.length > 1)
                time *= Integer.parseInt(splitted[1]);
            if (splitted[0].equals("!shutdownnow"))
                time = 1;
            cserv.shutdown(time);
        } else if (splitted[0].equals("!smega"))
            for (MapleCharacter mc : cserv.getPlayerStorage().getAllCharacters())
                mc.dropMessage(3, cserv.getPlayerStorage().getCharacterByName(splitted[1]).getName() + " : " + StringUtil.joinStringFrom(splitted, 3));
        else if (splitted[0].equals("!speak")) {
            MapleCharacter victim = cserv.getPlayerStorage().getCharacterByName(splitted[1]);
            victim.getMap().broadcastMessage(MaplePacketCreator.getChatText(victim.getId(), StringUtil.joinStringFrom(splitted, 2), victim.gmLevel() > 0, 0));
        } else if (splitted[0].equals("!sql"))
            try {
                PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(StringUtil.joinStringFrom(splitted, 1));
                ps.executeUpdate();
                ps.close();
                player.message("Done " + StringUtil.joinStringFrom(splitted, 1));
            } catch (Exception e) {
            }
        else if (splitted[0].equals("!zakum")) {
            player.getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800000), player.getPosition());
            for (int x = 8800003; x < 8800011; x++)
                player.getMap().spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(x), player.getPosition());
        } else
            player.message("Command " + splitted[0] + " does not exist");
    }
}