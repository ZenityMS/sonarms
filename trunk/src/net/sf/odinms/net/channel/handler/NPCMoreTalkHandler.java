

package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.scripting.npc.NPCConversationManager;
import net.sf.odinms.scripting.npc.NPCScriptManager;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public class NPCMoreTalkHandler extends AbstractMaplePacketHandler {

	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		byte lastMsg = slea.readByte();
		byte action = slea.readByte();

		if (lastMsg == 2) {
			String returnText = slea.readMapleAsciiString();
			NPCConversationManager cm = NPCScriptManager.getInstance().getCM(c);
			cm.setGetText(returnText);
			NPCScriptManager.getInstance().action(c, action, lastMsg, (byte) -1);
		} else {
			byte selection = -1;
			if (slea.available() > 0) {
				selection = slea.readByte();
			}
			NPCScriptManager.getInstance().action(c, action, lastMsg, selection);
		}
	}
}
