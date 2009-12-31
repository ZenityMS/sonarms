

package net.sf.odinms.net.channel.handler;

import java.sql.SQLException;

import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.server.MapleInventoryManipulator;
import net.sf.odinms.tools.MaplePacketCreator;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
*
* @author Acrylic (Terry Han)
*/
public class CouponCodeHandler extends AbstractMaplePacketHandler {
	private Logger log = LoggerFactory.getLogger(CouponCodeHandler.class);
	
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		log.info(slea.toString());
		c.getSession().write(MaplePacketCreator.enableActions());
	}
}
