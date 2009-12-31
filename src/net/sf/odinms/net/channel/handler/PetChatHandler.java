

package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.MapleCharacter;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class PetChatHandler extends AbstractMaplePacketHandler {

	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int petId = slea.readInt();
		slea.readInt();
		slea.readByte();
		int act = slea.readByte();
		String text = slea.readMapleAsciiString();
		MapleCharacter player = c.getPlayer();
		player.getMap().broadcastMessage(player, MaplePacketCreator.petChat(player.getId(), c.getPlayer().getPetIndex(petId), act, text), true);
	}
}
