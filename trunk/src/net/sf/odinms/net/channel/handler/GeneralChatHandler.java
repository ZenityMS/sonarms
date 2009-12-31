

package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.messages.CommandProcessor;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.StringUtil;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class GeneralChatHandler extends AbstractMaplePacketHandler {
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		String text = slea.readMapleAsciiString();
		int show = slea.readByte();

		if (!CommandProcessor.getInstance().processCommand(c, text)) {
			if (StringUtil.countCharacters(text, '@') > 4 || StringUtil.countCharacters(text, '%') > 4 ||
				StringUtil.countCharacters(text, '+') > 6 || StringUtil.countCharacters(text, '$') > 6 ||
				StringUtil.countCharacters(text, '&') > 6 || StringUtil.countCharacters(text, '~') > 6) {
				text = "I suck, big time. Don't listen to anything I say.";
			}
                        if (text.equalsIgnoreCase("cc plz")) {
                c.getPlayer().finishAchievement(14);
            }  
			c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getChatText(c.getPlayer().getId(), text, c.getPlayer().isGM() && c.getChannelServer().allowGmWhiteText(), show));
		}
	}
}