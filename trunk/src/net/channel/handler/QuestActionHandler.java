package net.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import scripting.quest.QuestScriptManager;
import server.quest.MapleQuest;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public class QuestActionHandler extends AbstractMaplePacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte action = slea.readByte();
        short quest = slea.readShort();
        MapleCharacter player = c.getPlayer();
        if (action == 1) { // start quest
            int npc = slea.readInt();
            slea.readInt(); // dont know *o*
            MapleQuest.getInstance(quest).start(player, npc);
        } else if (action == 2) { // complete quest
            int npc = slea.readInt();
            slea.readInt(); // dont know *o*
            if (slea.available() >= 4) {
                int selection = slea.readInt();
                MapleQuest.getInstance(quest).complete(player, npc, selection);
            } else
                MapleQuest.getInstance(quest).complete(player, npc);
        } else if (action == 3) // forfeit quest
            MapleQuest.getInstance(quest).forfeit(player);
        else if (action == 4) { // scripted start quest
            int npc = slea.readInt();
            slea.readInt(); // dont know *o*
            QuestScriptManager.getInstance().start(c, npc, quest);
        } else if (action == 5) { // scripted end quests
            int npc = slea.readInt();
            slea.readInt(); // dont know *o*
            QuestScriptManager.getInstance().end(c, npc, quest);
        }
    }
}
