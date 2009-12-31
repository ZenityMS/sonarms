

package net.sf.odinms.net.channel.handler;

import net.sf.odinms.client.ISkill;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.client.SkillFactory;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.net.MaplePacketHandler;
import net.sf.odinms.server.MapleStatEffect;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import net.sf.odinms.tools.MaplePacketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CancelBuffHandler extends AbstractMaplePacketHandler implements MaplePacketHandler {

	private Logger log = LoggerFactory.getLogger(CancelBuffHandler.class);
	
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int sourceid = slea.readInt();
		MapleStatEffect effect;
		ISkill skill = SkillFactory.getSkill(sourceid);
		
		if (sourceid == 3121004 || sourceid == 3221001 || sourceid == 2121001 || sourceid == 2221001 || sourceid == 2321001) {
			c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.skillCancel(c.getPlayer(), sourceid), false);
		}
		
		effect = skill.getEffect(1); // hack but we don't know the level that was casted on us ï¿½.o
		c.getPlayer().cancelEffect(effect, false, -1);
	}
}
