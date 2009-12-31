package net.sf.odinms.server;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.net.channel.ChannelServer;
import net.sf.odinms.client.messages.MessageCallback;

/**
 *
 * @author bassoe
 */
public class AutoSave {
    public static void main(String[] args) {
        for (int i = 0; i < ChannelServer.getAllInstances().size(); i++) {
            for (MapleCharacter character : ChannelServer.getInstance(i).getPlayerStorage().getAllCharacters()) {
                if (character == null) continue;
                try {
                    character.saveToDB(true);
                } catch (Exception e) {
                }
            }
        }
    }
}  